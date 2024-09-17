package zingg.common.core.util;

import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.List;

public class ListConverter<C> {
    public Seq<C> convertListToSeq(List<C> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator())
                .asScala()
                .toSeq();
    }
}
