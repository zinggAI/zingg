package zingg.common.client.relate.args;

import java.util.List;

import zingg.common.client.IZArgs;
import zingg.common.client.relate.IVertex;

public interface IRelateArgs extends IZArgs{

    List<IVertex> getVertices();
    
}
