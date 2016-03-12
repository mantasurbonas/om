package gugit.om.mapping;

public class NullWriteValue {

	private static NullWriteValue instance = new NullWriteValue();
	
	private NullWriteValue(){}
	
	public static NullWriteValue getInstance(){
		return instance;
	}
	
	public String toString(){
		return "null";
	}
}
