package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.DetailEntity;
import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;
import gugit.om.annotations.MasterEntity;

@Entity(name="RECURSIVE")
public class Recursive{
	@ID(name="ID")
	public Integer id;
	
	@Column(name="LABEL")
	public String label;
	
	@MasterEntity(masterProperty="id", myColumn="PARENT_ID")
	public Recursive parent;
	
	@DetailEntity(myProperty="id", detailColumn="PARENT_ID")
	public Recursive child;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Recursive getChild() {
		return child;
	}

	public void setChild(Recursive child) {
		this.child = child;
	}
	
	public Recursive getParent(){
		return parent;
	}
	
	public void setParent(Recursive parent){
		this.parent = parent;
	}
	
	public String toString(){
		return "Recursive #"+getId()
					+" '"+getLabel()+"' child is "
					+getChild();
	}
}
