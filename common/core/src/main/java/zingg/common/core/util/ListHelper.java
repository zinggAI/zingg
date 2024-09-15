package zingg.common.core.util;

import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.List;

public class ListHelper {
    public static Seq<String> convertListToSeq(List<String> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator())
                .asScala()
                .toSeq();
    }
}
