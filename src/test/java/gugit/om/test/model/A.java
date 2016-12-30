package gugit.om.test.model;

import java.util.LinkedList;
import java.util.List;

import gugit.om.annotations.ID;
import gugit.om.annotations.ManyToMany;
import gugit.om.annotations.Transient;

public class A{
	@ID(name="ID")
	private Integer id;
	
	@Transient
	private String nevermindMe;
	
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
	
	public String getNevermindMe() {
		return nevermindMe;
	}
	
	public void setNevermindMe(String nevermindMe) {
		this.nevermindMe = nevermindMe;
	}
}