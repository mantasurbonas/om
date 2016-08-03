package gugit.om.utils;

import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringTemplate {

	private String template;

	private String result;
	
	private static final Logger logger = LogManager.getLogger();
	
	public StringTemplate(final String template){
		this.template = template;
		this.result = template;
	}
	
	public StringTemplate reset(){
		result = template;
		return this;
	}
	
	public StringTemplate replace(final String key, final String replacement){
		result = replaceAll(result, "%"+key+"%", replacement);
		return this;
	}
	
	public StringTemplate removeUnusedKeys(){
		// TODO: remove all unused keys
		return this;
	}
	
	public String getResult(){
		return result;
	}
	
	private static String replaceAll(final String txt, final String key, final String replacement){
		return txt.replaceAll(Matcher.quoteReplacement(key), Matcher.quoteReplacement(replacement));
	}
	
	public StringTemplate dump(boolean shouldDump){
		if (shouldDump)
			logger.trace(result);
		return this;
	}
}
