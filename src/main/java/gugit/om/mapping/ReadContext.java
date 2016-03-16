package gugit.om.mapping;

import java.util.ArrayList;

public class ReadContext {
	
	private static class StackEntry{
		public Object entity;
		public Object id;
		public StackEntry(Object entity, Object id){
			this.entity = entity;
			this.id = id;
		}
		public StackEntry assign(Object entity, Object id){
			this.entity = entity;
			this.id = id;
			return this;
		}
	};
	
	private ArrayList<StackEntry> stack = new ArrayList<StackEntry>(50);
	private int stackSize = 0;
	
	public void entityIsBeingRead(Object entity, Object id){
		stackSize ++;
		if (stackSize > stack.size())
			stack.add(new StackEntry(entity, id));
		else
			stack.get(stackSize-1).assign(entity, id);
	}
	
	public void entityReadingFinished(){
		stackSize --;
	}
	
	public Object findEntity(Class<?> type, Object id){
		for (int i=0; i<stackSize; i++){
			StackEntry e = stack.get(i);
			if (type.isInstance(e.entity))
				if (e.id.equals(id))
					return e.entity;
		}
		return null;
	}
	
	public void clear(){
		stackSize = 0;
	}
}
