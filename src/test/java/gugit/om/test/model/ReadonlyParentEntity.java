package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.Entity;
import gugit.om.annotations.ID;
import gugit.om.annotations.Pojo;

@Entity
public class ReadonlyParentEntity {

	@ID
	Integer id;
	
	@Column(name="label")
	String label;
	
	@Pojo
	ReadonlyEntity readonly;

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

	public ReadonlyEntity getReadonly() {
		return readonly;
	}

	public void setReadonly(ReadonlyEntity readonly) {
		this.readonly = readonly;
	}
	
	
}
