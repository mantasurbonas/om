package gugit.om.metadata;

import gugit.om.annotations.Column;
import gugit.om.annotations.DetailEntities;
import gugit.om.annotations.DetailEntity;
import gugit.om.annotations.ID;
import gugit.om.annotations.Ignore;
import gugit.om.annotations.ManyToMany;
import gugit.om.annotations.MasterEntity;
import gugit.om.annotations.Transient;

import java.lang.annotation.Annotation;

public class AnnotationHelper {

	private Annotation[] annotations;

	public AnnotationHelper(Annotation[] annotations){
		this.annotations = annotations;
	}
	
	public boolean isIgnored() {
		return containsClass(Ignore.class);
	}
	
	public boolean isID() {
		return containsClass(ID.class);
	}

	public boolean isColumn() {
		return containsClass(Column.class);
	}

	public boolean isDetailEntity() {
		return containsClass(DetailEntity.class);
	}
	
	public boolean isDetailEntities() {
		return containsClass(DetailEntities.class);
	}
	
	public boolean isMasterEntity() {
		return containsClass(MasterEntity.class);
	}

	public boolean isManyToMany() {
		return containsClass(ManyToMany.class);
	}
	
	public boolean isTransient() {
		return annotations.length == 0 || containsClass(Transient.class);
	}
			
	public String getColumnName() {
		Column colAnnotation = (Column)getByClass(Column.class);
		return colAnnotation.name();
	}

	public Class<?> getDetailEntitiesType() {
		DetailEntities annotation = (DetailEntities)getByClass(DetailEntities.class);
		return annotation.detailClass();
	}
	
	public String getMasterPropertyName() {
		MasterEntity annotation = (MasterEntity) getByClass(MasterEntity.class);
		return annotation.masterProperty();
	}
	
	public String getMasterMyColumnName(){
		MasterEntity annotation = (MasterEntity) getByClass(MasterEntity.class);
		return annotation.myColumn();
	}
	
	public Class<?> getManyToManyFieldType() {
		ManyToMany annotation = (ManyToMany)getByClass(ManyToMany.class);
		return annotation.othClass();
	}

	
	
	public boolean containsClass(Class<?> clazz) {
		return getByClass(clazz) != null;
	}

	private Annotation getByClass(Class<?> clazz) {
		for (Annotation a: annotations)
			if (clazz.isInstance(a))
				return a;
		return null;
	}

}
