package gugit.om.mapping;

import java.util.List;

import gugit.om.metadata.ColumnFieldMetadata;
import gugit.om.metadata.DetailCollectionFieldMetadata;
import gugit.om.metadata.EntityMetadata;
import gugit.om.utils.StringTemplate;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class ReaderCompiler {

	private static final String READ_METHOD_TEMPLATE = 
		  " public Object read(IDataIterator row, int position, ReadContext readContext){ \n"
		+ "   if (row.isOutOfBounds(position)) \n"
		+ "       return null; \n"
		
		+ "   Object id = row.peek( position + %ID_COL_OFFSET% ); \n"
		
		+ "   if (id == null){  \n"
		+ "     readContext.resetRead(position); \n"
		+ "     return null; \n"
		+ "   } \n"

		+ "   %ENTITY_CLASS_NAME% entity = (%ENTITY_CLASS_NAME%)readContext.getCachedRead(position); \n"
		
		+ "   if (entity == null || !id.equals(entity.get%ID_SETTER_METHOD%())){ \n"
		
		+ "     %RESET_CACHED_DETAILS% \n"
		
		+ "     entity = (%ENTITY_CLASS_NAME%)readContext.createEntity(%ENTITY_CLASS_NAME%.class);  \n"
		+ "     entity.set%ID_SETTER_METHOD%( (%ID_TYPE%) id);  \n"
		
		+ "     %ADD_TO_READ_CONTEXT% \n"
		
		+ "     %FIELDS_MAPPING_SNIPPLET% \n"
		
		+ "     readContext.cacheRead(position, entity); \n"
		
		+ "   } else {\n"
		+"      %ADD_TO_READ_CONTEXT% \n"
		+ "   }\n"
		
		+ "  %DETAILS_COLLECTION_MAPPING_SNIPPLET% \n"
		
		+ "  %MASTER_SETTING_SNIPPLET% \n"
		
		+"   %REMOVE_FROM_READ_CONTEXT% \n"
		
		+ "  gugit.om.wrapping.EntityMarkingHelper.setDirty(entity, false); \n"
		
		+ "  return entity; \n"
		+ " } \n";
	
	/***
	 * ReadContext is only needed for master entity lookup.
	 * thus we may skip this method invocation if we are certain we will NOT need any master entity lookups.
	 */
	static final String ADD_TO_READ_CONTEXT_TEMPLATE=
			 "     readContext.entityIsBeingRead(entity, id); \n";

	/***
	 * ReadContext is only needed for master entity lookup.
	 * thus we may skip this method invocation if we are certain we will NOT need any master entity lookups.
	 */
	static final String END_READ_CONTEXT_TEMPLATE=
			"      readContext.entityReadingFinished(); \n";
	
	static final String RESET_READ_TEMPLATE = 
			"      readContext.resetRead(position + %START_OFFSET%); \n";
	
	static final String SIMPLE_FIELD_MAPPING_SNIPPLET_TEMPLATE = 
			  "  entity.set%FIELD_NAME%( (%FIELD_TYPE%) row.peek( position + %FIELD_COL_OFFSET% ) ); \n";
	
	static final String POJO_FIELD_MAPPING_SNIPPLET_TEMPLATE = 
			  " {  \n"
			+  "   %DETAIL_TYPE% detail = ( %DETAIL_TYPE% ) readContext.getReaderFor(%DETAIL_TYPE%.class).read(row, position + %POJO_START_OFFSET%, readContext);  \n"
			+  "   if (detail!=null) \n"
			+  "      entity.set%FIELD_NAME%(detail);  \n"
			+  " } \n";
	
	static final String ADD_DETAIL_TO_COLLECTION_SNIPPLET_TEMPLATE =
		 	  " {  \n"
			+ "    %DETAIL_TYPE% detail = ( %DETAIL_TYPE% ) readContext.getReaderFor(%DETAIL_TYPE%.class).read(row, position + %POJO_START_OFFSET%, readContext);  \n"
			+ "    if ((detail != null) && !entity.get%FIELD_NAME%().contains(detail))  \n"
			+ "        entity.get%FIELD_NAME%().add(detail);  \n"
			+ " }  \n";
	
	static final String SET_MASTER_SNIPPLET_TEMPLATE =
			  "{ \n"
			+ "  Object masterId = row.peek( position + %COL_OFFSET%); \n"
			+ "  if (masterId != null){\n"
			+ "    %MASTER_TYPE% master = (%MASTER_TYPE%) readContext.findMasterEntity(%MASTER_TYPE%.class, masterId);\n"
			+ "    if (master != null)\n"
			+ "        entity.set%MASTER_FIELD%(master); \n"
			+ "  }\n"
			+ " } \n";

	private boolean debugFlag = true;
	
	public ReaderCompiler(){
	}
	
	public <T> void addReaderMethods(CtClass resultClass, EntityMetadata<T> entityMetadata) throws Exception{
		String readMethodSrc = createReadMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(readMethodSrc, resultClass));
	}
	
	private String createReadMethodSrc(EntityMetadata<?> entityMetadata)throws Exception {
		boolean needsReadContext = entityMetadata.getPojoFields().size() + entityMetadata.getPojoCollectionFields().size() > 0;
		
		return new StringTemplate(READ_METHOD_TEMPLATE)
						.replace("ID_COL_OFFSET", ""+entityMetadata.getIdField().getColumnOffset())
						.replace("ENTITY_CLASS_NAME", entityMetadata.getEntityClass().getCanonicalName())
						.replace("ID_SETTER_METHOD", capitalize(entityMetadata.getIdField().getName()))
						.replace("ID_TYPE", entityMetadata.getIdField().getType().getCanonicalName())
						.replace("RESET_CACHED_DETAILS", createResetReadsSnipplet(entityMetadata.getPojoCollectionFields(), entityMetadata.getPojoFields()))
						.replace("FIELDS_MAPPING_SNIPPLET", createFieldsMappingSrc(entityMetadata.getPrimitiveFields(), entityMetadata.getPojoFields()))
						.replace("DETAILS_COLLECTION_MAPPING_SNIPPLET", createDetailsCollectionMappingSrc(entityMetadata.getPojoCollectionFields()))
						.replace("MASTER_SETTING_SNIPPLET", createMasterSettingSnipplet(entityMetadata.getMasterRefFields()))
						.replace("ADD_TO_READ_CONTEXT", needsReadContext?ADD_TO_READ_CONTEXT_TEMPLATE: "")
						.replace("REMOVE_FROM_READ_CONTEXT", needsReadContext?END_READ_CONTEXT_TEMPLATE: "")
						.removeUnusedKeys()
						.dump(debugFlag)
						.getResult();
	}

	private String createResetReadsSnipplet(List<DetailCollectionFieldMetadata> lists, List<ColumnFieldMetadata> pojos) {
		StringBuilder src = new StringBuilder();
		
		StringTemplate line = new StringTemplate(RESET_READ_TEMPLATE);
		
		for (DetailCollectionFieldMetadata list: lists)
			src.append(line
						.reset()
						.replace("START_OFFSET", ""+list.getColumnOffset())
						.getResult());
		
		for (ColumnFieldMetadata pojo: pojos)
			src.append(line
						.reset()
						.replace("START_OFFSET", ""+pojo.getColumnOffset())
						.getResult());
		
		return src.toString();
	}

	private String createDetailsCollectionMappingSrc(List<DetailCollectionFieldMetadata> detailCollections){
		StringBuilder src = new StringBuilder();
		
		for (DetailCollectionFieldMetadata field: detailCollections)
			src.append(createAddDetailSnipplet(field));
		
		return src.toString();
	}
	
	private String createAddDetailSnipplet(DetailCollectionFieldMetadata fieldInfo) {
		return new StringTemplate(ADD_DETAIL_TO_COLLECTION_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("DETAIL_TYPE", fieldInfo.getType().getCanonicalName())
							.replace("POJO_START_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}

	private String createMasterSettingSnipplet(List<ColumnFieldMetadata> masterFields) throws NotFoundException{
		StringBuilder src = new StringBuilder();
		
		for (ColumnFieldMetadata field: masterFields)
			src.append(createMasterFieldSrc(field));
		
		return src.toString();
	}
	
	private String createMasterFieldSrc(ColumnFieldMetadata field) throws NotFoundException {
		return new StringTemplate(SET_MASTER_SNIPPLET_TEMPLATE)
							.replace("COL_OFFSET", ""+field.getColumnOffset())
							.replace("MASTER_TYPE", field.getType().getCanonicalName())
							.replace("MASTER_FIELD", capitalize(field.getName()))
							.getResult();
	}

	private String createSimpleFieldSetterSrc(ColumnFieldMetadata fieldInfo) throws NotFoundException {		
		return new StringTemplate(SIMPLE_FIELD_MAPPING_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("FIELD_TYPE", fieldInfo.getType().getCanonicalName() )
							.replace("FIELD_COL_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}
	
	private String createFieldsMappingSrc(List<ColumnFieldMetadata> simpleFieldSetters, List<ColumnFieldMetadata> details)throws Exception {
		
		StringBuilder src = new StringBuilder();
		
		for (ColumnFieldMetadata field: simpleFieldSetters)
			src.append(createSimpleFieldSetterSrc(field));

		for (ColumnFieldMetadata field: details)
			src.append(createOneToOneDetailsSrc(field));
		
		return src.toString();
	}

	private String createOneToOneDetailsSrc(ColumnFieldMetadata fieldInfo) throws NotFoundException {
		return new StringTemplate(POJO_FIELD_MAPPING_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("DETAIL_TYPE", fieldInfo.getType().getCanonicalName() )
							.replace("POJO_START_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}

	private String capitalize(String name) {
		return name.substring(0,1).toUpperCase()+name.substring(1);
	}

}
