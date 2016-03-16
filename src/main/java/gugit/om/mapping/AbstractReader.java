package gugit.om.mapping;

import gugit.om.utils.ArrayIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/****
 * an abstract class for all (runtime-generated) entity readers. 
 * 
 * @author urbonman
 */
public abstract class AbstractReader implements IReader{

	/***
	 * readers that help read entities of other types, should we need it
	 * 
	 * maps [entity class -> reader]
	 */
	protected Map readers = new HashMap();

	/***
	 * cached states for each position (resultset column).
	 * we need to preserve a last-read entity from a previous run, so we could 
	 * 	optimize entity reading from a very next row.
	 * 
	 * maps [position (i.e. colIndex) -> state]
	 */
	protected ArrayList states = new ArrayList();
	
	/***
	 * init method
	 */
	public void setReaders(Map readers){
		this.readers = readers;
	}
	
	/***
	 * call this before reusing same reader instance for reading from a new recordset.
	 * (clears cached states from a previous run)
	 */
	public void reset(){
		for (int i =0 ; i< states.size(); i++)
			reset(i);
	}
	
	/***
	 * reads object graph from a row of Objects, and returns a (properly filled-in) Entity.
	 * 
	 * ReaderCompiler will create an actual implementation of this method.
	 */
	public abstract Object read(ArrayIterator row, int position, ReadContext readContext);

	
	
	//////
	// private impl down here
	
	protected static class State{
		public Object lastId = null;
		public Object lastEntity = null;
	}
	
	protected AbstractReader getReader(Class entityClass){
		return (AbstractReader)readers.get(entityClass);
	}

	protected State getState(int position){
		if (states.size()<= position)
			for (int i=states.size(); i<=position; i++)
				states.add(null);
		
		State state = (State) states.get(position);
		if (state == null){
			state = new AbstractReader.State();
			states.set(position,  state);
		}
		return state;
	}
	
	public void reset(int position){
		if (states.size()<=position)
			return;
		
		State state = (State) states.get(position);
		if (state == null)
			return;
		
		state.lastEntity = null;
		state.lastId = null;
	}	
}