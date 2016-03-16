package gugit.om.metadata;

import gugit.om.WritePad;
import gugit.om.mapping.IWriter;

public class DependencyWriter implements IWriter {

	private WriteTimeDependency dependency;

	public DependencyWriter(WriteTimeDependency dependency) {
		this.dependency = dependency;
	}

	@Override
	public void write(Object value, WritePad<?> writePad) {
		writePad.addDependency(dependency);
	}

}
