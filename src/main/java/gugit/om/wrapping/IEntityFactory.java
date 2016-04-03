package gugit.om.wrapping;

public interface IEntityFactory {
	
	<E> E create(Class<E> entityClass);
	
}
