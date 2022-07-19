package zingg.domain.model.labels;

import zingg.domain.model.predictions.Prediction;
import zingg.domain.model.predictions.PredictionIterator;

public interface LabellerUserInterface {
    LabellingCommand getFeedback(Prediction prediction);

    void printProgress(PredictionIterator predictionIterator);

}
