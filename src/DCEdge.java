
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
       
        public boolean containsNodes(DCNode node1, DCNode node2){
            if((this.getStartNode().equals(node1) && this.getEndNode().equals(node2)) ||
               (this.getEndNode().equals(node1) && this.getStartNode().equals(node2))){
                return true;
            }
            return false;
        }
        
       	@Override
	 public String toString(){
		 return "Edge with id = " + this.edgeId + " , start node id = " + this.startNode.getId() + " , end node id = " + this.endNode.getId() + " and weight = " + this.weight;
	 }
}
