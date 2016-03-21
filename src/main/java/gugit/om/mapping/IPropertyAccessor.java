package gugit.om.mapping;

/***
 * A read-write binding to some specific property of an entity.
 * 
 * @author urbonman
 *
 */
public interface IPropertyAccessor<E, V> {

	void setValue(E entity, V value);
	
	V getValue(E entity);
}
