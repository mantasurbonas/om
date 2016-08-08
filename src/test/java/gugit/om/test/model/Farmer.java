package gugit.om.test.model;

import java.util.ArrayList;
import java.util.List;

import gugit.om.annotations.ID;
import gugit.om.annotations.Pojos;

public class Farmer {

	@ID(name="ID")
	private Integer id;
	
	@Pojos(detailClass=Cow.class)
	private List<Cow> cows = new ArrayList<>();
	
	@Pojos(detailClass=Pig.class)
	private List<Pig> pigs = new ArrayList<>();
	
	@Pojos(detailClass=Duck.class)
	private List<Duck> ducks = new ArrayList<>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Cow> getCows() {
		return cows;
	}

	public void setCows(List<Cow> cows) {
		this.cows = cows;
	}

	public List<Pig> getPigs() {
		return pigs;
	}

	public void setPigs(List<Pig> pigs) {
		this.pigs = pigs;
	}

	public List<Duck> getDucks() {
		return ducks;
	}

	public void setDucks(List<Duck> ducks) {
		this.ducks = ducks;
	}
	
}
