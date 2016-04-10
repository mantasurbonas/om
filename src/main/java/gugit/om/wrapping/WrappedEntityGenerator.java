package gugit.om.wrapping;

import java.util.HashMap;
import java.util.Map;

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

	private static Map<Class<?>, Class<?>> cache = new HashMap<Class<?>, Class<?>>();
	
	private ClassPool pool;
	
	public WrappedEntityGenerator(){
		this.pool = ClassPool.getDefault();
		this.pool.importPackage("gugit");
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<T> unwrap(Class<? extends T> wrappedClass){
		wrappedClass = (Class<? extends T>) wrappedClass.getSuperclass();
		return (Class<T>)wrappedClass;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<? extends T> getWrappedEntityClass(Class<? extends T> entityClass)throws Exception{
		if (entityClass.isInstance(IWrappedEntity.class))
			entityClass = unwrap(entityClass);
		
		if (doesWrapperClassExist(entityClass))
			return getExistingWrapperClass(entityClass);
		
		if (!cache.containsKey(entityClass))
			cache.put(entityClass, generateWrappedClass(entityClass));
		
		return (Class<? extends T>) cache.get(entityClass);
	}

	public boolean doesWrapperClassExist(Class<?> entityClass) {
		return pool.getOrNull( getGeneratedClassName(entityClass.getCanonicalName())) != null;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<? extends T> getExistingWrapperClass(Class<T> entityClass) throws Exception {
		String wrappedClassName = getGeneratedClassName(entityClass.getCanonicalName());
		try{
			return (Class<? extends T>) Class.forName(wrappedClassName);
		}catch(Exception e){
			System.out.println("failed finding a class "+entityClass+" in classloader, getting it from class pool");
			return (Class<? extends T>) pool.get( wrappedClassName).toClass(getClass().getClassLoader(), null);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Class<? extends T> generateWrappedClass(Class<T> entityClass) throws Exception {
		String generatedClassName = getGeneratedClassName(entityClass.getCanonicalName());
		
		CtClass resultClass = pool.getOrNull(generatedClassName);
		if (resultClass != null)
			return (Class<T>)Class.forName(generatedClassName);

		System.out.println("Generating wrapper for entity class "+entityClass);
		
		CtClass superClass = pool.getOrNull(entityClass.getCanonicalName());
		if (superClass == null)
			throw new RuntimeException("Entity class "+entityClass.getCanonicalName()+" could not be found.");
		
		resultClass = pool.makeClass(generatedClassName, superClass);
		resultClass.addInterface( pool.get(WRAPPED_ENTITY_INTERFACE_CLASS) );

		resultClass.addField(CtField.make(DIRTY_FLAG_FIELD_SRC, resultClass));
		resultClass.addMethod(CtNewMethod.make(SET_DIRTY_FLAG_METHOD_SRC, resultClass));
		resultClass.addMethod(CtNewMethod.make(CLEAR_DIRTY_METHOD_SRC, resultClass));
		resultClass.addMethod(CtNewMethod.make(IS_DIRTY_METHOD_SRC, resultClass));

		CtMethod[] methods = superClass.getMethods();
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
						+"         System.out.println(\" invocation of "+methodName+" caused dirty flag\"); \n"
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
