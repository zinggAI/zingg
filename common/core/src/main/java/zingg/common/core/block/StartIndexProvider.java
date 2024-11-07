package zingg.common.core.block;

public class StartIndexProvider {
    public static int getStartIndex(FieldIteratorType fieldIteratorType) {
        return fieldIteratorType.equals(FieldIteratorType.DEFAULT) ? -1 : 0;
    }
}
