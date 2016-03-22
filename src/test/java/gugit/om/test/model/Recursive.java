package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.Pojo;
import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;
import gugit.om.annotations.MasterRef;

@Entity(name="RECURSIVE")
public class Recursive{
	@ID(name="ID")
	private Integer id;
	
	@Column(name="LABEL")
	private String label;
	
	@MasterRef(myColumn="PARENT_ID")
	private Recursive parent;
	
	@Pojo
	private Recursive child;

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
