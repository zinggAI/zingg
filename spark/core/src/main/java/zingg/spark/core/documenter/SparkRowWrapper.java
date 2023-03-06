package zingg.spark.core.documenter;

import org.apache.spark.sql.Row;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import zingg.common.core.documenter.RowWrapper;

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