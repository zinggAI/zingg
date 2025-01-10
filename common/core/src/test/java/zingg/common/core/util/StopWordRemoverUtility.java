package zingg.common.core.util;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IMatchType;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;
import zingg.common.core.preprocess.stopwords.StopWordsRemover;

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
        ArrayList<IMatchType> matchTypelistFuzzy = new ArrayList<IMatchType>();
        matchTypelistFuzzy.add(MatchTypes.FUZZY);
        FieldDefinition eventFD = new FieldDefinition();
        eventFD.setDataType("string");
        eventFD.setFieldName("statement");
        eventFD.setMatchType(matchTypelistFuzzy);
        addStopWordRemover(eventFD);

        //add second stopWordRemover
        String stopWordsFileName1 = Objects.requireNonNull(
        StopWordRemoverUtility.class.getResource("../../../../preProcess/stopwords/stopWords.csv")).getFile();
        FieldDefinition fieldDefinition1 = new FieldDefinition();
        fieldDefinition1.setStopWords(stopWordsFileName1);
        fieldDefinition1.setFieldName("field1");
        addStopWordRemover(fieldDefinition1);

        //add third stopWordRemover
        String stopWordsFileName2 = Objects.requireNonNull(
        StopWordRemoverUtility.class.getResource("../../../../preProcess/stopwords/stopWordsWithoutHeader.csv")).getFile();
        FieldDefinition fieldDefinition2 = new FieldDefinition();
        fieldDefinition2.setStopWords(stopWordsFileName2);
        fieldDefinition2.setFieldName("field1");
        addStopWordRemover(fieldDefinition2);

        //add fourth stopWordRemover
        String stopWordsFileName3 = Objects.requireNonNull(
        StopWordRemoverUtility.class.getResource("../../../../preProcess/stopwords/stopWordsMultipleCols.csv")).getFile();
        FieldDefinition fieldDefinition3 = new FieldDefinition();
        fieldDefinition3.setStopWords(stopWordsFileName3);
        fieldDefinition3.setFieldName("field1");
        addStopWordRemover(fieldDefinition3);
    }

    public List<StopWordsRemover<S, D, R, C, T>> getStopWordsRemovers() {
        return this.stopWordsRemovers;
    }

    public abstract void addStopWordRemover(FieldDefinition fd);
}
