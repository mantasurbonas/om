package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;

public class Duck {

	@ID(name="ID")
	private Integer id;
	
	@Column(name="NAME")
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
