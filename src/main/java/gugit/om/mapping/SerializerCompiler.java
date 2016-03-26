package gugit.om.mapping;

import gugit.om.metadata.ColumnFieldMetadata;
import gugit.om.metadata.DetailCollectionFieldMetadata;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.FieldMetadata;
import gugit.om.metadata.IEntityMetadataFactory;
import gugit.om.utils.StringTemplate;

import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class SerializerCompiler {

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
			
			+ "}\n";
	
	private static final String MERGE_METHOD_CASE_SNIPPLET=
			"     case %OFFSET%: { %MAPPING_SNIPPLET% } break; \n";
	
	private static final String READ_METHOD_TEMPLATE = 
			  " public Object read(IDataIterator row, int position, ReadContext readContext){ \n"
			+ "   if (row.isOutOfBounds(position)) \n"
			+ "       return null; \n"
			
			+ "   Object id = row.peek( position + %ID_COL_OFFSET% ); \n"
			
			+ "   if (id == null){  \n"
			+ "     readContext.cacheRead(position, null); \n"
			+ "     return null; \n"
			+ "   } \n"

			+ "   %ENTITY_CLASS_NAME% entity = (%ENTITY_CLASS_NAME%)readContext.getCachedRead(position); \n"
			
			+ "  if (entity == null || !id.equals(entity.get%ID_SETTER_METHOD%())){ \n"
			+ "     entity = new %ENTITY_CLASS_NAME%();  \n"
			+ "     entity.set%ID_SETTER_METHOD%( (%ID_TYPE%) id);  \n"
			
			+"      %ADD_TO_READ_CONTEXT% \n"
			
			+ "     %FIELDS_MAPPING_SNIPPLET% \n"
			
			+ "     readContext.cacheRead(position, entity); \n"
			
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
			+  "   %DETAIL_TYPE% detail = ( %DETAIL_TYPE% ) readContext.getReaderFor(%DETAIL_TYPE%.class).read(row, position + %POJO_START_OFFSET%, readContext);  \n"
			+  "   if (detail!=null) \n"
			+  "      entity.set%FIELD_NAME%(detail);  \n"
			+  " } \n";
	
	private static final String ADD_DETAIL_TO_COLLECTION_SNIPPLET_TEMPLATE = 
		 	  " {  \n"
			+ "    %DETAIL_TYPE% detail = ( %DETAIL_TYPE% ) readContext.getReaderFor(%DETAIL_TYPE%.class).read(row, position + %POJO_START_OFFSET%, readContext);  \n"
			+ "    if ((detail != null) && !entity.get%FIELD_NAME%().contains(detail))  \n"
			+ "        entity.get%FIELD_NAME%().add(detail);  \n"
			+ " }  \n";
	
	private static final String SET_MASTER_SNIPPLET_TEMPLATE =
			  "{ \n"
			+ "  Object masterId = row.peek( position + %COL_OFFSET%); \n"
			+ "  if (masterId != null){\n"
			+ "    %MASTER_TYPE% master = (%MASTER_TYPE%) readContext.findMasterEntity(%MASTER_TYPE%.class, masterId);\n"
			+ "    if (master != null)\n"
			+ "        entity.set%MASTER_FIELD%(master); \n"
			+ "  }\n"
			+ " } \n";
			
	private final String ID_ACCESS_FIELD_TEMPLATE = 
			"	private static IPropertyAccessor idAccess = new %ID_ACCESS_CLASS_NAME%();\n";
			
		private final String PROP_ACCESS_SET_VALUE =
			"		public void setValue(Object obj, Object id){\n"+
			"			((%ENTITY_TYPE%)obj).set%ID_FIELD_NAME%((%ID_FIELD_TYPE%)id);\n"+
			"		}\n";
		
		private final String PROP_ACCESS_GET_VALUE = 
			"		public Object getValue(Object obj){\n"+
			"			return ((%ENTITY_TYPE%)obj).get%ID_FIELD_NAME%();\n"+
			"		}\n";
		
		private final String WRITE_METHOD_TEMPLATE = 
		  " public void write(Object obj, WriteBatch batch, WriteContext writeContext){\n"
		
	    + "		if (obj == null)\n"
		+ "			return;\n"
		
		+ "		if (batch.isEntityScheduledAlready(obj))\n"
		+ "			return;\n"
		
		+ "		WritePacket writePacket = batch.createWritePacket(obj);\n"
		
		+ "		writePacket.setID(\"%ID_COLUMN_NAME%\", \"%ID_FIELD_NAME%\", idAccess);\n"
		
		+ "		%ENTITY_TYPE% entity = (%ENTITY_TYPE%)obj;\n"

		+ "     %WRITE_PRIMITIVE_PROPERTIES_SNIPPLET% \n"
		
		+ "     %WRITE_POJO_PROPERTIES_SNIPPLET% \n"
		
		+ "     %WRITE_POJO_COLLECTIONS_SNIPPLET% \n"

		+ "     %WRITE_MASTER_DEPENDENCIES_SNIPPLET% \n"
		
		+ "	}\n";

		private final String PARENT_DEPENDENCY_FIELD_TEMPLATE = 
				  " private static IDependency %DEPENDENCY_VAR_NAME% "
				  + " = new %DEPENDENCY_CLASS_NAME%();\n ";
		
		private final String SOLVE_METHOD_TEMPLATE = 
				  "		public Object solve(Object obj) {\n"
				+ "			%PROPERTY_TYPE% $$id=((%ENTITY_TYPE%)obj).get%MASTER_ACCESSOR_NAME%().get%MASTER_PROPERTY_NAME%();\n"
				+ "			if ($$id==null)\n"
				+ "				return null;\n"
				+ "			return new Object[]{\"%COLUMN_NAME%\", \"%MASTER_ACCESSOR_NAME%\", $$id};\n"
				+ "		}\n";
					
		private final String  WRITE_PRIMITIVE_PROPERTY_SNIPPLET=
		  "		writePacket.addElement(\"%COLUMN_NAME%\", \"%PROPERTY_NAME%\", entity.get%PROPERTY_NAME%());\n";	
				
		private final String WRITE_POJO_PROPERTY_SNIPPLET =
		  "		if (entity.get%PROPERTY_NAME%() != null)\n"
		+ "			writeContext.getWriterFor(%POJO_TYPE%.class).write(entity.get%PROPERTY_NAME%(), batch, writeContext);\n";

		private final String WRITE_POJO_COLLECTION_SNIPPLET = 
		  "		if (entity.get%PROPERTY_NAME%() !=null){\n"
		+ "        IWriter wr = writeContext.getWriterFor(%POJO_TYPE%.class);\n"
		+ "		   java.util.Iterator it = entity.get%PROPERTY_NAME%().iterator(); \n"
		+ "        while(it.hasNext()) \n"
		+ "			  wr.write(it.next(), batch, writeContext);\n"
		+ "     }\n";
						
		private final String WRITE_MASTER_DEPENDENCY_SNIPPLET = 
		  "		{\n"
		+ "			if (entity.get%MASTER_ACCESSOR_NAME%() == null)\n"
		+ "				writePacket.addElement(\"%COLUMN_NAME%\", \"%MASTER_ACCESSOR_NAME%\", null);\n"
		+ "			else\n"
		+ "			{\n"
		+ "				writeContext.getWriterFor(%MASTER_CLASS%.class).write(entity.get%MASTER_ACCESSOR_NAME%(), batch, writeContext);\n"
		+ "				if (entity.get%MASTER_ACCESSOR_NAME%().get%MASTER_PROPERTY_NAME%() == null)\n"
		+ "					writePacket.addDependency(%DEPENDENCY_VAR_NAME%);\n"
		+ "				else\n"
		+ "					writePacket.addElement(\"%COLUMN_NAME%\", "
												+ "\"%MASTER_ACCESSOR_NAME%\", "
												+ "entity.get%MASTER_ACCESSOR_NAME%().get%MASTER_PROPERTY_NAME%());\n"
		+ "			}\n"
		+ "		}\n";	

	
	private ClassPool pool;

	private IEntityMetadataFactory metadataFactory;

	public SerializerCompiler(IEntityMetadataFactory metadataFactory){
		this.metadataFactory = metadataFactory;
		
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit.om.mapping");
		this.pool.importPackage("gugit.om.utils");
	}

	public boolean doesSerializerClassExist(Class<?> entityClass) {
		return pool.getOrNull( getGeneratedClassName(entityClass.getCanonicalName())) != null;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<ISerializer<T>> getExistingSerializerClass(Class<T> entityClass) throws ClassNotFoundException {
		return (Class<ISerializer<T>>) Class.forName(getGeneratedClassName(entityClass.getCanonicalName()));
	}

	@SuppressWarnings("unchecked")
	public <T> Class<ISerializer<T>> makeSerializerClass(Class<T> entityClass) throws Exception {
		EntityMetadata<T> entityMetadata = metadataFactory.getMetadataFor(entityClass);
		
		String entityClassName = entityClass.getCanonicalName();
		
		System.out.println("compiling serializer class for "+entityClassName);
		
		String generatedClassName = getGeneratedClassName(entityClassName);
		
		CtClass resultClass = pool.getOrNull(generatedClassName);
		if (resultClass != null)
			return (Class<ISerializer<T>>)Class.forName(generatedClassName);
		
		resultClass = pool.makeClass(generatedClassName);
		
		resultClass.addInterface( pool.get("gugit.om.mapping.ISerializer") );

		createIDAccessClass(entityClass, entityMetadata.getIdField());
		createIdAccessField(entityClass, resultClass);
		
		for (ColumnFieldMetadata masterInfo: entityMetadata.getRefFields()){
			EntityMetadata<?> masterMeta = metadataFactory.getMetadataFor(masterInfo.getType());
			createDependencyClass(entityClass, masterMeta, masterInfo);
			createDependencyField(entityClass, resultClass, masterMeta, masterInfo);
		}
		
		createWriteMethod(entityMetadata, resultClass);
		
		String readMethodSrc = createReadMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(readMethodSrc, resultClass));
		
		String getIdMethodSrc = createGetIdMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(getIdMethodSrc, resultClass));
		
		String getPropertyMethodSrc = createGetPropertyMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(getPropertyMethodSrc, resultClass));
		
		String leftJoinMethodSrc = createLeftJoinMethodSrc(entityMetadata);
		resultClass.addMethod(CtNewMethod.make(leftJoinMethodSrc, resultClass));

		return resultClass.toClass();
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
					.replace("ADD_TO_READ_CONTEXT", needsReadContext?ADD_TO_READ_CONTEXT_TEMPLATE: "")
					.replace("REMOVE_FROM_READ_CONTEXT", needsReadContext?END_READ_CONTEXT_TEMPLATE: "")
					.getResult();
	}
	
	private String createReadMethodSrc(EntityMetadata<?> entityMetadata)throws Exception {
		
		boolean needsReadContext = entityMetadata.getPojoFields().size() + entityMetadata.getPojoCollectionFields().size() > 0;
		
		return new StringTemplate(READ_METHOD_TEMPLATE)
						.replace("ID_COL_OFFSET", ""+entityMetadata.getIdField().getColumnOffset())
						.replace("ENTITY_CLASS_NAME", entityMetadata.getEntityClass().getCanonicalName())
						.replace("ID_SETTER_METHOD", capitalize(entityMetadata.getIdField().getName()))
						.replace("ID_TYPE", entityMetadata.getIdField().getType().getCanonicalName())
						.replace("FIELDS_MAPPING_SNIPPLET", createFieldsMappingSrc(entityMetadata.getPrimitiveFields(), entityMetadata.getPojoFields()))
						.replace("DETAILS_COLLECTION_MAPPING_SNIPPLET", createDetailsCollectionMappingSrc(entityMetadata.getPojoCollectionFields()))
						.replace("MASTER_SETTING_SNIPPLET", createMasterSettingSnipplet(entityMetadata.getMasterRefFields()))
						.replace("ADD_TO_READ_CONTEXT", needsReadContext?ADD_TO_READ_CONTEXT_TEMPLATE: "")
						.replace("REMOVE_FROM_READ_CONTEXT", needsReadContext?END_READ_CONTEXT_TEMPLATE: "")
						.removeUnusedKeys()
						.dump(false)
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
	
	private String createDetailsCollectionMappingSrc(List<DetailCollectionFieldMetadata> detailCollections){
		StringBuilder src = new StringBuilder();
		
		for (DetailCollectionFieldMetadata field: detailCollections)
			src.append(createAddDetailSnipplet(field));
		
		return src.toString();
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

	private String createOneToOneDetailsSrc(ColumnFieldMetadata fieldInfo) throws NotFoundException {
		return new StringTemplate(POJO_FIELD_MAPPING_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("DETAIL_TYPE", fieldInfo.getType().getCanonicalName() )
							.replace("POJO_START_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}

	private String createAddDetailSnipplet(DetailCollectionFieldMetadata fieldInfo) {
		return new StringTemplate(ADD_DETAIL_TO_COLLECTION_SNIPPLET_TEMPLATE)
							.replace("FIELD_NAME", capitalize(fieldInfo.getName()))
							.replace("DETAIL_TYPE", fieldInfo.getType().getCanonicalName())
							.replace("POJO_START_OFFSET", ""+fieldInfo.getColumnOffset())
							.getResult();
	}
	
	private void createWriteMethod(EntityMetadata<?> entityMetadata, CtClass resultClass) throws Exception {
		String writeMethodSrc = new StringTemplate(WRITE_METHOD_TEMPLATE)
										.replace("ID_COLUMN_NAME", escape(entityMetadata.getIdField().getColumnName()))
										.replace("ID_FIELD_NAME", capitalize(entityMetadata.getIdField().getName()))
										.replace("ENTITY_TYPE", entityMetadata.getEntityClass().getCanonicalName())
										.replace("WRITE_PRIMITIVE_PROPERTIES_SNIPPLET", createPrimPropsSnipplet(entityMetadata.getPrimitiveFields()))
										.replace("WRITE_POJO_PROPERTIES_SNIPPLET", createPojoPropsSnipplet(entityMetadata.getPojoFields()))
										.replace("WRITE_POJO_COLLECTIONS_SNIPPLET", createPojoColectionsSnipplet(entityMetadata.getPojoCollectionFields()))
										.replace("WRITE_MASTER_DEPENDENCIES_SNIPPLET", createMasterRefSnipplet(entityMetadata.getRefFields()) )
										.dump(false)
										.getResult();

		resultClass.addMethod(CtNewMethod.make(writeMethodSrc, resultClass));
	}

	private void createIdAccessField(Class<?> entityClass, CtClass resultClass) throws CannotCompileException {
		String accessFieldSrc = new StringTemplate(ID_ACCESS_FIELD_TEMPLATE)
											.replace("ID_ACCESS_CLASS_NAME", getIdAccessClassName(entityClass))
											.dump(false)
											.getResult();
		
		resultClass.addField(CtField.make(accessFieldSrc, resultClass));
	}

	private void createDependencyField(Class<?> entityClass, CtClass resultClass, EntityMetadata<?> masterMeta, ColumnFieldMetadata masterRef) throws Exception {
		String depClassName = getMasterDependencyClassName(entityClass.getCanonicalName(), masterRef.getName(), masterMeta.getIdField().getName());
		String depVarName = getMasterDependencyVarName(masterRef.getName(), masterMeta.getIdField().getName());
		
		String masterRefFieldSrc = new StringTemplate(PARENT_DEPENDENCY_FIELD_TEMPLATE)
											.replace("DEPENDENCY_VAR_NAME", depVarName)
											.replace("DEPENDENCY_CLASS_NAME", depClassName)
											.dump(false)
											.getResult();
		resultClass.addField(CtField.make(masterRefFieldSrc, resultClass));
	}

	private void createDependencyClass(Class<?> entityClass, EntityMetadata<?> masterEntityMeta, ColumnFieldMetadata masterInfo) throws Exception{
		String entityClassName = entityClass.getCanonicalName();
		
		String masterRefName = masterInfo.getName();
		String masterId = masterEntityMeta.getIdField().getName();
		Class<?> masterIdType = masterEntityMeta.getIdField().getType();
		String dependencyClassName = getMasterDependencyClassName(entityClassName, masterRefName , masterId);
		
		String masterRefCol = masterInfo.getColumnName();
		CtClass depClass = pool.makeClass(dependencyClassName);
		depClass.addInterface(pool.get("gugit.om.mapping.IDependency"));
		depClass.addMethod(CtNewMethod.make(createSolveMethodSrc(entityClassName, 
																masterRefName, 
																masterId,
																masterIdType,
																masterRefCol), depClass));
		depClass.toClass();
	}

	private void createIDAccessClass(Class<?> entityClass, FieldMetadata idField) throws Exception{
		CtClass accClass = pool.makeClass(getIdAccessClassName(entityClass));
		accClass.addInterface(pool.get("gugit.om.mapping.IPropertyAccessor"));
		
		String entityClassName = entityClass.getCanonicalName();
		
		accClass.addMethod(CtNewMethod.make(createSetValueMethod(entityClassName, idField), accClass));
		accClass.addMethod(CtNewMethod.make(createGetValueMethod(entityClassName, idField), accClass));
		accClass.toClass();
	}

	private String getIdAccessClassName(Class<?> entityClass) {
		return entityClass.getCanonicalName()+"$$GUGIT$$IdAccessor";
	}
	
	private String createSolveMethodSrc(String entityClassName, 
										String masterRefName, 
										String masterIdName,
										Class<?> masterIdType,
										String masterRefCol) throws NotFoundException {
		return new StringTemplate(SOLVE_METHOD_TEMPLATE)
						.replace("MASTER_ACCESSOR_NAME", capitalize(masterRefName))
						.replace("MASTER_PROPERTY_NAME", capitalize(masterIdName))
						.replace("PROPERTY_TYPE", masterIdType.getCanonicalName())
						.replace("ENTITY_TYPE", entityClassName)
						.replace("COLUMN_NAME", escape(masterRefCol))
						.dump(false)
						.getResult();
	}

	private String createGetValueMethod(String entityClassName, FieldMetadata idField) throws NotFoundException {
		return new StringTemplate(PROP_ACCESS_GET_VALUE)
					.replace("ENTITY_TYPE", entityClassName)
					.replace("ID_FIELD_NAME", capitalize(idField.getName()))
					.replace("ID_FIELD_TYPE", idField.getType().getCanonicalName())
					.dump(false)
					.getResult();	
	}

	private String createSetValueMethod(String entityClassName, FieldMetadata idField) throws NotFoundException {
		return new StringTemplate(PROP_ACCESS_SET_VALUE)
					.replace("ENTITY_TYPE", entityClassName)
					.replace("ID_FIELD_NAME", capitalize(idField.getName()))
					.replace("ID_FIELD_TYPE", idField.getType().getCanonicalName())
					.dump(false)
					.getResult();
	}

	private String createPrimPropsSnipplet(List<ColumnFieldMetadata> primitiveFields) {
		StringBuilder src = new StringBuilder();
		
		for (ColumnFieldMetadata fieldInfo: primitiveFields)
			src.append(new StringTemplate(WRITE_PRIMITIVE_PROPERTY_SNIPPLET)
							.replace("COLUMN_NAME", escape(fieldInfo.getColumnName()))
							.replace("PROPERTY_NAME", capitalize(fieldInfo.getName()))
							.getResult());
		
		return src.toString();
	}

	private String createPojoPropsSnipplet(List<ColumnFieldMetadata> pojoFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (ColumnFieldMetadata fieldInfo: pojoFields)
			src.append(new StringTemplate(WRITE_POJO_PROPERTY_SNIPPLET)
							.replace("PROPERTY_NAME", capitalize(fieldInfo.getName()))
							.replace("POJO_TYPE", fieldInfo.getType().getCanonicalName())
							.getResult());
		return src.toString();
	}

	private String createPojoColectionsSnipplet(List<DetailCollectionFieldMetadata> pojoCollectionFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (DetailCollectionFieldMetadata fieldInfo: pojoCollectionFields)
			src.append(new StringTemplate(WRITE_POJO_COLLECTION_SNIPPLET)
							.replace("PROPERTY_NAME", capitalize(fieldInfo.getName()))
							.replace("POJO_TYPE", fieldInfo.getType().getCanonicalName())
							.getResult());
		return src.toString();	
	}

	private String createMasterRefSnipplet(List<ColumnFieldMetadata> masterReferenceFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (ColumnFieldMetadata masterRef: masterReferenceFields){
			EntityMetadata<?> masterEntityMeta = metadataFactory.getMetadataFor(masterRef.getType());
			src.append(new StringTemplate(WRITE_MASTER_DEPENDENCY_SNIPPLET)
							.replace("MASTER_ACCESSOR_NAME", capitalize(masterRef.getName()))
							.replace("COLUMN_NAME", escape(masterRef.getColumnName()))
							.replace("MASTER_CLASS", masterRef.getType().getCanonicalName())
							.replace("MASTER_PROPERTY_NAME", capitalize(masterEntityMeta.getIdField().getName()))
							.replace("DEPENDENCY_VAR_NAME", getMasterDependencyVarName(masterRef.getName(), masterEntityMeta.getIdField().getName()))
							.getResult());
		}
		return src.toString();	
	}


	private String getGeneratedClassName(String entityClassName) {
		return entityClassName+"$$GugitSerializer";
	}
	
	private String getMasterDependencyVarName(String masterRefName, String masterEntityId) {
		return "$dependency_to_"
					+masterRefName
					+"_$_"
					+masterEntityId;
	}

	private String getMasterDependencyClassName(String entityClassName, String masterRefName, String masterEntityId) {
		return entityClassName
					+"$$GUGIT$$"
					+masterRefName
					+"$$"
					+masterEntityId
					+"Dependency";
	}

	
	private String capitalize(String name) {
		return name.substring(0,1).toUpperCase()+name.substring(1);
	}
	
	private static String escape(final String str){
		return str.replace("\"", "\\\"");//.replace("\\", "\\\\");
	}
}
