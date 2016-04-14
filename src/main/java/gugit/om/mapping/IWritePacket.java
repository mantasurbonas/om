package gugit.om.mapping;

import java.util.List;

public interface IWritePacket {
	
	String getEntityName();
	
	boolean trySolveDependencies();
	
	List<WritePacketElement> getElements();
	
	WritePacketElement getByFieldName(final String fieldName);
}
