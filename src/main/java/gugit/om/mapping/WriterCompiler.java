package gugit.om.mapping;


import gugit.om.metadata.DetailCollectionFieldMetadata;
import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.FieldMetadata;
import gugit.om.metadata.MasterRefFieldMetadata;
import gugit.om.utils.StringTemplate;

import java.util.LinkedList;
import java.util.List;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class WriterCompiler {

	private final String ID_ACCESS_FIELD_TEMPLATE = 
		"	private static PropertyAccessor idAccess = new %ID_ACCESS_CLASS_NAME%();\n";
		
	private final String PROP_ACCESS_SET_VALUE =
		"		public void setValue(Object obj, Object id){\n"+
		"			((%ENTITY_TYPE%)obj).set%ID_FIELD_NAME%((%ID_FIELD_TYPE%)id);\n"+
		"		}\n";
	
	private final String PROP_ACCESS_GET_VALUE = 
		"		public Object getValue(Object obj){\n"+
		"			return ((%ENTITY_TYPE%)obj).get%ID_FIELD_NAME%();\n"+
		"		}\n";
	
	private final String WRITE_METHOD_TEMPLATE = 
	  " public void write(Object obj, WriteBatch batch){\n"
	
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
			  " private static Dependency %DEPENDENCY_VAR_NAME% "
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
	+ "			getWriter(%POJO_TYPE%.class).write(entity.get%PROPERTY_NAME%(), batch);\n";

	private final String WRITE_POJO_COLLECTION_SNIPPLET = 
	  "		if (entity.get%PROPERTY_NAME%() !=null)\n"
	+ "			getWriter(%POJO_TYPE%.class).writeAll(entity.get%PROPERTY_NAME%(), batch);\n";
					
	private final String WRITE_MASTER_DEPENDENCY_SNIPPLET = 
	  "		{\n"
	+ "			if (entity.get%MASTER_ACCESSOR_NAME%() == null)\n"
	+ "				writePacket.addElement(\"%COLUMN_NAME%\", \"%MASTER_ACCESSOR_NAME%\", null);\n"
	+ "			else\n"
	+ "			{\n"
	+ "				getWriter(%MASTER_CLASS%.class).write(entity.get%MASTER_ACCESSOR_NAME%(), batch);\n"
	+ "				if (entity.get%MASTER_ACCESSOR_NAME%().get%MASTER_PROPERTY_NAME%() == null)\n"
	+ "					writePacket.addDependency(%DEPENDENCY_VAR_NAME%);\n"
	+ "				else\n"
	+ "					writePacket.addElement(\"%COLUMN_NAME%\", "
											+ "\"%MASTER_ACCESSOR_NAME%\", "
											+ "entity.get%MASTER_ACCESSOR_NAME%().get%MASTER_PROPERTY_NAME%());\n"
	+ "			}\n"
	+ "		}\n";	
	
	// temp
	// TODO: refactor to parameter
	private CtClass ctEntityClass;
	
	private ClassPool pool;

	public WriterCompiler(){		
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit.om.mapping");
		this.pool.importPackage("gugit.om.utils");
	}

	public boolean doesWriterClassExist(Class<?> entityClass) {
		return pool.getOrNull( getGeneratedClassName(entityClass.getCanonicalName())) != null;
	}

	@SuppressWarnings("unchecked")
	public Class<AbstractWriter> getExistingWriterClass(Class<?> entityClass) throws ClassNotFoundException {
		return (Class<AbstractWriter>) Class.forName(getGeneratedClassName(entityClass.getCanonicalName()));
	}

	public Class<AbstractWriter> makeWriterClass(EntityMetadata<?> entityMetadata) throws Exception{
		return makeWriterClass(entityMetadata.getEntityClass(), 
								entityMetadata.getIdField(), 
								entityMetadata.getPrimitiveFields(), 
								entityMetadata.getPojoFields(), 
								entityMetadata.getPojoCollectionFields(), 
								entityMetadata.getMasterRefFields() );
	}
	
	
	@SuppressWarnings("unchecked")
	private Class<AbstractWriter> makeWriterClass(Class<?> entityClass,
												FieldMetadata idField, 
												LinkedList<FieldMetadata> primitiveFields,
												LinkedList<FieldMetadata> pojoFields,
												LinkedList<DetailCollectionFieldMetadata> pojoCollectionFields,
												LinkedList<MasterRefFieldMetadata> masterReferenceFields) throws Exception{		
		String entityClassName = entityClass.getCanonicalName();

		this.ctEntityClass = pool.get(entityClassName);
		
		String idAccessClassName = entityClass.getCanonicalName()+"$$GUGIT$$IdAccessor";
		
		CtClass accClass = pool.makeClass(idAccessClassName);
		accClass.addInterface(pool.get("gugit.om.mapping.PropertyAccessor"));
		
		accClass.addMethod(CtNewMethod.make(createM1(entityClassName, idField), accClass));
		accClass.addMethod(CtNewMethod.make(createM2(entityClassName, idField), accClass));
		accClass.toClass();
		
		
		for (MasterRefFieldMetadata masterInfo: masterReferenceFields){
			String refClassName = getMasterDependencyClassName(entityClassName, masterInfo);
			
			CtClass depClass = pool.makeClass(refClassName);
			depClass.addInterface(pool.get("gugit.om.mapping.Dependency"));
			
			depClass.addMethod(CtNewMethod.make(createDependencyClass(entityClassName, masterInfo), depClass));
			depClass.toClass();
		}
		
		CtClass resultClass = pool.makeClass(getGeneratedClassName(entityClassName));		
		resultClass.setSuperclass( pool.get("gugit.om.mapping.AbstractWriter") );

		String accessFieldSrc = new StringTemplate(ID_ACCESS_FIELD_TEMPLATE)
											.replace("ID_ACCESS_CLASS_NAME", idAccessClassName)
											.getResult();
		
		resultClass.addField(CtField.make(accessFieldSrc, resultClass));
		
		for (MasterRefFieldMetadata masterRef: masterReferenceFields){
			String depClassName = getMasterDependencyClassName(entityClassName, masterRef);
			String depVarName = getMasterDependencyVarName(masterRef);
			
			String masterRefFieldSrc = new StringTemplate(PARENT_DEPENDENCY_FIELD_TEMPLATE)
												.replace("DEPENDENCY_VAR_NAME", depVarName)
												.replace("DEPENDENCY_CLASS_NAME", depClassName)
												.getResult();
			resultClass.addField(CtField.make(masterRefFieldSrc, resultClass));
		}
		
		String writeMethodSrc = new StringTemplate(WRITE_METHOD_TEMPLATE)
										.replace("ID_COLUMN_NAME", idField.getColumnName())
										.replace("ID_FIELD_NAME", capitalize(idField.getName()))
										.replace("ENTITY_TYPE", entityClassName)
										.replace("WRITE_PRIMITIVE_PROPERTIES_SNIPPLET", createPrimPropsSnipplet(primitiveFields))
										.replace("WRITE_POJO_PROPERTIES_SNIPPLET", createPojoPropsSnipplet(pojoFields))
										.replace("WRITE_POJO_COLLECTIONS_SNIPPLET", createPojoColectionsSnipplet(pojoCollectionFields))
										.replace("WRITE_MASTER_DEPENDENCIES_SNIPPLET", createMasterRefSnipplet(masterReferenceFields) )
										.getResult();

		resultClass.addMethod(CtNewMethod.make(writeMethodSrc, resultClass));

		return resultClass.toClass();
	}
	
	private String createDependencyClass(String entityClassName, MasterRefFieldMetadata masterRef) throws NotFoundException {
		return new StringTemplate(SOLVE_METHOD_TEMPLATE)
						.replace("MASTER_ACCESSOR_NAME", capitalize(masterRef.getName()))
						.replace("MASTER_PROPERTY_NAME", capitalize(masterRef.getMasterIDName()))
						.replace("PROPERTY_TYPE", Integer.class.getCanonicalName()) // this.getSetterParamType(masterRef.getName()))
						.replace("ENTITY_TYPE", entityClassName)
						.replace("COLUMN_NAME", masterRef.getColumnName())
						.getResult();
	}

	private String createM2(String entityClassName, FieldMetadata idField) throws NotFoundException {
		return new StringTemplate(PROP_ACCESS_GET_VALUE)
					.replace("ENTITY_TYPE", entityClassName)
					.replace("ID_FIELD_NAME", capitalize(idField.getName()))
					.replace("ID_FIELD_TYPE", getSetterParamType(idField.getName()))
					.getResult();	
	}

	private String createM1(String entityClassName, FieldMetadata idField) throws NotFoundException {
		return new StringTemplate(PROP_ACCESS_SET_VALUE)
					.replace("ENTITY_TYPE", entityClassName)
					.replace("ID_FIELD_NAME", capitalize(idField.getName()))
					.replace("ID_FIELD_TYPE", getSetterParamType(idField.getName()))
					.getResult();
	}

	private String createPrimPropsSnipplet(List<FieldMetadata> primitiveFields) {
		StringBuilder src = new StringBuilder();
		
		for (FieldMetadata fieldInfo: primitiveFields)
			src.append(new StringTemplate(WRITE_PRIMITIVE_PROPERTY_SNIPPLET)
							.replace("COLUMN_NAME", fieldInfo.getColumnName())
							.replace("PROPERTY_NAME", capitalize(fieldInfo.getName()))
							.getResult());
		
		return src.toString();
	}

	private String createPojoPropsSnipplet(List<FieldMetadata> pojoFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (FieldMetadata fieldInfo: pojoFields)
			src.append(new StringTemplate(WRITE_POJO_PROPERTY_SNIPPLET)
							.replace("PROPERTY_NAME", capitalize(fieldInfo.getName()))
							.replace("POJO_TYPE", getSetterParamType(fieldInfo.getName()))
							.getResult());
		return src.toString();
	}

	private String createPojoColectionsSnipplet(List<DetailCollectionFieldMetadata> pojoCollectionFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (DetailCollectionFieldMetadata fieldInfo: pojoCollectionFields)
			src.append(new StringTemplate(WRITE_POJO_COLLECTION_SNIPPLET)
							.replace("PROPERTY_NAME", capitalize(fieldInfo.getName()))
							.replace("POJO_TYPE", fieldInfo.getDetailType().getCanonicalName())
							.getResult());
		return src.toString();	
	}

	private String createMasterRefSnipplet(List<MasterRefFieldMetadata> masterReferenceFields) throws NotFoundException {
		StringBuilder src = new StringBuilder();
		for (MasterRefFieldMetadata fieldInfo: masterReferenceFields)
			src.append(new StringTemplate(WRITE_MASTER_DEPENDENCY_SNIPPLET)
							.replace("MASTER_ACCESSOR_NAME", capitalize(fieldInfo.getName()))
							.replace("COLUMN_NAME", fieldInfo.getColumnName())
							.replace("MASTER_CLASS", getSetterParamType(fieldInfo.getName()))
							.replace("MASTER_PROPERTY_NAME", capitalize(fieldInfo.getMasterIDName()))
							.replace("DEPENDENCY_VAR_NAME", getMasterDependencyVarName(fieldInfo))
							.getResult());
		return src.toString();	
	}

	private static String capitalize(String name) {
		return name.substring(0,1).toUpperCase()+name.substring(1);
	}
	
	private String getSetterParamType(final String fieldName) throws NotFoundException {
		return ctEntityClass
				.getDeclaredMethod("set"+capitalize(fieldName))
				.getParameterTypes()[0]
				.getName();
	}
	
	private String getGeneratedClassName(String entityClassName) {
		return entityClassName+"$$GugitWriter";
	}

	private String getMasterDependencyVarName(MasterRefFieldMetadata masterRef) {
		return "$dependency_to_"
					+masterRef.getName()
					+"_$_"
					+masterRef.getMasterIDName();
	}

	private String getMasterDependencyClassName(String entityClassName, MasterRefFieldMetadata masterInfo) {
		return entityClassName
					+"$$GUGIT$$"
					+masterInfo.getName()
					+"$$"+
					masterInfo.getMasterIDName()
					+"Dependency";
	}

}
