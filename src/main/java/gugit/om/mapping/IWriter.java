package gugit.om.mapping;


/***
 * serializes an entity to the specified WriteBatch.
 * 
 * stateless and thus thread-safe
 * 	(provided params are thread-specific, of course)
 *  
 * @author urbonman 
 */
public interface IWriter <E>{

	IPropertyAccessor<E, ?> getIdAccessor();
	
	void write(E entity, WriteBatch batch, WriteContext writeContext);
	
}
