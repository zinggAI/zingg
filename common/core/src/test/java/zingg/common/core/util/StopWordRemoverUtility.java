package zingg.common.core.util;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;
import zingg.common.core.preprocess.StopWordsRemover;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class StopWordRemoverUtility<S, D, R, C, T> {

    protected final List<StopWordsRemover<S, D, R, C, T>> stopWordsRemovers;

    public StopWordRemoverUtility() throws ZinggClientException {
        this.stopWordsRemovers = new ArrayList<StopWordsRemover<S, D, R, C, T>>();;
    }

    public void buildStopWordRemovers() throws ZinggClientException {

        //add first stopWordRemover
        List<FieldDefinition> fdList = new ArrayList<FieldDefinition>(4);
        ArrayList<MatchType> matchTypelistFuzzy = new ArrayList<MatchType>();
        matchTypelistFuzzy.add((MatchType) MatchTypes.FUZZY);
        FieldDefinition eventFD = new FieldDefinition();
        eventFD.setDataType("string");
        eventFD.setFieldName("statement");
        eventFD.setMatchType(matchTypelistFuzzy);
        fdList.add(eventFD);
        IArguments stmtArgs = new Arguments();
        stmtArgs.setFieldDefinition(fdList);
        addStopWordRemover(stmtArgs);

        //add second stopWordRemover
        String stopWordsFileName1 = Objects.requireNonNull(
                StopWordRemoverUtility.class.getResource("../../../../preProcess/stopWords.csv")).getFile();
        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setStopWords(stopWordsFileName1);
        fieldDefinition1.setFieldName("field1");
        List<FieldDefinition> fieldDefinitionList1 = List.of(fieldDefinition1);
        stmtArgs = new Arguments();
        stmtArgs.setFieldDefinition(fieldDefinitionList1);
        addStopWordRemover(stmtArgs);

        //add third stopWordRemover
        String stopWordsFileName2 = Objects.requireNonNull(
                StopWordRemoverUtility.class.getResource("../../../../preProcess/stopWordsWithoutHeader.csv")).getFile();
        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setStopWords(stopWordsFileName2);
        fieldDefinition2.setFieldName("field1");
        List<FieldDefinition> fieldDefinitionList2 = List.of(fieldDefinition2);
        stmtArgs = new Arguments();
        stmtArgs.setFieldDefinition(fieldDefinitionList2);
        addStopWordRemover(stmtArgs);
    }

    public List<StopWordsRemover<S, D, R, C, T>> getStopWordsRemovers() {
        return this.stopWordsRemovers;
    }

    public abstract void addStopWordRemover(IArguments iArguments);
}
