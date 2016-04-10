package gugit.om.mapping;

public interface IWritePacket {
	
	boolean trySolveDependencies();
	
	String getEntityName();
}
