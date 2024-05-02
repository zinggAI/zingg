package zingg.common.client.relate;

public class VertexType implements IVertexType{

    private String name;

	public VertexType(String name){
		setName(name);
	}

	public VertexType(){

	}

	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
    
}
