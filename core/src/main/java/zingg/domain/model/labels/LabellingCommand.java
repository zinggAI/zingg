package zingg.domain.model.labels;

public abstract class LabellingCommand {

    abstract boolean isAbort();

    static class Abort extends LabellingCommand{
        @Override
        boolean isAbort() {
            return true;
        }
    }

    static class UpdateLabels extends LabellingCommand{
        public final LabelFeedback labelFeedback;

        public UpdateLabels(LabelFeedback labelFeedback) {
            this.labelFeedback = labelFeedback;
        }

        @Override
        boolean isAbort() {
            return false;
        }
    }

    public static final LabellingCommand ABORT = new Abort();

    public static UpdateLabels update(LabelFeedback labelFeedback){
        return new UpdateLabels(labelFeedback);
    }
}
