package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;
import gugit.om.annotations.MasterRef;

public class Address {

	@ID(name="\"ID\"")
	private Integer id;
	
	@Column(name="\"COUNTRY\"")
	private String country;
	
	@Column(name="CITY")
	private String city;
	
	@Column(name="STREET")
	private String street;

	@MasterRef(myColumn="\"OWNER_ID\"")
	private Person owner;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}
	
	public String toString(){
		return "Address #"+id+": "+city+", "+street+", "+country;
	}

	public Person getOwner() {
		return owner;
	}

	public void setOwner(Person person) {
		this.owner = person;
	}
}
