package gugit.om.mapping;

import java.util.List;


import gugit.om.metadata.ColumnFieldMetadata;
import gugit.om.metadata.DetailCollectionFieldMetadata;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.EntityMetadataRegistry;
import gugit.om.metadata.FieldMetadata;
import gugit.om.metadata.IEntityMetadataFactory;
import gugit.om.metadata.ManyToManyFieldMetadata;
import gugit.om.utils.StringTemplate;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import gugit.om.utils.StringUtils;

public class WriterCompiler {

    private static final boolean DUMP_FLAG = false;

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

	private final String PARENT_DEPENDENCY_FIELD_TEMPLATE = 
	    "       private static IDependency %DEPENDENCY_VAR_NAME% "
	  + "                       = new %DEPENDENCY_CLASS_NAME%();\n ";

	private final String SOLVE_METHOD_TEMPLATE = 
		  "		public Object solve(Object obj) {\n"
		+ "			%PROPERTY_TYPE% $$id=((%ENTITY_TYPE%)obj).get%MASTER_ACCESSOR_NAME%().get%MASTER_PROPERTY_NAME%();\n"
		+ "			if ($$id==null)\n"
		+ "				return null;\n"
		+ "			return new Object[]{\"%COLUMN_NAME%\", \"%MASTER_ACCESSOR_NAME%\", $$id};\n"
		+ "		}\n";

	private final String GET_ID_ACCESSOR_TEMPLATE=
		"  public IPropertyAccessor getIdAccessor(){ return idAccess; } \n";
	
	private final String WRITE_METHOD_TEMPLATE = 
		  " public void write(Object obj, WriteBatch batch, WriteContext writeContext){\n"
		
	    + "		if (obj == null)\n"
		+ "			return;\n"
		
		+ "		if (batch.isEntityScheduledAlready(obj))\n"
		+ "			return;\n"
		
		+ "     boolean wasDirty = gugit.om.wrapping.EntityMarkingHelper.isDirty(obj); \n"
		
		+ "		EntityWritePacket writePacket = batch.createWritePacket(obj);\n"
		
		+ "		writePacket.setID(\"%ID_COLUMN_NAME%\", \"%ID_FIELD_NAME%\", idAccess);\n"
		
		+ "		%ENTITY_TYPE% entity = (%ENTITY_TYPE%)obj;\n"

		+ "     %WRITE_PRIMITIVE_PROPERTIES_SNIPPLET% \n"
		
		+ "     %WRITE_POJO_PROPERTIES_SNIPPLET% \n"
		
		+ "     %WRITE_POJO_COLLECTIONS_SNIPPLET% \n"

		+ "     %WRITE_MASTER_DEPENDENCIES_SNIPPLET% \n"
		
		+ "     gugit.om.wrapping.EntityMarkingHelper.setDirty(entity, false); \n"
		
		+ "	}\n";
	
	private final String  WRITE_PRIMITIVE_PROPERTY_SNIPPLET=
	  "    writePacket.addElement(\"%COLUMN_NAME%\", \"%PROPERTY_NAME%\", entity.get%PROPERTY_NAME%());\n";	

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
	
	private final String WRITE_MANY_TO_MANY_COLLECTION_SNIPPLET = 
	  "     if (entity.get%PROPERTY_NAME%()!=null){\n"
    + "        IWriter wr = writeContext.getWriterFor(%POJO_TYPE%.class); \n"
			  
	+ "		   java.util.Iterator it = entity.get%PROPERTY_NAME%().iterator(); \n"
	+ "        while(it.hasNext()) \n"
	+ "			  wr.write(it.next(), batch, writeContext);\n"
	
    + "        if (wasDirty) {\n"
    + "            M2MWritePacket m2mWritePacket = batch.createManyToManyWritePacket(entity, \"%M2M_TABLE_NAME%\");\n"
    + "            m2mWritePacket.setLeftSideDependency(\"%MY_COLUMN_NAME%\", \"%ENTITY_TYPE_SHORT_NAME%\", entity, idAccess);\n"	
    + "            m2mWritePacket.setRightSideDependency(\"%OTH_COLUMN_NAME%\", \"%POJO_TYPE_SHORT_NAME%\", entity.get%PROPERTY_NAME%(), wr.getIdAccessor(), \"%OTH_TABLE_NAME%\", \"%OTH_TABLE_ID_NAME%\");\n"
    + "        } \n"
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

	public WriterCompiler(IEntityMetadataFactory metadataFactory, ClassPool pool){
		this.pool = pool;
		this.metadataFactory = metadataFactory;
	}
	
