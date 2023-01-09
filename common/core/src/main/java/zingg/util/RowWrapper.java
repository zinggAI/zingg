package zingg.util;

import freemarker.template.Version;

import org.apache.spark.sql.Row;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class RowWrapper extends DefaultObjectWrapper {

    public RowWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
        if (obj instanceof Row) {
            return  new RowAdapter((Row) obj, this);
        }

        return super.handleUnknownType(obj);
    }

}