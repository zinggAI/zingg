package zingg.common.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.model.data.ModelDFData;
import zingg.common.core.model.model.ModelDF;
import zingg.common.core.util.ModelUtil;

public abstract class TestModelBase<S,D,R,C,T> {

    protected Context<S, D, R, C, T> context;
    protected DFObjectUtil<S, D, R, C> dfObjectUtil;

    public TestModelBase(){
    }

    public void initialize(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) {
        this.dfObjectUtil = dfObjectUtil;
        this.context = context;
    }

    public abstract ModelUtil<S,T, D,R,C> getModelUtil();

    @Test
    public void testPredict() throws ZinggClientException, Exception {  
        ModelUtil<S,T, D,R,C> modelUtil = getModelUtil();
        Model<S,T, D,R,C> model = modelUtil.getModel(false, getArgs()); 
        model.register();

        ZFrame<D, R, C> posDF = dfObjectUtil.getDFFromObjectList(ModelDFData.getPosDF(), ModelDF.class);
        ZFrame<D, R, C> negDF = dfObjectUtil.getDFFromObjectList(ModelDFData.getNegDF(), ModelDF.class);
        ZFrame<D, R, C> unseenDFForPrediction = dfObjectUtil.getDFFromObjectList(ModelDFData.getUnseenRowsForPredictDF(), ModelDF.class);
        
        model.fit(posDF, negDF);
        
        // combine the two DFs
        ZFrame<D, R, C>  testDF = getPredictDF(new ZFrame[] {posDF, negDF,unseenDFForPrediction});

        // call predict
        ZFrame<D ,R, C>  testDFPredicted = model.predict(testDF);
        
        ZFrame<D ,R, C> assertionDF = testDFPredicted.select(new String[]{"z_zid","z_prediction","z_score"});
        List<R>  assertionRows = assertionDF.collectAsList();

        
        for (R row : assertionRows) {
            //System.out.println("row is " + row);
            if(assertionDF.getAsInt(row, "z_zid")<10000) {
                assertEquals(1,assertionDF.getAsDouble(row, "z_prediction"));
            } else {
                assertEquals(0,assertionDF.getAsDouble(row, "z_prediction"));
            }
            
        }
        
    }

    private ZFrame<D,R,C> getPredictDF(ZFrame<D,R,C>[] dfToCombine) {
        
        ZFrame<D ,R, C> combinedDF = null;
        if (dfToCombine != null && dfToCombine.length > 0) {
            for (int i = 0; i < dfToCombine.length; i++) {
                if(combinedDF==null) {
                    combinedDF = dfToCombine[i];
                } else if (dfToCombine[i] != null) {
                    combinedDF = combinedDF.union(dfToCombine[i]);
                }
            }
        } else {
            return null;
        }
        
        ZFrame<D ,R, C> predictDF = combinedDF.drop(ColName.MATCH_FLAG_COL).drop(ColName.COL_PREFIX+ColName.MATCH_FLAG_COL);
        return predictDF;
    }

    protected Arguments getArgs() throws ZinggClientException {
        
        Arguments args = new Arguments();
        args.setModelId("junit");
        
        List<FieldDefinition> fdList = new ArrayList<FieldDefinition>(4);

        ArrayList<MatchType> matchTypelistId = new ArrayList<MatchType>();
        matchTypelistId.add(MatchType.DONT_USE);
        ArrayList<MatchType> matchTypelistFuzzy = new ArrayList<MatchType>();
        matchTypelistFuzzy.add(MatchType.FUZZY);

        FieldDefinition idFD = new FieldDefinition();
        idFD.setDataType("int");
        idFD.setFieldName("id");
        idFD.setMatchType(matchTypelistId);
        fdList.add(idFD);
        
        FieldDefinition yearFD = new FieldDefinition();
        yearFD.setDataType("int");
        yearFD.setFieldName("year");
        yearFD.setMatchType(matchTypelistFuzzy);
        fdList.add(yearFD);
        
        FieldDefinition nameFD = new FieldDefinition();
        nameFD.setDataType("string");
        nameFD.setFieldName("name");
        nameFD.setMatchType(matchTypelistFuzzy);
        fdList.add(nameFD);
        
        FieldDefinition eventFD = new FieldDefinition();
        eventFD.setDataType("string");
        eventFD.setFieldName("event");
        eventFD.setMatchType(matchTypelistFuzzy);
        fdList.add(eventFD);

        FieldDefinition dobFD = new FieldDefinition();
        dobFD.setDataType("date");
        dobFD.setFieldName("dob");
        dobFD.setMatchType(matchTypelistFuzzy);
        fdList.add(dobFD);
        
        FieldDefinition commentFD = new FieldDefinition();
        commentFD.setDataType("string");
        commentFD.setFieldName("comment");
        commentFD.setMatchType(matchTypelistFuzzy);
        fdList.add(commentFD);

        args.setFieldDefinition(fdList);

        return args;
    }
    
}

