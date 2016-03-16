package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;

public class SimpleAddress{
	@ID(name="ID")
	public Integer id;
	
	@Column(name="COLUMN")
	public String country;
	
	@Column(name="CITY")
	public String city;
	
	@Column(name="STREET")
	public String street;
	
	@Column(name="PERSON")
	public String person;

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

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}
	
}

