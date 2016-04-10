package gugit.om.test.model;

import java.util.LinkedList;
import java.util.List;

import gugit.om.annotations.ID;
import gugit.om.annotations.ManyToMany;

public class A{
	@ID(name="ID")
	private Integer id;
	
	@ManyToMany(detailClass=B.class, joinTable="A_TO_B", myColumn="A_ID", otherColumn="B_ID")
	private List<B> bees = new LinkedList<B>();
	
	public Integer getId(){
		return id;
	}
	public void setId(Integer i){
		this.id = i;
	}
	public List<B> getBees(){
		return bees;
	}
}