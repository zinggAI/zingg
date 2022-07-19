package zingg.domain.model.labels;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum LabelFeedback {
    NO_MATCH(0),
    MATCH(1),
    NOT_SURE(2);

    public final int value;

    LabelFeedback(final int newValue) {
        value = newValue;
    }

    private static final Map<Integer, LabelFeedback> REVERSE_MAP;

    static {
        Map<Integer,LabelFeedback> map = new HashMap<>();
        for(LabelFeedback feedback: LabelFeedback.values()){
            map.put(feedback.value, feedback);
        }
        REVERSE_MAP = Collections.unmodifiableMap(map);
    }

    public static Optional<LabelFeedback> fromValue(int value){
        if(REVERSE_MAP.containsKey(value)){
            return Optional.of(REVERSE_MAP.get(value));
        }
        else {
            return Optional.empty();
        }
    }

}