	public <T> void addWriterMethods(CtClass resultClass, EntityMetadata<T> entityMetadata) throws Exception{
		Class<T> entityClass = entityMetadata.getEntityClass();
		
		createIDAccessClass(entityClass, entityMetadata.getIdField());
		createIdAccessField(entityClass, resultClass);

		for (ColumnFieldMetadata masterInfo: entityMetadata.getRefFields()){
			EntityMetadata<?> masterMeta = metadataFactory.getMetadataFor(masterInfo.getType());
			createDependencyClass(entityClass, masterMeta, masterInfo);
			createDependencyField(entityClass, resultClass, masterMeta, masterInfo);
		}
		
		createWriteMethod(entityMetadata, resultClass);
		
		createGetIdAccessorMethod(resultClass);
	}
	
	private void createGetIdAccessorMethod(CtClass resultClass) throws Exception {
		resultClass.addMethod(CtNewMethod.make(GET_ID_ACCESSOR_TEMPLATE, resultClass));		
	}

	private String getIdAccessClassName(Class<?> entityClass) {
		return entityClass.getCanonicalName()+"$$GUGIT$$IdAccessor";
	}
	
	private void createIDAccessClass(Class<?> entityClass, FieldMetadata idField) throws Exception{
		CtClass accClass = pool.makeClass(getIdAccessClassName(entityClass));
		accClass.addInterface(pool.get("gugit.om.mapping.IPropertyAccessor"));
		
		String entityClassName = entityClass.getCanonicalName();
		
		accClass.addMethod(CtNewMethod.make(createSetValueMethod(entityClassName, idField), accClass));
		accClass.addMethod(CtNewMethod.make(createGetValueMethod(entityClassName, idField), accClass));
		accClass.toClass();
	}
	
	private void createIdAccessField(Class<?> entityClass, CtClass resultClass) throws CannotCompileException {
		String accessFieldSrc = new StringTemplate(ID_ACCESS_FIELD_TEMPLATE)
											.replace("ID_ACCESS_CLASS_NAME", getIdAccessClassName(entityClass))
											.dump(DUMP_FLAG)
											.getResult();
		
		resultClass.addField(CtField.make(accessFieldSrc, resultClass));
	}

	private String createSetValueMethod(String entityClassName, FieldMetadata idField) throws NotFoundException {
		return new StringTemplate(PROP_ACCESS_SET_VALUE)
					.replace("ENTITY_TYPE", entityClassName)
					.replace("ID_FIELD_NAME", StringUtils.capitalize(idField.getName()))
					.replace("ID_FIELD_TYPE", idField.getType().getCanonicalName())
					.dump(DUMP_FLAG)
					.getResult();
	}

	private String createGetValueMethod(String entityClassName, FieldMetadata idField) throws NotFoundException {
		return new StringTemplate(PROP_ACCESS_GET_VALUE)
					.replace("ENTITY_TYPE", entityClassName)
					.replace("ID_FIELD_NAME", StringUtils.capitalize(idField.getName()))
					.replace("ID_FIELD_TYPE", idField.getType().getCanonicalName())
					.dump(DUMP_FLAG)
					.getResult();	
	}
	
