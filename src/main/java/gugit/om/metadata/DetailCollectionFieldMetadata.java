package gugit.om.metadata;

public class DetailCollectionFieldMetadata extends FieldMetadata{

	private Class<?> detailType;
	
	public DetailCollectionFieldMetadata(String name, Class<?> detailType, int columnOffset) {
		super(name, "-=ignore=-", columnOffset);
		this.detailType = detailType;
	}

	public Class<?> getDetailType(){
		return detailType;
	}
}
