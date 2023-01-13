package zingg.util;

import freemarker.template.Version;

import org.apache.spark.sql.Row;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public abstract class RowWrapper<R> extends DefaultObjectWrapper {

    public RowWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    

}