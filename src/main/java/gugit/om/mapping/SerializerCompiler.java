package gugit.om.mapping;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.IEntityMetadataFactory;
import javassist.ClassPool;
import javassist.CtClass;

public class SerializerCompiler {	
	
	private ClassPool pool;
	private IEntityMetadataFactory metadataFactory;

	public SerializerCompiler(IEntityMetadataFactory metadataFactory){
		this.metadataFactory = metadataFactory;
		
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit.om.mapping");
		this.pool.importPackage("gugit.om.utils");
		this.pool.importPackage("gugit.om.wrapping");
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
		
		String generatedClassName = getGeneratedClassName(entityClassName);
		
		CtClass resultClass = pool.getOrNull(generatedClassName);
		if (resultClass != null)
			return (Class<ISerializer<T>>)Class.forName(generatedClassName);
		
		System.out.println("compiling serializer class for "+entityClassName);
		
		resultClass = pool.makeClass(generatedClassName);
		resultClass.addInterface( pool.get("gugit.om.mapping.ISerializer") );

		new WriterCompiler(metadataFactory, pool).addWriterMethods(resultClass, entityMetadata);
		new ReaderCompiler().addReaderMethods(resultClass, entityMetadata);
		new MergerCompiler().addMergerMethods(resultClass, entityMetadata);
		
		return resultClass.toClass();
	}	
	

	private String getGeneratedClassName(String entityClassName) {
		return entityClassName+"$$GugitSerializer";
	}
	
}
