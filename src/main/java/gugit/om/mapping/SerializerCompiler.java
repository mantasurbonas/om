package gugit.om.mapping;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.IEntityMetadataFactory;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;

public class SerializerCompiler {	
	
	private ClassPool pool;
	private ClassLoader classLoader = getClass().getClassLoader();
	
	private IEntityMetadataFactory metadataFactory;
	
	private WriterCompiler writerCompiler;
	private ReaderCompiler readerCompiler;
	private MergerCompiler mergerCompiler;

	private static final Logger logger = LogManager.getLogger();
	
	public SerializerCompiler(IEntityMetadataFactory metadataFactory){
		this.metadataFactory = metadataFactory;
		
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit.om.mapping");
		this.pool.importPackage("gugit.om.utils");
		this.pool.importPackage("gugit.om.wrapping");
		
		pool.insertClassPath(new ClassClassPath(ISerializer.class));
		
		this.writerCompiler = new WriterCompiler(metadataFactory, pool);
		this.readerCompiler = new ReaderCompiler();
		this.mergerCompiler = new MergerCompiler();
	}

	public <T> Class<ISerializer<T>> getSerializerClassFor(Class<T> entityClass) throws Exception {
		String serializerClassName = getSerializerClassName(entityClass.getCanonicalName());
		
		Class<ISerializer<T>> knownClass = getKnownClass(serializerClassName);
		if (knownClass != null)
			return knownClass;
		
		synchronized(this){
			knownClass = getKnownClass(serializerClassName);
			if (knownClass != null)
				return knownClass;
			
			Class<ISerializer<T>> classFromPool = addClassFromPoolToClassloader(entityClass, serializerClassName);
			if (classFromPool != null)
				return classFromPool;
			
			return makeSerializerClass(entityClass, serializerClassName);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Class<ISerializer<T>>  getKnownClass(String className){
		try{
			return (Class<ISerializer<T>>) classLoader.loadClass(className);
		}catch(ClassNotFoundException e){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> Class<ISerializer<T>> addClassFromPoolToClassloader(Class<T> entityClass, String serializerClassName) throws Exception{
		CtClass resultClass = pool.getOrNull(serializerClassName);
		if (resultClass == null)
			return null;
	
	    List<String> relatedClasses = writerCompiler.getRelatedClassNames(entityClass); 
	    for (String relatedClass: relatedClasses)
	    	try{
	    		pool.get(relatedClass).toClass(classLoader, null);
	    	}catch(Exception eee){
	    		logger.error("Failed loading related class "+relatedClass+" for "+entityClass, eee );
	    	}
		    
	    return (Class<ISerializer<T>>) pool.get(serializerClassName).toClass(classLoader, null);
	}
	
	@SuppressWarnings("unchecked")
	private <T> Class<ISerializer<T>> makeSerializerClass(Class<T> entityClass, String serializerClassName) throws Exception {
		logger.debug("compiling serializer class for "+entityClass);
		
		EntityMetadata<T> entityMetadata = metadataFactory.getMetadataFor(entityClass);

		// the above might have just caused a recursive call to getSerializerClassFor() !
		// need to check if we haven't just created the serializer class already....
		CtClass resultClass = pool.getOrNull(serializerClassName); 
		if (resultClass != null){
			Class<ISerializer<T>> knownClass = getKnownClass(serializerClassName);
			if (knownClass != null)
				return knownClass;
			
			return (Class<ISerializer<T>>) resultClass.toClass(classLoader, null);
		}
		
		resultClass = pool.makeClass(serializerClassName);
		resultClass.addInterface( pool.get("gugit.om.mapping.ISerializer") );

		writerCompiler.addWriterMethods(resultClass, entityMetadata);
		readerCompiler.addReaderMethods(resultClass, entityMetadata);
		mergerCompiler.addMergerMethods(resultClass, entityMetadata);
		
		return resultClass.toClass();
	}	
	

	private String getSerializerClassName(String entityClassName) {
		return entityClassName+"$$GugitSerializer";
	}
	
}
