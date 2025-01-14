package zingg.common.client.util;

public class PathUtil {

    public static String getSparkLocationFromPath(String name){
        name =  name.replaceAll("-", "");
        return name;
    }

    public static String getSnowTableFromPath(String name){
        name =  name.replaceAll("/", "_");
        name =  name.replaceAll("-", "");
        return name;
    }
    
}
