
public class DCNode {
	
	private String nodeId;
	private int mark;
	private String role = "";
	
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
		 return "node with id - " + this.nodeId + " and mark - " + this.mark;
	 }
}
