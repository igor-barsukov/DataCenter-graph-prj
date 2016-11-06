
public enum NodeRoles {
	SOURCE("source"), 
	RECEIVER("receiver");
	
	private final String roleValue;
	
	private NodeRoles(String role){
		this.roleValue = role;
	}
	
	public String toString(){
		return roleValue;
	}
	
}
