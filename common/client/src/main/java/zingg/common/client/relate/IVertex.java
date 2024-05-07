package zingg.common.client.relate;

import java.util.List;

import zingg.common.client.Named;
import zingg.common.client.pipe.Pipe;

public interface IVertex extends Named{

    public List<IEdge> getEdges();
    public void setEdges(List<IEdge> edges);
    public IVertexType getVertexType();
    public void setVertextType(IVertexType v);
    public void setData(Pipe[] p);
    public Pipe[] getData();
    
}
