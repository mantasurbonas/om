package gugit.om.utils;

public class StringUtils {

	public static String capitalize(String name) {
		return name.substring(0,1).toUpperCase()+name.substring(1);
	}

	public static String escape(final String str){
		return str.replace("\"", "\\\"");//.replace("\\", "\\\\");
	}
}
