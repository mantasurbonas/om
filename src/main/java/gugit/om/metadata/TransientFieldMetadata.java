package gugit.om.metadata;

import gugit.om.mapping.NoBinding;
import gugit.om.mapping.NoReader;
import gugit.om.mapping.NoWriter;

public class TransientFieldMetadata extends FieldMetadata {

	private static TransientFieldMetadata instance = new TransientFieldMetadata();
	
	private TransientFieldMetadata() {
		super("<transient>", 
				NoBinding.getInstance(), 
				NoWriter.getInstance(), 
				NoReader.getInstance());
	}
	
	public static TransientFieldMetadata getInstance(){
		return instance;
	}

}
