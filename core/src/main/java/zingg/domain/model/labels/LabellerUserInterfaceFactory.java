package zingg.domain.model.labels;

import org.apache.spark.sql.Column;

import java.util.List;

public interface LabellerUserInterfaceFactory {

    LabellerUserInterface newInterface(List<Column> displayColumn);
}
