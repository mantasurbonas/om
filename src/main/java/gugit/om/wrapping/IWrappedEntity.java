package gugit.om.wrapping;

public interface IWrappedEntity {
	void setDirty();
	void clearDirty();
	boolean isDirty();
}
