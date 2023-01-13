package zingg.util;



import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.WrappingTemplateModel;

public abstract class RowAdapter<R> extends WrappingTemplateModel implements TemplateSequenceModel,
AdapterTemplateModel {

protected final R row;

public RowAdapter(R row, ObjectWrapper ow) {
     super(ow);  // coming from WrappingTemplateModel
     this.row = row;
}



@Override  // coming from AdapterTemplateModel
public Object getAdaptedObject(Class hint) {
     return row;
}

}