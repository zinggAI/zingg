package zingg.common.core.preprocess;

import java.util.ArrayList;
import java.util.List;

public class CommonPreprocOrder implements IPreprocOrder{

   List<IPreprocType> order;

    public CommonPreprocOrder(){
        order = new ArrayList<>();
        order.add(IPreprocTypes.TRIM);
        order.add(IPreprocTypes.LOWERCASE);
        order.add(IPreprocTypes.STOPWORDS);
    }

    @Override
    public List<IPreprocType> getOrder() {
        return order;
    }

   

}
