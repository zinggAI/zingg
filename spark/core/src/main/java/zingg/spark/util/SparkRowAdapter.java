package zingg.spark.util;

import org.apache.spark.sql.Row;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;
import freemarker.template.WrappingTemplateModel;
import zingg.util.RowAdapter;

public class SparkRowAdapter extends RowAdapter<Row>{

     public SparkRowAdapter(Row row, ObjectWrapper ow) {
          super(row, ow); 
     }

     @Override  // coming from TemplateSequenceModel
     public int size() throws TemplateModelException {
          return row.size();
     }
     
     @Override  // coming from TemplateSequenceModel
     public TemplateModel get(int index) throws TemplateModelException {
          return wrap(row.get(index));
     }

}