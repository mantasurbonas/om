package gugit.om.test.model;

import gugit.om.annotations.ID;

public class B{
	
	@ID(name="ID")
	private Integer id;
	
	public Integer getId(){
		return id;
	}
	
	public void setId(Integer i){
		this.id = i;
	}
	
	public void touch() {
		; // a no-op, potentially state mutating method
	}
}
