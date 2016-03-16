package gugit.om.mapping;

import gugit.om.utils.StringTemplate;

import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * Generates entity-specific Readers (de-serializers) in run time, using javassist.
 * 
 * @author urbonman
 *
 */
public class ReaderCompiler {
	
	private static final String READ_METHOD_TEMPLATE = 
			  " public Object read(ArrayIterator row, int position, ReadContext readContext){ \n"
			+ "   if (row.isOutOfBounds(position)) \n"
			+ "       return null; \n"
			
			+ "   gugit.research.AbstractReader.State state = getState(position); \n"
			+ "   Object id = row.peek( position + %ID_COL_OFFSET% ); \n"
			
			+ "   if (id == null){  \n"
			+ "     state.lastId = null; \n"
			+ "	    state.lastEntity = null; \n"
			+ "     return null; \n"
			+ "   } \n"

			+ "   %ENTITY_CLASS_NAME% entity = (%ENTITY_CLASS_NAME%)state.lastEntity; \n"
			
			+ "  if (!id.equals(state.lastId)){ \n"
			+ "     entity = new %ENTITY_CLASS_NAME%();  \n"
			+ "     entity.set%ID_SETTER_METHOD%( (%ID_TYPE%) id);  \n"
			
			+"      %ADD_TO_READ_CONTEXT% \n"
			
			+ "     %FIELDS_MAPPING_SNIPPLET% \n"
			
			+ "     state.lastEntity = entity; \n"
			+ "     state.lastId = id; \n"
			+ "  } else {\n"
			+"      %ADD_TO_READ_CONTEXT% \n"
			+ "  }\n"
			
			+ "  %DETAILS_COLLECTION_MAPPING_SNIPPLET% \n"
			
			+ "  %MASTER_SETTING_SNIPPLET% \n"
			
			+"   %REMOVE_FROM_READ_CONTEXT% \n"
			
			+ "  return entity; \n"
			+ " } \n";
	
	/***
	 * ReadContext is only needed for master entity lookup.
	 * thus we may skip this method invocation if we are certain we will NOT need any master entity lookups.
	 */
	private static final String ADD_TO_READ_CONTEXT_TEMPLATE=
			 "     readContext.entityIsBeingRead(entity, id); \n";

	/***
	 * ReadContext is only needed for master entity lookup.
	 * thus we may skip this method invocation if we are certain we will NOT need any master entity lookups.
	 */
	private static final String END_READ_CONTEXT_TEMPLATE=
			"      readContext.entityReadingFinished(); \n";
	
	private static final String SIMPLE_FIELD_MAPPING_SNIPPLET_TEMPLATE = 
			  "  entity.set%FIELD_NAME%( (%FIELD_TYPE%) row.peek( position + %FIELD_COL_OFFSET% ) ); \n";
	
	private static final String POJO_FIELD_MAPPING_SNIPPLET_TEMPLATE = 
			   " {  \n"
			+  "   %DETAIL_TYPE% detail = ( %DETAIL_TYPE% ) getReader(%DETAIL_TYPE%.class).read(row, position + %POJO_START_OFFSET%, readContext);  \n"
			+  "   if (detail!=null) \n"
			+  "      entity.set%FIELD_NAME%(detail);  \n"
			+  " } \n";
	
	private static final String ADD_DETAIL_TO_COLLECTION_SNIPPLET_TEMPLATE = 
		 	  " {  \n"
			+ "    %DETAIL_TYPE% detail = ( %DETAIL_TYPE% ) getReader(%DETAIL_TYPE%.class).read(row, position + %POJO_START_OFFSET%, readContext);  \n"
			+ "    if ((detail != null) && !entity.get%FIELD_NAME%().contains(detail))  \n"
			+ "        entity.get%FIELD_NAME%().add(detail);  \n"
			+ " }  \n";
	
	private static final String SET_MASTER_SNIPPLET_TEMPLATE =
			  "{ \n"
			+ "  Object masterId = row.peek( position + %COL_OFFSET%); \n"
			+ "  if (masterId != null){\n"
			+ "    %MASTER_TYPE% master = (%MASTER_TYPE%) readContext.findEntity(%MASTER_TYPE%.class, masterId);\n"
			+ "    if (master != null)\n"
			+ "        entity.set%MASTER_FIELD%(master); \n"
			+ "  }\n"
			+ " } \n";
			
	private ClassPool pool;

	// temp
	// TODO: refactor to parameter
	private CtClass ctEntityClass;
	
	public static class FieldInfo{
		public int columnOffset;
		public String capitalizedName;
		public Class<?> fieldType;
		
		public FieldInfo(int colOffset, String capitalizedFieldName){
			this(colOffset, capitalizedFieldName, null);
		}
		public FieldInfo(int colOffset, String capitalizedFieldName, Class<?> fieldType){
			this.columnOffset=colOffset;
			this.capitalizedName=capitalizedFieldName;
			this.fieldType=fieldType;
		}
	};


	public ReaderCompiler(Class<?> entityClass){		
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit.research");
	}

	public boolean doesReaderClassExist(Class<?> entityClass) {
		return pool.getOrNull( getGeneratedClassName(entityClass.getCanonicalName())) != null;
	}

