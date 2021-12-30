package zingg.util;

import org.apache.spark.sql.Row;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.WrappingTemplateModel;

public class RowAdapter extends WrappingTemplateModel implements TemplateSequenceModel,
AdapterTemplateModel {

private final Row row;

public RowAdapter(Row row, ObjectWrapper ow) {
super(ow);  // coming from WrappingTemplateModel
this.row = row;
}

@Override  // coming from TemplateSequenceModel
public int size() throws TemplateModelException {
return row.size();
}

@Override  // coming from TemplateSequenceModel
public TemplateModel get(int index) throws TemplateModelException {
     return wrap(row.get(index));
}

@Override  // coming from AdapterTemplateModel
public Object getAdaptedObject(Class hint) {
return row;
}

}