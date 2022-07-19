package zingg.infrastructure.streams;

import zingg.domain.model.stats.LabellerStat;
import zingg.domain.model.stats.StatsEmitter;

import java.io.PrintStream;

public class StreamStatsEmitter implements StatsEmitter {

    private final PrintStream printStream;
    private final int emptyLinesBeforeStatsCount;

    public StreamStatsEmitter(PrintStream printStream,int emptyLinesBeforeStatsCount) {
        this.printStream = printStream;
        this.emptyLinesBeforeStatsCount = emptyLinesBeforeStatsCount;
    }

    @Override
    public void emitStats(LabellerStat labellerStat) {
        String msg = String.format(
                "\tLabelled pairs so far    : %d/%d MATCH, %d/%d DO NOT MATCH, %d/%d NOT SURE",
                labellerStat.getPositivePairsCount(), labellerStat.getTotalCount(),
                labellerStat.getNegativePairsCount(), labellerStat.getTotalCount(),
                labellerStat.getNotSurePairsCount(), labellerStat.getTotalCount(),
        );
        for(int i=0;i<emptyLinesBeforeStatsCount;i++){
            printStream.println();
        }
        printStream.println(msg);

    }
}
