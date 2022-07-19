package zingg.infrastructure.streams;

import org.apache.spark.sql.Column;
import zingg.client.util.ColValues;
import zingg.domain.model.labels.LabelFeedback;
import zingg.domain.model.labels.LabellerUserInterface;
import zingg.domain.model.labels.LabellingCommand;
import zingg.domain.model.predictions.Prediction;
import zingg.domain.model.predictions.PredictionIterator;
import zingg.util.LabelMatchType;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class StreamLabellerUserInterface implements LabellerUserInterface {

    private final InputStream inputStream;
    private final PrintStream outputStream;

    private final List<Column> displayColumns;


    public StreamLabellerUserInterface(InputStream inputStream, PrintStream outputStream, List<Column> displayColumns) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.displayColumns = displayColumns;
    }

    public static StreamLabellerUserInterface fromConsole(List<Column> displayColumns){
        return new StreamLabellerUserInterface(System.in, System.out, displayColumns);
    }

    @Override
    public void printProgress(PredictionIterator iterator) {
        String template = "\tCurrent labelling round  : %d/%d pairs labelled\n"
        String message = String.format(template, iterator.currentIterationIndex(), iterator.totalIterations());
        outputStream.println(message);
    }

    private void printCommandInstructions(){
        outputStream.println();
        outputStream.println("\tNo, they do not match : 0");
        outputStream.println("\tYes, they match       : 1");
        outputStream.println("\tNot sure              : 2");
        outputStream.println();
        outputStream.println("\tTo exit               : 9");
        outputStream.println();
        outputStream.print("\tPlease enter your choice [0,1,2 or 9]: ");
    }

    @Override
    public LabellingCommand readCommand(Prediction prediction) {
        Scanner sc = new Scanner(System.in);
        printCommandInstructions();
        Optional<LabellingCommand> labellingCommand = Optional.empty();
        while(!labellingCommand.isPresent()){
            String word = sc.next();
            labellingCommand = parseCommand(word);
            if(!labellingCommand.isPresent()) {
                System.out.println("Nope, please enter one of the allowed options!");
            }
        }
        return labellingCommand.get();
    }

    private static Optional<LabellingCommand> parseCommand(String word){
        if(word.equalsIgnoreCase("9")){
            return Optional.of(LabellingCommand.ABORT);
        }
        else{
            try {
                int value = Integer.parseInt(word);
                return LabelFeedback.fromValue(value).map(
                    labelFeedback -> LabellingCommand.update(labelFeedback)
                );
            }
            catch(NumberFormatException numberFormatException){
                return Optional.empty();
            }

        }
    }

    public String getMsg1(int index, int totalPairs) {
        return String.format("\tCurrent labelling round  : %d/%d pairs labelled\n", index, totalPairs);
    }

    public String getMsg2(double prediction, double score) {
        String msg2 = "";
        String matchType = LabelMatchType.get(prediction).msg;
        if (prediction == ColValues.IS_NOT_KNOWN_PREDICTION) {
            msg2 = String.format(
                    "\tZingg does not do any prediction for the above pairs as Zingg is still collecting training data to build the preliminary models.");
        } else {
            msg2 = String.format("\tZingg predicts the above records %s with a similarity score of %.2f",
                    matchType, Math.floor(score * 100) * 0.01);
        }
        return msg2;
    }

}
