package zingg.common.core.model.data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import zingg.common.core.model.model.ModelDF;

public class ModelDFData {

    public static List<ModelDF> getPosDF() {

        int row_id = 1;
        List<ModelDF> sample = new ArrayList<ModelDF>();
        sample.add(new ModelDF( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Date(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Date(101),1));
        sample.add(new ModelDF( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Date(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Date(201),1  ));
        sample.add(new ModelDF(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Date(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Date(301),1 ));
        sample.add(new ModelDF( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Date(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Date(101),1));
        sample.add(new ModelDF( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Date(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Date(201),1  ));
        sample.add(new ModelDF(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Date(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Date(301),1 ));
        sample.add(new ModelDF( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Date(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Date(101),1));
        sample.add(new ModelDF( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Date(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Date(201),1  ));
        sample.add(new ModelDF(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Date(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Date(301),1 ));
        sample.add(new ModelDF( row_id++, "1675683807452:31",  "nicole","event1","comment1", 1992,  new Date(100),   1, row_id++, "1675683807452:31",    "nicol","event11","comment11"    , 1992, new Date(101),1));
        sample.add(new ModelDF( row_id++, "1675683807452:32", "vkas","event2","comment2",1993,     new Date(200),1, row_id++, "1675683807452:32", "vikas","event12","comment21"      ,1992, new Date(201),1  ));
        sample.add(new ModelDF(row_id++, "1675683807452:33",  "agrawaal","event3","comment3",1994,    new Date(300),1, row_id++, "1675683807452:33", "agarwal","event13","comment31"    ,1992,      new Date(301),1 ));

        return sample;
    }

    public static List<ModelDF> getNegDF() {

        int row_id = 10000;
        int i = 0;
        List<ModelDF> sample = new ArrayList<ModelDF>();
        sample.add(new ModelDF( row_id++, "41",   "akevin"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "42",   "bkevin"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "43",   "ckevin"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "44",   "dkevin"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "45",   "ekevin"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "46",   "fkevin"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "47",   "gkevin"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "48",   "kevinh"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "49",   "kevini"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "50",   "kevinj"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "51",   "kevink"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));
        sample.add(new ModelDF( row_id++, "52",   "kevinl"     ,"event"+(99+i++)     ,"comment"+(99+i++)    ,1992+i++,   new Date(300), 0,row_id++, "510"+(i++),   "carmin"+(i++),    (i++)+"hello",    "useless"+(i++),       1811+i++,       new Date(System.currentTimeMillis()), 0));

        return sample;
    }

    public static List<ModelDF> getUnseenRowsForPredictDF(){

        int row_id = 1000;
        List<ModelDF> sample = new ArrayList<ModelDF>();
        sample.add(new ModelDF( row_id++, "7675683807452:131",  "rakesh","show","hello1", 2001,  new Date(100),   1, row_id++, "7675683807452:131",    "rakes","showbiz","hell111"    , 2002, new Date(101),1));
        sample.add(new ModelDF( 10100, "52",   "abc"     ,"def"    ,"geh"    ,1900,   new Date(1900), 0,10101, "410",   "ijk",    "lmn",    "opq",       2001,       new Date(System.currentTimeMillis()), 0));

        return sample;

    }

    
}


