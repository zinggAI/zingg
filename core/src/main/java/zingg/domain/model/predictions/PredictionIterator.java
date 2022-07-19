package zingg.domain.model.predictions;

import java.util.Iterator;

public interface PredictionIterator extends Iterator<Prediction> {
    public int currentIterationIndex();
    public int totalIterations();
}