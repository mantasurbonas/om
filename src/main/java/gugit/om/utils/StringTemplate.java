package gugit.om.utils;

public class StringTemplate {

	private String template;

	private String result;
	
	public StringTemplate(final String template){
		this.template = template;
		this.result = template;
	}
	
	public StringTemplate reset(){
		result = template;
		return this;
	}
	
	public StringTemplate replace(final String key, final String replacement){
		result = result.replaceAll("%"+key+"%", replacement);
		return this;
	}
	
	public StringTemplate removeUnusedKeys(){
		// TODO: remove all unused keys
		return this;
	}
	
	public String getResult(){
		return result;
	}
}
