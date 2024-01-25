package zingg.common.py.processors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessorContext {
    private static final ProcessorContext INSTANCE = new ProcessorContext();

    private Map<String, List<String>> classMethodsMap = new HashMap<>();

    private ProcessorContext() {
    }

    public static ProcessorContext getInstance() {
        return INSTANCE;
    }

    public Map<String, List<String>> getClassMethodsMap() {
        return classMethodsMap;
    }
}
