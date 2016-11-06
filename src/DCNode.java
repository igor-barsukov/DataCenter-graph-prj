
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DCNode {
	
	private String nodeId;
	private int mark;
	private String role = "";
        private List<DCNode> neighbors = new ArrayList<>();
	
	private static int INF = Integer.MAX_VALUE/2;
	
	public DCNode(String id){
		this.nodeId = id;
		this.mark = INF;
	}
	
	public void setId(String id){
		this.nodeId = id;
	}
	
	public String getId(){
		return nodeId;	
	}
	
	public void setMark(int mark){
		this.mark = mark;
	}
	
	public int getMark(){
		return mark;	
	}
	
	public void setSourceRole(){
		this.role = "source";
	}
	
	public void setReceiverRole(){
		this.role = "receiver";
	}
	
	public String getRole(){
		return role;
	}
        
        public void setNeighbor(DCNode neighbor){
            this.neighbors.add(neighbor);
        }
        
        public List<DCNode> getNeighbors(){
            return this.neighbors;
        }
               
        // fetching gateway and link for sources or receivers - assuming that there is only one link, on one side of it placed source(receiver) and on another - gateway
        public Map.Entry<DCNode, DCEdge> fetchGateway(List<DCEdge> listOfEdges){
            DCNode gateway = null;
            DCEdge link = null;
            for(DCEdge edge : listOfEdges){
                if(edge.getStartNode().equals(this)){
                        gateway = edge.getEndNode();
                        link = edge;
                        break;
                } else if(edge.getEndNode().equals(this)){
                        gateway = edge.getStartNode();
                        link = edge;
                        break;
                }
            }
            Map.Entry<DCNode, DCEdge> resultEntry = new AbstractMap.SimpleEntry<>(gateway, link);
            return resultEntry;
        }
        
       // searches link where "from" and "to" node places and returnes link weight
       public static int getWeightForNodes(DCNode from, DCNode to, List<DCEdge> listOfEdges){
            for(DCEdge edge : listOfEdges){
                if(edge.containsNodes(from, to)){
                    return Integer.parseInt(edge.getWeight());
                }
            }
            return 0;
        }
	
//	@Override
//    public boolean equals(Object object)
//    {
//        boolean sameId = false;
//
//        if (object != null && object instanceof Node)
//        {
//            sameId = this.nodeId == ((Node) object).nodeId;
//        }
//
//        return sameId;
//    }
	
	 @Override
	    public boolean equals(Object object) {

	        if (object != null && object instanceof DCNode) {
	        	DCNode thing = (DCNode) object;
	            if (nodeId == null) {
	                return (thing.nodeId == null);
	            }
	            else {
	                return nodeId.equals(thing.nodeId);
	            }
	        }

	        return false;
	    }
	 
	 @Override
	 public String toString(){
		 return "Node with id - " + this.nodeId + " and mark - " + this.mark;
	 }
}
