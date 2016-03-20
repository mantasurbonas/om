package gugit.om.utils;

import java.util.ArrayList;

public class FastStack{

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
	
	private ArrayList<StackEntry> array = new ArrayList<StackEntry>(50);
	private int size = 0;

	public void push(Object entity, Object id){
		size ++;
		if (size > array.size())
			array.add(new StackEntry(entity, id));
		else
			array.get(size-1).assign(entity, id);
	}
	
	public Object pop(){
		return array.get(--size).entity;
	}
	
	public Object find(Class<?> type, Object id){
		for (int i=0; i<size; i++){
			StackEntry e = array.get(i);
			if (type.isInstance(e.entity))
				if (e.id.equals(id))
					return e.entity;
		}
		return null;
	}

	public void clear() {
		size = 0;
	}
}
