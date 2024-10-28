package zingg.common.core.util;

import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;

import java.util.List;

public class ListConverter<C> {
    public Seq<String> convertListToSeq(List<C> inputList) {
        return (Seq<String>) JavaConverters.asScalaIteratorConverter(inputList.iterator())
                .asScala()
                .toSeq();
    }
}