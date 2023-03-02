package zingg.spark.core.util;

import freemarker.template.Version;
import zingg.util.RowWrapper;

import org.apache.spark.sql.Row;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class SparkRowWrapper extends RowWrapper<Row> {

    public SparkRowWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if (obj instanceof Row) {
            return  new SparkRowAdapter((Row) obj, this);
        }

        return super.handleUnknownType(obj);
    }

    

}