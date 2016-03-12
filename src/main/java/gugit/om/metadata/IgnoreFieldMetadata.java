package gugit.om.metadata;

import gugit.om.mapping.NoBinding;
import gugit.om.mapping.NoWriter;
import gugit.om.mapping.SkipReader;


public class IgnoreFieldMetadata extends FieldMetadata {

	private static IgnoreFieldMetadata instance = new IgnoreFieldMetadata();
	
	private IgnoreFieldMetadata() {
		super("<ignored>", 
				NoBinding.getInstance(), 
				NoWriter.getInstance(), 
				SkipReader.getInstance());
	}
	
	public static IgnoreFieldMetadata getInstance(){
		return instance;
	}

}