	private void createDependencyField(Class<?> entityClass, CtClass resultClass, EntityMetadata<?> masterMeta, ColumnFieldMetadata masterRef) throws Exception {
		String depClassName = getMasterDependencyClassName(entityClass.getCanonicalName(), masterRef.getName(), masterMeta.getIdField().getName());
		String depVarName = getMasterDependencyVarName(masterRef.getName(), masterMeta.getIdField().getName());
		
		String masterRefFieldSrc = new StringTemplate(PARENT_DEPENDENCY_FIELD_TEMPLATE)
											.replace("DEPENDENCY_VAR_NAME", depVarName)
											.replace("DEPENDENCY_CLASS_NAME", depClassName)
											.dump(DUMP_FLAG)
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

  	private String createSolveMethodSrc(String entityClassName, 
										String masterRefName, 
										String masterIdName,
										Class<?> masterIdType,
										String masterRefCol) throws NotFoundException {
		return new StringTemplate(SOLVE_METHOD_TEMPLATE)
						.replace("MASTER_ACCESSOR_NAME", StringUtils.capitalize(masterRefName))
						.replace("MASTER_PROPERTY_NAME", StringUtils.capitalize(masterIdName))
						.replace("PROPERTY_TYPE", masterIdType.getCanonicalName())
						.replace("ENTITY_TYPE", entityClassName)
						.replace("COLUMN_NAME", StringUtils.escape(masterRefCol))
						.dump(DUMP_FLAG)
						.getResult();
	}

	private void createWriteMethod(EntityMetadata<?> entityMetadata, CtClass resultClass) throws Exception {
		String writeMethodSrc = new StringTemplate(WRITE_METHOD_TEMPLATE)
										.replace("ID_COLUMN_NAME", StringUtils.escape(entityMetadata.getIdField().getColumnName()))
										.replace("ID_FIELD_NAME", StringUtils.capitalize(entityMetadata.getIdField().getName()))
										.replace("ENTITY_TYPE", entityMetadata.getEntityClass().getCanonicalName())
										.replace("WRITE_PRIMITIVE_PROPERTIES_SNIPPLET", createPrimPropsSnipplet(entityMetadata.getPrimitiveFields()))
										.replace("WRITE_POJO_PROPERTIES_SNIPPLET", createPojoPropsSnipplet(entityMetadata.getPojoFields()))
										.replace("WRITE_POJO_COLLECTIONS_SNIPPLET", createPojoColectionsSnipplet(entityMetadata.getEntityClass(), entityMetadata.getPojoCollectionFields()))
										.replace("WRITE_MASTER_DEPENDENCIES_SNIPPLET", createMasterRefSnipplet(entityMetadata.getRefFields()) )
										.dump(DUMP_FLAG)
										.getResult();

		resultClass.addMethod(CtNewMethod.make(writeMethodSrc, resultClass));
	}

	private String createPrimPropsSnipplet(List<ColumnFieldMetadata> primitiveFields) {
		StringBuilder src = new StringBuilder();
		
		for (ColumnFieldMetadata fieldInfo: primitiveFields)
			src.append(new StringTemplate(WRITE_PRIMITIVE_PROPERTY_SNIPPLET)
							.replace("COLUMN_NAME", StringUtils.escape(fieldInfo.getColumnName()))
							.replace("PROPERTY_NAME", StringUtils.capitalize(fieldInfo.getName()))
							.getResult());
		
		return src.toString();
	}

	private String createPojoPropsSnipplet(List<ColumnFieldMetadata> pojoFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (ColumnFieldMetadata fieldInfo: pojoFields)
			src.append(new StringTemplate(WRITE_POJO_PROPERTY_SNIPPLET)
							.replace("PROPERTY_NAME", StringUtils.capitalize(fieldInfo.getName()))
							.replace("POJO_TYPE", fieldInfo.getType().getCanonicalName())
							.getResult());
		return src.toString();
	}

	private String createPojoColectionsSnipplet(Class<?> entityClass, List<DetailCollectionFieldMetadata> pojoCollectionFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (DetailCollectionFieldMetadata fieldInfo: pojoCollectionFields){
			if (fieldInfo instanceof ManyToManyFieldMetadata){
				ManyToManyFieldMetadata m2mField = (ManyToManyFieldMetadata) fieldInfo;
				
				EntityMetadata<?> detailMetadata = metadataFactory.getMetadataFor(m2mField.getType());
				
				src.append(new StringTemplate(WRITE_MANY_TO_MANY_COLLECTION_SNIPPLET)
								.replace("PROPERTY_NAME", StringUtils.capitalize(fieldInfo.getName()))
								.replace("POJO_TYPE", fieldInfo.getType().getCanonicalName())
								.replace("M2M_TABLE_NAME", StringUtils.escape(m2mField.getTableName()))
								.replace("MY_COLUMN_NAME", StringUtils.escape(m2mField.getMyColumnName()))
								.replace("POJO_TYPE_SHORT_NAME", fieldInfo.getType().getSimpleName())
								.replace("ENTITY_TYPE_SHORT_NAME", entityClass.getSimpleName())
								.replace("OTH_COLUMN_NAME", StringUtils.escape(m2mField.getOthColumnName()))
								.replace("OTH_TABLE_NAME", StringUtils.escape(detailMetadata.getEntityName()))
								.replace("OTH_TABLE_ID_NAME", StringUtils.escape(detailMetadata.getIdField().getColumnName()))
								.getResult());
			}
			else{
				src.append(new StringTemplate(WRITE_POJO_COLLECTION_SNIPPLET)
								.replace("PROPERTY_NAME", StringUtils.capitalize(fieldInfo.getName()))
								.replace("POJO_TYPE", fieldInfo.getType().getCanonicalName())
								.getResult());
			}
		}
		return src.toString();	
	}

	private String createMasterRefSnipplet(List<ColumnFieldMetadata> masterReferenceFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (ColumnFieldMetadata masterRef: masterReferenceFields){
			EntityMetadata<?> masterEntityMeta = metadataFactory.getMetadataFor(masterRef.getType());
			src.append(new StringTemplate(WRITE_MASTER_DEPENDENCY_SNIPPLET)
							.replace("MASTER_ACCESSOR_NAME", StringUtils.capitalize(masterRef.getName()))
							.replace("COLUMN_NAME", StringUtils.escape(masterRef.getColumnName()))
							.replace("MASTER_CLASS", masterRef.getType().getCanonicalName())
							.replace("MASTER_PROPERTY_NAME", StringUtils.capitalize(masterEntityMeta.getIdField().getName()))
							.replace("DEPENDENCY_VAR_NAME", getMasterDependencyVarName(masterRef.getName(), masterEntityMeta.getIdField().getName()))
							.getResult());
		}
		return src.toString();	
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
	
}
