package gugit.om.mapping;

import gugit.om.metadata.ColumnFieldMetadata;
import gugit.om.metadata.DetailCollectionFieldMetadata;
import gugit.om.metadata.EntityMetadata;
import gugit.om.utils.StringTemplate;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class MergerCompiler {
	
	private static final String GET_ID_METHOD_TEMPLATE = 
			" public Object getID(Object entity){ \n"
			+ "   return ((%ENTITY_CLASS_NAME%)entity).get%ID_SETTER_METHOD%(); \n"
			+ " }\n";
	
	private static final String GET_PROPERTY_INDEX_METHOD_TEMPLATE = 
			" public int getPropertyIndex(String p){\n"
			+ "    %PROPERTY_INDEX_SNIPPLETS% \n"
			+ "    throw new java.lang.RuntimeException(\"cannot find property \"+p+\" in the entity \"); \n"
			+ " }\n";
	
	private static final String PROPERTY_INDEX_SNIPPLETS = 
			  "    if (p.equals(\"%PROP%\")) return %INDEX%; \n";
	
	private static final String MERGE_METHOD_TEMPLATE = 
			"public void leftJoin(Object obj, int propIndex, IDataIterator row, int position, ReadContext readContext){ \n"
			+ "   if (row.isOutOfBounds(position)) \n"
			+ "       throw new java.lang.RuntimeException(\"Resultset is empty when left-join merging entity with a new resultset\"); \n"
			
			+ "   position = position - propIndex + 1; \n"
			
			+ "  %ENTITY_CLASS_NAME% entity = (%ENTITY_CLASS_NAME%)obj; \n"
			+ "  Object id = row.peek(0); \n"
			
			+ "   %ADD_TO_READ_CONTEXT%   \n"
			
			+ "  switch(propIndex){ \n"
			+ "     %CASE_SNIPPLETS% \n"
			+ "     default: throw new java.lang.RuntimeException(\"When merging entity, could not find property for index \"+propIndex+\". \");"
			+ "  }\n"
			
			+ "  %REMOVE_FROM_READ_CONTEXT% \n"
			
			+ "  gugit.om.wrapping.EntityMarkingHelper.setDirty(entity, false); \n"
			
			+ "}\n";
	
	private static final String MERGE_METHOD_CASE_SNIPPLET=
			"     case %OFFSET%: { %MAPPING_SNIPPLET% } break; \n";
	
	public <T> void addMergerMethods(CtClass resultClass, EntityMetadata<T> entityMetadata) throws Exception{
		String getIdMethodSrc = createGetIdMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(getIdMethodSrc, resultClass));
		
		String getPropertyMethodSrc = createGetPropertyMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(getPropertyMethodSrc, resultClass));
		
		String leftJoinMethodSrc = createLeftJoinMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(leftJoinMethodSrc, resultClass));
	}
	
	private String createGetIdMethodSrc(EntityMetadata<?> metadata){
		return new StringTemplate(GET_ID_METHOD_TEMPLATE)
					.replace("ENTITY_CLASS_NAME", metadata.getEntityClass().getCanonicalName())
					.replace("ID_SETTER_METHOD", capitalize(metadata.getIdField().getName()))
					.getResult();
	}
	
	private String createGetPropertyMethodSrc(EntityMetadata<?> metadata){
		StringBuilder snipplets = new StringBuilder();
		StringTemplate snipplet = new StringTemplate(PROPERTY_INDEX_SNIPPLETS);
		
		for (ColumnFieldMetadata field: metadata.getPrimitiveFields())
			snipplets.append(snipplet.
								reset().
								replace("PROP", field.getName()).
								replace("INDEX", ""+field.getColumnOffset()).
								getResult());

		for (ColumnFieldMetadata field: metadata.getPojoFields())
			snipplets.append(snipplet.
								reset().
								replace("PROP", field.getName()).
								replace("INDEX", ""+field.getColumnOffset()).
								getResult());
		
		for (DetailCollectionFieldMetadata field: metadata.getPojoCollectionFields())
			snipplets.append(snipplet.
								reset().
								replace("PROP", field.getName()).
								replace("INDEX", ""+field.getColumnOffset()).
								getResult());

		for (ColumnFieldMetadata field: metadata.getMasterRefFields())
			snipplets.append(snipplet.
								reset().
								replace("PROP", field.getName()).
								replace("INDEX", ""+field.getColumnOffset()).
								getResult());
		
		snipplets.append(snipplet
							.reset()
							.replace("PROP", metadata.getIdField().getName())
							.replace("INDEX", ""+metadata.getIdField().getColumnOffset())
							.getResult());	
		
		return new StringTemplate(GET_PROPERTY_INDEX_METHOD_TEMPLATE)
					.replace("PROPERTY_INDEX_SNIPPLETS", snipplets.toString())
					.getResult();
	}
	
	private String createLeftJoinMethodSrc(EntityMetadata<?> metadata) throws Exception{
		StringBuilder snipplets = new StringBuilder();
		
		StringTemplate snipplet = new StringTemplate(MERGE_METHOD_CASE_SNIPPLET);
		
		for (ColumnFieldMetadata field: metadata.getPrimitiveFields())
			snipplets.append(snipplet.
								reset().
								replace("OFFSET", ""+field.getColumnOffset()).
								replace("MAPPING_SNIPPLET", createSimpleFieldSetterSrc(field)).
								getResult());

		for (ColumnFieldMetadata field: metadata.getPojoFields())
			snipplets.append(snipplet.
								reset().
								replace("OFFSET", ""+field.getColumnOffset()).
								replace("MAPPING_SNIPPLET", createOneToOneDetailsSrc(field)).
								getResult());
		
		for (DetailCollectionFieldMetadata field: metadata.getPojoCollectionFields())
			snipplets.append(snipplet.
								reset().
								replace("OFFSET", ""+field.getColumnOffset()).
								replace("MAPPING_SNIPPLET", createAddDetailSnipplet(field)).
								getResult());

		for (ColumnFieldMetadata field: metadata.getMasterRefFields())
			snipplets.append(snipplet.
								reset().
								replace("OFFSET", ""+field.getColumnOffset()).
								replace("MAPPING_SNIPPLET", createMasterFieldSrc(field)).
								getResult());
		
		boolean needsReadContext = metadata.getPojoFields().size() + metadata.getPojoCollectionFields().size() > 0;
		
		return new StringTemplate(MERGE_METHOD_TEMPLATE)
					.replace("ENTITY_CLASS_NAME", metadata.getEntityClass().getCanonicalName())
					.replace("CASE_SNIPPLETS", snipplets.toString())
					.replace("ADD_TO_READ_CONTEXT", needsReadContext?ReaderCompiler.ADD_TO_READ_CONTEXT_TEMPLATE: "")
					.replace("REMOVE_FROM_READ_CONTEXT", needsReadContext?ReaderCompiler.END_READ_CONTEXT_TEMPLATE: "")
					.getResult();
	}
	
	private String createSimpleFieldSetterSrc(ColumnFieldMetadata fieldInfo) throws NotFoundException {		
		return new StringTemplate(ReaderCompiler.SIMPLE_FIELD_MAPPING_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("FIELD_TYPE", fieldInfo.getType().getCanonicalName() )
							.replace("FIELD_COL_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}
	
	private String createMasterFieldSrc(ColumnFieldMetadata field) throws NotFoundException {
		return new StringTemplate(ReaderCompiler.SET_MASTER_SNIPPLET_TEMPLATE)
							.replace("COL_OFFSET", ""+field.getColumnOffset())
							.replace("MASTER_TYPE", field.getType().getCanonicalName())
							.replace("MASTER_FIELD", capitalize(field.getName()))
							.getResult();
	}
	
	private String createOneToOneDetailsSrc(ColumnFieldMetadata fieldInfo) throws NotFoundException {
		return new StringTemplate(ReaderCompiler.POJO_FIELD_MAPPING_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("DETAIL_TYPE", fieldInfo.getType().getCanonicalName() )
							.replace("POJO_START_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}

	private String createAddDetailSnipplet(DetailCollectionFieldMetadata fieldInfo) {
		return new StringTemplate(ReaderCompiler.ADD_DETAIL_TO_COLLECTION_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("DETAIL_TYPE", fieldInfo.getType().getCanonicalName())
							.replace("POJO_START_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}
	
	
	private String capitalize(String name) {
		return name.substring(0,1).toUpperCase()+name.substring(1);
	}

}
