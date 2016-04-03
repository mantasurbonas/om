package gugit.om.wrapping;

public class EntityMarkingHelper {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Class<T> getEntityClass(Class<T> wrappedClass){
		Class ret = wrappedClass;
		while (IWrappedEntity.class.isAssignableFrom(ret))
			ret = ret.getSuperclass();
		return ret;
	}
	
	public static void setDirty(Object e, boolean b){
		if (!(e instanceof IWrappedEntity))
			return;
		
		if (b)
			((IWrappedEntity)e).setDirty();
		else
			((IWrappedEntity)e).clearDirty();
	}
	
	public static boolean isDirty(Object e){
		if (!(e instanceof IWrappedEntity))
			return true;
		
		return ((IWrappedEntity)e).isDirty(); 
	}

}
