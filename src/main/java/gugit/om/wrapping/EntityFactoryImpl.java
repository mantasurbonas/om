package gugit.om.wrapping;

public class EntityFactoryImpl implements IEntityFactory{

	private static WrappedEntityGenerator generator = new WrappedEntityGenerator();
		
	public <E> E create(Class<E> entityClass){
		try{
			Class<? extends E> wrappedEntityClass = generator.getWrappedEntityClass(entityClass);
			return wrappedEntityClass.newInstance();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
}
