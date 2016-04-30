package gugit.om.mapping;

import java.util.List;

import gugit.om.metadata.EntityMetadata;
import gugit.om.metadata.IEntityMetadataFactory;
import javassist.ClassPool;
import javassist.CtClass;

public class SerializerCompiler {	
	
	private ClassPool pool;
	private IEntityMetadataFactory metadataFactory;
	
	private WriterCompiler writerCompiler;
	private ReaderCompiler readerCompiler;
	private MergerCompiler mergerCompiler;
	
	public SerializerCompiler(IEntityMetadataFactory metadataFactory){
		this.metadataFactory = metadataFactory;
		
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit.om.mapping");
		this.pool.importPackage("gugit.om.utils");
		this.pool.importPackage("gugit.om.wrapping");
		
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
            System.out.println("failed finding a serializer for class "+entityClass+" in classloader, getting it from class pool");

            ClassLoader classLoader = getClass().getClassLoader();
            
            List<String> relatedClasses = writerCompiler.getRelatedClassNames(entityClass); 
            for (String relatedClass: relatedClasses)
                pool.get(relatedClass).toClass(classLoader, null);
            
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
		
		System.out.println("compiling serializer class for "+entityClassName);
		
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