	@SuppressWarnings("unchecked")
	public Class<AbstractReader> getExistingReaderClass(Class<?> entityClass) throws ClassNotFoundException {
		return (Class<AbstractReader>) Class.forName(getGeneratedClassName(entityClass.getCanonicalName()));
	}

	@SuppressWarnings("unchecked")
	public Class<AbstractReader> makeReaderClass(Class<?> entityClass,
												FieldInfo idField, 
												List<FieldInfo> primitiveFields, 
												List<FieldInfo> pojoFields, 
												List<FieldInfo> pojoCollectionFields,
												List<FieldInfo> masterReferenceFields) throws Exception {

		String entityClassName = entityClass.getCanonicalName();
		
		CtClass resultClass = pool.makeClass(getGeneratedClassName(entityClassName));		
		resultClass.setSuperclass( pool.get("gugit.research.AbstractReader") );
		
		this.ctEntityClass = pool.get(entityClassName);

		boolean needsReadContext = pojoFields.size()+pojoCollectionFields.size() > 0;
		
		String methodSrc = new StringTemplate(READ_METHOD_TEMPLATE)
									.replace("ID_COL_OFFSET", ""+idField.columnOffset)
									.replace("ENTITY_CLASS_NAME", entityClassName)
									.replace("ID_SETTER_METHOD", idField.capitalizedName)
									.replace("ID_TYPE", getSetterParamType("set"+idField.capitalizedName))
									.replace("FIELDS_MAPPING_SNIPPLET", createFieldsMappingSrc(primitiveFields, pojoFields))
									.replace("DETAILS_COLLECTION_MAPPING_SNIPPLET", createDetailsCollectionMappingSrc(pojoCollectionFields))
									.replace("MASTER_SETTING_SNIPPLET", createMasterSettingSnipplet(masterReferenceFields))
									
									.replace("ADD_TO_READ_CONTEXT", needsReadContext?ADD_TO_READ_CONTEXT_TEMPLATE: "")
									
									.replace("REMOVE_FROM_READ_CONTEXT", needsReadContext?END_READ_CONTEXT_TEMPLATE: "")
									
									.removeUnusedKeys()
									.getResult();
		
		resultClass.addMethod(CtNewMethod.make(methodSrc, resultClass));

		return resultClass.toClass();
	}
	
	private String createFieldsMappingSrc(List<FieldInfo> simpleFieldSetters, List<FieldInfo> details)throws Exception {
		
		StringBuilder src = new StringBuilder();
		
		for (FieldInfo field: simpleFieldSetters)
			src.append(createSimpleFieldSetterSrc(field));

		for (FieldInfo field: details)
			src.append(createOneToOneDetailsSrc(field));
		
		return src.toString();
	}
	
	private String createDetailsCollectionMappingSrc(List<FieldInfo> detailCollections){
		StringBuilder src = new StringBuilder();
		
		for (FieldInfo field: detailCollections)
			src.append(createAddDetailSnipplet(field));
		
		return src.toString();
	}


	private String createMasterSettingSnipplet(List<FieldInfo> masterFields) throws NotFoundException{
		StringBuilder src = new StringBuilder();
		
		for (FieldInfo field: masterFields)
			src.append(createMasterFieldSrc(field));
		
		return src.toString();
	}
	
	private String createMasterFieldSrc(FieldInfo field) throws NotFoundException {
		return new StringTemplate(SET_MASTER_SNIPPLET_TEMPLATE)
							.replace("COL_OFFSET", ""+field.columnOffset)
							.replace("MASTER_TYPE", getSetterParamType("set"+field.capitalizedName))
							.replace("MASTER_FIELD", field.capitalizedName)
							.getResult();
	}

	private String createSimpleFieldSetterSrc(FieldInfo fieldInfo) throws NotFoundException {		
		return new StringTemplate(SIMPLE_FIELD_MAPPING_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", fieldInfo.capitalizedName)
							.replace("FIELD_TYPE", getSetterParamType("set"+fieldInfo.capitalizedName) )
							.replace("FIELD_COL_OFFSET", ""+fieldInfo.columnOffset)
							.getResult();
	}

	private String createOneToOneDetailsSrc(FieldInfo fieldInfo) throws NotFoundException {
		return new StringTemplate(POJO_FIELD_MAPPING_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", fieldInfo.capitalizedName)
							.replace("DETAIL_TYPE", getSetterParamType("set"+fieldInfo.capitalizedName ) )
							.replace("POJO_START_OFFSET", ""+fieldInfo.columnOffset)
							.getResult();
	}

	private String createAddDetailSnipplet(FieldInfo fieldInfo) {
		return new StringTemplate(ADD_DETAIL_TO_COLLECTION_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", fieldInfo.capitalizedName)
							.replace("DETAIL_TYPE", fieldInfo.fieldType.getCanonicalName())
							.replace("POJO_START_OFFSET", ""+fieldInfo.columnOffset)
							.getResult();
	}
	
	
	private String getSetterParamType(final String setterName) throws NotFoundException {
		return ctEntityClass
				.getDeclaredMethod(setterName)
				.getParameterTypes()[0]
				.getName();
	}

	private String getGeneratedClassName(String entityClassName) {
		return entityClassName+"$$GugitReader";
	}

}
