package zingg.util;

import freemarker.template.Version;
import freemarker.template.DefaultObjectWrapper;

public abstract class RowWrapper<R> extends DefaultObjectWrapper {

    public RowWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    

}