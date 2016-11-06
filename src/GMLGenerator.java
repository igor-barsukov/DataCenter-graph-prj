import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GMLGenerator {
	private List<DCNode> nodesList;
	private List<DCEdge> edgesList;
	private String fileName;
	
	public GMLGenerator(List<DCNode> nodes, List<DCEdge> edges, String initialFileName) {
		this.nodesList = nodes;
		this.edgesList = edges;
		this.fileName = "gml_" + initialFileName.substring(0, initialFileName.indexOf('.')) + ".gml";
	}
	
	
	public void execute() throws IOException{
        BufferedWriter writer = null;
		try {
            
            File file = new File("configs/" + fileName);
            if (!file.exists()) {
            	file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            writer.write("graph");
            writer.newLine();
            writer.write("[");
            writer.newLine();
            
	        for(DCNode node : nodesList){
	        	  writer.write("\tnode");
	        	  writer.newLine();
	              writer.write("\t[");
	              writer.newLine();
	        	  writer.write("\t\tid " + node.getId());
	              writer.newLine();
	              writer.write("\t\tlabel \"Node " + node.getId() + "\"");
	              writer.newLine();
	              writer.write("\t]");
	              writer.newLine();
	        }
	        
	        for(DCEdge edge : edgesList){
	        	  writer.write("\tedge");
	        	  writer.newLine();
	              writer.write("\t[");
	              writer.newLine();
	              writer.write("\t\tsource " + edge.getStartNode().getId());
	              writer.newLine();
	              writer.write("\t\ttarget " + edge.getEndNode().getId());
	              writer.newLine();
	              writer.write("\t\tlabel \"Edge " + edge.getStartNode().getId() + " to " + edge.getEndNode().getId() + "\"");
	              writer.newLine();
	              writer.write("\t]");
	              writer.newLine();
	        }
	        
	        writer.write("]");
            writer.newLine();
            System.out.println("gml parser fihished");
	        	        	       
		} catch (IOException e) {
			System.out.println("IO - " + e);
		} catch (Exception e){
			System.out.println("Common e - " + e);
		}
		finally{
			writer.close();
        }
		
	}
	
}
