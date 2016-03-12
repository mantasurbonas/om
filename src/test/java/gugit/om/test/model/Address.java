package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;
import gugit.om.annotations.MasterEntity;

public class Address {

	@ID(name="ID")
	public Integer id;
	
	@Column(name="COUNTRY")
	public String country;
	
	@Column(name="CITY")
	public String city;
	
	@Column(name="STREET")
	public String street;

	@MasterEntity(masterProperty="id", myColumn="PERSON_ID")
	public Person person;
	
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

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
}
