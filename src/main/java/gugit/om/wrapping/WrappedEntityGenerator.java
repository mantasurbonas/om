package gugit.om.wrapping;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class WrappedEntityGenerator {

	private static final String WRAPPED_ENTITY_INTERFACE_CLASS = "gugit.om.wrapping.IWrappedEntity";

	private static final String IS_DIRTY_METHOD_SRC = "public boolean isDirty(){ return $$dirty; }";

	private static final String CLEAR_DIRTY_METHOD_SRC = "public void clearDirty(){ $$dirty = false; }";

	private static final String SET_DIRTY_FLAG_METHOD_SRC = "public void setDirty(){ $$dirty = true; }";
		
	private static final String DIRTY_FLAG_FIELD_SRC = "private boolean $$dirty = true;";


	private static final Logger logger = LogManager.getLogger();
	
	private ClassPool pool;
	private ClassLoader classLoader = getClass().getClassLoader();
	
	
	private static WrappedEntityGenerator instance = new WrappedEntityGenerator();
	
	private WrappedEntityGenerator(){
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit");
	}
	
	public static WrappedEntityGenerator getInstance(){
		return instance;
	}
	
	public <T> Class<? extends T> getWrappedEntityClass(Class<? extends T> entityClass)throws Exception{
		if (entityClass.isInstance(IWrappedEntity.class))
			entityClass = unwrap(entityClass);
		
		String generatedClassName = getGeneratedClassName(entityClass.getCanonicalName());
		
		Class<T> knownClass = getKnownClass(generatedClassName);
		if (knownClass != null)
			return knownClass;
		
		synchronized(this){
			knownClass = getKnownClass(generatedClassName);
			if (knownClass != null)
				return knownClass;
			
			Class<T> classFromPool = addClassFromPoolToClassloader(generatedClassName);
			if (classFromPool != null)
				return classFromPool;
			
			return generateWrappedClass(entityClass, generatedClassName);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> getKnownClass(String className){
		try{
			return (Class<T>) classLoader.loadClass(className);
		}catch(Exception e){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> Class<T> addClassFromPoolToClassloader(String className) throws Exception{
		CtClass resultClass = pool.getOrNull(className);
		if (resultClass != null)
			return (Class<T>) pool.get( className).toClass(classLoader, null);
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Class<? extends T> generateWrappedClass(Class<T> superClass, String generatedClassName) throws Exception {
		logger.debug("Generating wrapper for entity class "+superClass);
		
		CtClass ctSuperClass = pool.getOrNull(superClass.getCanonicalName());
		if (ctSuperClass == null)
			throw new RuntimeException("Entity class "+superClass.getCanonicalName()+" could not be found.");
		
		CtClass resultClass = pool.makeClass(generatedClassName, ctSuperClass);
		resultClass.addInterface( pool.get(WRAPPED_ENTITY_INTERFACE_CLASS) );

		resultClass.addField(CtField.make(DIRTY_FLAG_FIELD_SRC, resultClass));
		resultClass.addMethod(CtNewMethod.make(SET_DIRTY_FLAG_METHOD_SRC, resultClass));
		resultClass.addMethod(CtNewMethod.make(CLEAR_DIRTY_METHOD_SRC, resultClass));
		resultClass.addMethod(CtNewMethod.make(IS_DIRTY_METHOD_SRC, resultClass));

		CtMethod[] methods = ctSuperClass.getMethods();
		for (CtMethod m: methods){
			
			if (m.getDeclaringClass().getName().equals("java.lang.Object"))
				continue;

			if (!isMutator(m))
				continue;
			
			String src = createMethodSrc(m);
			if (src == null)
				continue;

			resultClass.addMethod(CtNewMethod.make(src, resultClass));
		}
		
		return resultClass.toClass();
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> unwrap(Class<? extends T> wrappedClass){
		wrappedClass = (Class<? extends T>) wrappedClass.getSuperclass();
		return (Class<T>)wrappedClass;
	}

	private static String createMethodSrc(CtMethod m) throws NotFoundException{
		String modifier = "";
		switch(m.getModifiers()){
			case 0: modifier = ""; break;
			case 1: modifier = "public"; break;
			case 4: modifier = "protected"; break;
			default: return null;
		}

		String returnType = m.getReturnType().getName();
		String methodName = m.getName();
		
		String params = "";
		String args = "";
		String join = "";
		int i=0;
		for (CtClass paramType: m.getParameterTypes()){
			params += join + paramType.getName()+" arg"+i;
			args+=join + " arg"+i;
			join = ", ";
			i++;
		}
		String ret = (returnType=="void"?"":"return ");
		
		boolean isGetPojo = (methodName.startsWith("get") && returnType!="void" && params.isEmpty());
		
		String methodBody = "";
		if (isGetPojo)
			methodBody = " "+returnType+" $$ret=super."+methodName+"(); \n"
						+" if ($$ret!=null){ \n"
						+"     if (!$$dirty){ \n"
						//+"         System.out.println(\" invocation of "+methodName+" caused dirty flag\"); \n"
						+"         setDirty(); \n"
						+"     }"
						+" }"
						+" return $$ret; \n";
		else
			methodBody = " setDirty(); \n"
					   + " "+ret+" super."+methodName+"("+args+"); \n";
		
		return modifier+" " +returnType+" "+methodName+"("+params+")\n"
				+ "{ \n"
				+ methodBody
				+ "}\n";
	}
	
	public static boolean isMutator(CtMethod method) throws NotFoundException{
		if (method.getDeclaringClass().getName().equals("java.lang.Object"))
			return false;
		
		String name = method.getName();
		if(name.startsWith("get") && isSimpleReturnType(method.getReturnType()))
			return false;
		
		String returnTypeName = method.getReturnType().getName();
		
		if (name.equals("toString") && returnTypeName.equals("java.lang.String"))
			return false;
		
		if (name.equals("equals") && returnTypeName.equals("boolean"))
			return false;
		
		if (name.equals("hashCode") && method.getReturnType().equals("int"))
			return false;
		
		return true;
	}
	
	public static boolean isSimpleReturnType(CtClass returnType) {
		return returnType.isPrimitive() ||
				returnType.getName().equals("java.lang.Integer") ||
				returnType.getName().equals("java.lang.Double") ||
				returnType.getName().equals("java.lang.Float") ||
				returnType.getName().equals("java.math.BigDecimal") ||
				returnType.getName().equals("java.lang.Long") ||
				returnType.getName().equals("java.lang.Boolean") ||
				returnType.getName().equals("java.lang.Short") ||
				returnType.getName().equals("java.lang.Byte") ||
				returnType.getName().equals("java.lang.String") ||
				returnType.getName().equals("java.util.Date") ||
				returnType.getName().equals("java.sql.Date") ||
				returnType.getName().equals("java.sql.Timestamp") ||
				returnType.isEnum()
				;
	}
	
	private static String getGeneratedClassName(String entityClassname){
		return entityClassname+"$$GugitWrapped";
	}

}
