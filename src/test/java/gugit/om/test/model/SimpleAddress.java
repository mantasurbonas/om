package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;

public class SimpleAddress{
	
	public static final String IGNORE_ME_FIELD = "hi"; 
	
	@ID(name="ID")
	private Integer id;
	
	@Column(name="\"COLUMN\"")
	private String country;
	
	@Column(name="CITY")
	private String city;
	
	@Column(name="STREET")
	private String street;
	
	@Column(name="PERSON")
	private String person;

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

