package gugit.om.mapping;

import java.util.LinkedList;

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
	
	private LinkedList<StackEntry> stack = new LinkedList<StackEntry>();
	private LinkedList<StackEntry> recycle = new LinkedList<StackEntry>();
	
	public void entityIsBeingRead(Object entity, Object id){
		StackEntry entry = recycle.isEmpty()?new StackEntry(entity, id):recycle.removeLast().assign(entity, id);
		stack.add(entry);
	}
	
	public void entityReadingFinished(){
		recycle.addLast(stack.removeLast());
	}
	
	public Object findEntity(Class<?> type, Object id){
		for (StackEntry e:stack){		
			if (type.isInstance(e.entity))
				if (e.id.equals(id))
					return e.entity;
		}
		return null;
	}
}
