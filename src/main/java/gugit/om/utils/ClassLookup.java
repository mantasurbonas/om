package gugit.om.utils;

import javassist.ClassPool;
import javassist.CtClass;

public class ClassLookup {

	private ClassLoader classLoader;
	private ClassPool classPool;
	
	public interface ClassGenerator{
		public <T> Class<T> generateClass(Class<T> superclass, String name, ClassLoader classLoader, ClassPool classPool);
	}

	public ClassLookup(ClassLoader classLoader, ClassPool classPool){
		this.classLoader = classLoader;
		this.classPool = classPool;
	}
	
	public <T> Class<T> getClass(Class<T> superclass, String name, ClassGenerator generator) throws Exception{
		Class<T> knownClass = getKnownClass(name);
		if (knownClass != null)
			return knownClass;
		
		synchronized(this){
			knownClass = getKnownClass(name);
			if (knownClass != null)
				return knownClass;
			
			Class<T> classFromPool = addClassFromPoolToClassloader(superclass, name);
			if (classFromPool != null)
				return classFromPool;
			
			return generator.generateClass(superclass, name, classLoader, classPool);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Class<T> getKnownClass(String className){
		try{
			return (Class<T>) classLoader.loadClass(className);
		}catch(Exception e){
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> Class<T> addClassFromPoolToClassloader(Class<T> superclass, String className) throws Exception{
		CtClass resultClass = classPool.getOrNull(className);
		if (resultClass != null)
			return (Class<T>) classPool.get( className).toClass(classLoader, superclass.getProtectionDomain());
		
		return null;
	}
}
