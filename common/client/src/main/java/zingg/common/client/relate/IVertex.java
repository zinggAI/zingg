package zingg.common.client.relate;

import java.util.List;

import zingg.common.client.Named;

public interface IVertex extends Named{

    public List<IEdge> getEdges();
    public void setEdges(List<IEdge> edges);
    
}
