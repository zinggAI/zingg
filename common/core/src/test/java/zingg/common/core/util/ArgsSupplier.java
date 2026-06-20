package zingg.common.core.util;

import java.util.ArrayList;
import java.util.List;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IMatchType;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.Arguments;

public class ArgsSupplier {
    
    public static Arguments getArgs() throws ZinggClientException {
        
        Arguments args = new Arguments();
        args.setModelId("junit");
        
        List<FieldDefinition> fdList = new ArrayList<FieldDefinition>(4);

        ArrayList<IMatchType> matchTypelistId = new ArrayList<IMatchType>();
        matchTypelistId.add(MatchTypes.DONT_USE);
        ArrayList<IMatchType> matchTypelistFuzzy = new ArrayList<IMatchType>();
        matchTypelistFuzzy.add(MatchTypes.FUZZY);

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
