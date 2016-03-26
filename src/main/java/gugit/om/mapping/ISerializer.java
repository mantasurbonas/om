package gugit.om.mapping;


/***
 * Facade for both Reader, Merger and Writer
 * 
 * @author urbonman
 */
public interface ISerializer <E> extends IReader<E>, IMerger<E>, IWriter<E>{
		
}
