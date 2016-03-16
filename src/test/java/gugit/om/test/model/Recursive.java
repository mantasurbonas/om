package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.DetailEntity;
import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;

@Entity(name="RECURSIVE")
public class Recursive{
	@ID(name="ID")
	public Integer id;
	
	@Column(name="LABEL")
	public String label;
	
	@DetailEntity(myProperty="id", detailColumn="PARENT_ID")
	public Recursive recursive;

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

	public Recursive getRecursive() {
		return recursive;
	}

	public void setRecursive(Recursive recursive) {
		this.recursive = recursive;
	}
	
	public String toString(){
		return "Recursive #"+getId()
					+" '"+getLabel()+"' "
					+getRecursive();
	}
}
