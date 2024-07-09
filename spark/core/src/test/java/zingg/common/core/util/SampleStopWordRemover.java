package zingg.common.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.ZinggClientException;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SampleStopWordRemover {

    public static List<StopWordsRemover<SparkSession, Dataset<Row>, Row, Column, DataType>> getStopWordRemovers(ZinggSparkContext zsCTX,
                                                                                                                IArguments args) throws ZinggClientException {

        List<StopWordsRemover<SparkSession, Dataset<Row>, Row, Column, DataType>> sparkStopWordsRemovers = new ArrayList<>();

        //add first stopWordRemover
        List<FieldDefinition> fdList = new ArrayList<FieldDefinition>(4);
        ArrayList<MatchType> matchTypelistFuzzy = new ArrayList<MatchType>();
        matchTypelistFuzzy.add(MatchType.FUZZY);
        FieldDefinition eventFD = new FieldDefinition();
        eventFD.setDataType("string");
        eventFD.setFieldName("statement");
        eventFD.setMatchType(matchTypelistFuzzy);
        fdList.add(eventFD);
        IArguments stmtArgs = new Arguments();
        stmtArgs.setFieldDefinition(fdList);
        sparkStopWordsRemovers.add(new SparkStopWordsRemover(zsCTX,stmtArgs));

        //add second stopWordRemover
        String stopWordsFileName1 = Objects.requireNonNull(
                SampleStopWordRemover.class.getResource("../../../../preProcess/stopWords.csv")).getFile();
        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setStopWords(stopWordsFileName1);
        fieldDefinition1.setFieldName("field1");
        List<FieldDefinition> fieldDefinitionList1 = List.of(fieldDefinition1);
        args.setFieldDefinition(fieldDefinitionList1);
        sparkStopWordsRemovers.add(new SparkStopWordsRemover(zsCTX, args));

        //add third stopWordRemover
        String stopWordsFileName2 = Objects.requireNonNull(
                SampleStopWordRemover.class.getResource("../../../../preProcess/stopWordsWithoutHeader.csv")).getFile();
        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setStopWords(stopWordsFileName2);
        fieldDefinition2.setFieldName("field1");
        List<FieldDefinition> fieldDefinitionList2 = List.of(fieldDefinition2);
        args.setFieldDefinition(fieldDefinitionList2);
        sparkStopWordsRemovers.add(new SparkStopWordsRemover(zsCTX, args));

        return sparkStopWordsRemovers;
    }
}
