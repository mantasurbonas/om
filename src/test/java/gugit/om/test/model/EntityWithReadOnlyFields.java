package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;
import gugit.om.annotations.Pojo;

public class EntityWithReadOnlyFields {

	@ID(name="ID")
	private Integer id;
	
	@Column(name="NAME")
	private String name;
	
	@Column(name="PCODE", readOnly=true)
	private String pcode;
	
	@Pojo(myColumn="B1_ID")
	private B b1;
	
	@Pojo(myColumn="B2_ID", readOnly=true)
	private B b2;

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

	public String getPcode() {
		return pcode;
	}

	public void setPcode(String pcode) {
		this.pcode = pcode;
	}

	public B getB1() {
		return b1;
	}

	public void setB1(B b1) {
		this.b1 = b1;
	}

	public B getB2() {
		return b2;
	}

	public void setB2(B b2) {
		this.b2 = b2;
	}
	
	
}
