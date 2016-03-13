package gugit.om.metadata;

import gugit.om.mapping.Binding;
import gugit.om.mapping.IReader;
import gugit.om.mapping.IWriter;

public class FieldMetadata {

	// a name of a POJO field - just as specified by the developer
	private String name;
	
	// helper used to access value of a POJO field
	private Binding accessor;
	
	// a writer used to persist this field's value
	private IWriter writer;

	// a reader used to read value for this field
	private IReader reader;
	
	public FieldMetadata(final String name, Binding binding, IWriter writer, IReader reader){
		this.name = name;
		this.accessor = binding;
		this.writer = writer;
		this.reader = reader;				
	}
	
	public Binding getBinding() {
		return accessor;
	}

	public IWriter getWriter() {
		return writer;
	}
	
	public String getName(){
		return name;
	}

	public IReader getReader() {
		return reader;
	}

	public boolean isColumn(){
		return false; // to be overriden by subclasses
	}
}
