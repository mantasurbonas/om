package gugit.om.test.model;

import gugit.om.annotations.Column;
import gugit.om.annotations.ID;

public class Address {

	@ID
	public Integer id;
	
	@Column(name="COUNTRY")
	public String country;
	
	@Column(name="CITY")
	public String city;
	
	@Column(name="STREET")
	public String street;

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
}
