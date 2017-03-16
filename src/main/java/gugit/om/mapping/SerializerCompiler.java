package gugit.om.mapping;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.IEntityMetadataFactory;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class SerializerCompiler {	
	
	private ClassPool pool;
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

	public boolean doesSerializerClassExist(Class<?> entityClass) {
		return pool.getOrNull( getGeneratedClassName(entityClass.getCanonicalName())) != null;
	}

   @SuppressWarnings("unchecked")
   public <T> Class<ISerializer<T>> getExistingSerializerClass(Class<T> entityClass) throws Exception {
        String serializerClassName = getGeneratedClassName(entityClass.getCanonicalName());
       
        try{
            return (Class<ISerializer<T>>) Class.forName(serializerClassName);
        }catch(Exception e){        	
            return getExistingSerializerClassFromClassloader(entityClass, serializerClassName, getClass().getClassLoader());
        }        
    }

   @SuppressWarnings("unchecked")
   private <T> Class<ISerializer<T>> getExistingSerializerClassFromClassloader(Class<T> entityClass, 
		   																		String serializerClassName, 
		   																		ClassLoader classLoader)
		   																		throws CannotCompileException, NotFoundException {
		synchronized(classLoader){
		    try{
		    	Class<ISerializer<T>> ret= (Class<ISerializer<T>>) Class.forName(serializerClassName, true, classLoader);
		    	if (ret != null){
		    		logger.error("race condition avoided: retrieved a class in classloader on a second try");
		    		return ret;
		    	}
		    }catch(Exception ee){
		    	// expected
		    }
		    
		    List<String> relatedClasses = writerCompiler.getRelatedClassNames(entityClass); 
		    for (String relatedClass: relatedClasses)
		    	try{
		    		pool.get(relatedClass).toClass(classLoader, null);
		    	}catch(Exception eee){
		    		logger.error("Failed loading related class "+relatedClass+" for "+entityClass, eee );
		    	}
		    
		    return (Class<ISerializer<T>>) pool.get(serializerClassName).toClass(classLoader, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<ISerializer<T>> makeSerializerClass(Class<T> entityClass) throws Exception {
		EntityMetadata<T> entityMetadata = metadataFactory.getMetadataFor(entityClass);
		
		String entityClassName = entityClass.getCanonicalName();
		
		String generatedClassName = getGeneratedClassName(entityClassName);
		
		CtClass resultClass = pool.getOrNull(generatedClassName);
		if (resultClass != null)
		    return getExistingSerializerClass(entityClass);
//			return (Class<ISerializer<T>>)Class.forName(generatedClassName);
		
		logger.debug("compiling serializer class for "+entityClassName);
		
		resultClass = pool.makeClass(generatedClassName);
		resultClass.addInterface( pool.get("gugit.om.mapping.ISerializer") );

		writerCompiler.addWriterMethods(resultClass, entityMetadata);
		readerCompiler.addReaderMethods(resultClass, entityMetadata);
		mergerCompiler.addMergerMethods(resultClass, entityMetadata);
		
		return resultClass.toClass();
	}	
	

	private String getGeneratedClassName(String entityClassName) {
		return entityClassName+"$$GugitSerializer";
	}
	
}
