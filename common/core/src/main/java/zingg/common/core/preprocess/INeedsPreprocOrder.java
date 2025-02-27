package zingg.common.core.preprocess;

public interface INeedsPreprocOrder{

   default IPreprocOrder getPreprocOrder(){
      return new CommonPreprocOrder();
   }

}
