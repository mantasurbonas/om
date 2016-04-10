package gugit.om.metadata;

import gugit.om.annotations.Column;
import gugit.om.annotations.Pojos;
import gugit.om.annotations.Pojo;
import gugit.om.annotations.ID;
import gugit.om.annotations.Ignore;
import gugit.om.annotations.ManyToMany;
import gugit.om.annotations.MasterRef;
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
		return containsClass(Pojo.class);
	}
	
	public boolean isDetailEntities() {
		return containsClass(Pojos.class);
	}
	
	public boolean isManyToMany() {
		return containsClass(ManyToMany.class);
	}
	
	public boolean isMasterEntity() {
		return containsClass(MasterRef.class);
	}
	
	public boolean isTransient() {
		return annotations.length == 0 || containsClass(Transient.class);
	}
			
	public String getColumnName() {
		Column colAnnotation = (Column)getByClass(Column.class);
		return colAnnotation.name();
	}

	public String getDetailMyColumnName(){
		Pojo annotation = (Pojo) getByClass(Pojo.class);
		return annotation.myColumn().isEmpty()?null:annotation.myColumn();
	}
	
	public Class<?> getDetailEntitiesType() {
		Pojos annotation = (Pojos)getByClass(Pojos.class);
		return annotation.detailClass();
	}
	
	public Class<?> getManyToManyType() {
		ManyToMany annotation = (ManyToMany)getByClass(ManyToMany.class);
		return annotation.detailClass();
	}

	public String getManyToManyMyColumn() {
		ManyToMany annotation = (ManyToMany)getByClass(ManyToMany.class);
		return annotation.myColumn();
	}

	public String getManyToManyOthColumn() {
		ManyToMany annotation = (ManyToMany)getByClass(ManyToMany.class);
		return annotation.otherColumn();
	}

	public String getManyToManyTableName() {
		ManyToMany annotation = (ManyToMany)getByClass(ManyToMany.class);
		return annotation.joinTable();
	}
	
	public String getMasterMyColumnName(){
		MasterRef annotation = (MasterRef) getByClass(MasterRef.class);
		return annotation.myColumn();
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
