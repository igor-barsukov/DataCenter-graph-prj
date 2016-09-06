
public class DCEdge {
	private DCNode startNode;
	private DCNode endNode;
	private Integer edgeId;
	private String weight;
	
	public DCEdge(DCNode startNode, DCNode endNode, int edge, String weight){
		this.startNode = startNode;
		this.endNode = endNode;
		this.edgeId = edge;
		this.weight = weight;
	}
	
	public Integer getId(){
		return this.edgeId;
	}
	
	public String getWeight(){
		return this.weight;
	}
	
	public DCNode getStartNode(){
		return this.startNode;
	}
	
	public DCNode getEndNode(){
		return this.endNode;
	}
}
