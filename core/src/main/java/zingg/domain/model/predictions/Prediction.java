package zingg.domain.model.predictions;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.LabelMatchType;

import java.util.Optional;

public class Prediction {

    public Prediction(Dataset<Row> pair) {
        this.pair = pair;
    }

    private final Dataset<Row> pair;

    public double getScore() {
        return pair.head().getAs(ColName.SCORE_COL);
    }


    public Optional<LabelMatchType> getMatchType(){
        double rawValue =  pair.head().getAs(ColName.PREDICTION_COL);
        if(rawValue== ColValues.IS_NOT_KNOWN_PREDICTION){
            return Optional.empty();
        }
        else{
            return Optional.of(
                    LabelMatchType.get(rawValue)
            );
        }
    }

    public Prediction withUpdatedMatch(LabelMatchType matchType){
        Dataset<Row> pair = this.pair.withColumn(ColName.MATCH_FLAG_COL, functions.lit(feedback.value));
    }



}
