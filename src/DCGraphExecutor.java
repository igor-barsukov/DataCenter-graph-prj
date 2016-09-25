import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DCGraphExecutor {

	private static List<DCNode> nodes = new ArrayList<DCNode>(); 
	private static List<DCEdge> edges = new ArrayList<DCEdge>();
	private static List<String> rawData = new ArrayList<String>();
	
	private static int edgeId;
	
	private static String fileName1 = "WeightedFile2.txt";
	private static String fileName2 = "oregon1_010331.txt";
	private static String gmlFile = "gml_oregon1_010331.gml";
	private static String fileNameOut = "oregon1_010331_Weight.txt";
	
	public static void main(String[] args) throws Exception{
		PrintStream out = new PrintStream(new FileOutputStream("logs/output.txt"));
		System.setOut(out);
		
		rawData = graphConfigParserWithWeigtAdding();
		graphDataParser(rawData);
		System.out.println("nodes size " + nodes.size());
		System.out.println("edges size " + edges.size());
		gmlParser();
	}
	
	private static List<String> graphConfigParser(){
		BufferedReader reader;
		List<String> lines = new ArrayList<String>();
		
		try {
			reader = new BufferedReader(new FileReader("configs/" + fileName1));
	        String line;
	
			while ((line = reader.readLine()) != null) {
			    lines.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return lines;
	}
	
	private static List<String> graphConfigParserWithWeigtAdding() throws IOException{
		BufferedReader reader = null;
        BufferedWriter writer = null;
		Integer generatedWeight;
		List<String> lines = new ArrayList<String>();
		System.out.println("graphConfigParserWithWeigtAdding starting");
		try {
            reader = new BufferedReader(new FileReader("configs/" + fileName2));
            File file = new File("configs/" + fileNameOut);
            if (!file.exists()) {
            	file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
	        String line;
                while ((line = reader.readLine()) != null) {
                	if(!line.startsWith("#")){
                    	generatedWeight = ThreadLocalRandom.current().nextInt(5, 25);
                    	System.out.println("generatedWeight - " + generatedWeight.toString());
                    	line = line.concat("        ").concat(generatedWeight.toString());
                    	System.out.println("new line - " + line);
                	}
                    // write into file 
                    writer.write(line);
                    writer.newLine();
                    lines.add(line);
                    System.out.println("line has written");
                }
		} catch (IOException e) {
			System.out.println("IO - " + e);
		} catch (Exception e){
			System.out.println("Common e - " + e);
		}
		finally{
			reader.close();
			writer.close();
        }
		
		return lines;
	}
	
	private static void gmlParser() throws IOException{
        BufferedWriter writer = null;
		try {
            
            File file = new File("configs/" + gmlFile);
            if (!file.exists()) {
            	file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            writer.write("graph");
            writer.newLine();
            writer.write("[");
            writer.newLine();
            
	        for(DCNode node : nodes){
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
	        
	        for(DCEdge edge : edges){
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
	
	/*put here role distributing logic*/
	private static void graphDataParser(List <String> rawData) throws Exception{
//		edgeId = 0;
		for (String line : rawData){
//			System.out.println(line);
			if(!line.startsWith("#")){
				String [] arr = line.split("\\s+");
				String sNodeId = arr[0]; 
				System.out.println("snode - " + sNodeId);
				DCNode sNode = new DCNode(sNodeId);
				if(!nodes.contains(sNode)){
					nodes.add(sNode);
				}
				String eNodeId = arr[1];
				System.out.println("enode - " + eNodeId);
				DCNode eNode = new DCNode(eNodeId);
				if(!nodes.contains(eNode)){
					nodes.add(eNode);
				}
				String weight = arr[2];
				System.out.println("weight - " + weight);
				
				
				DCEdge newEdge = new DCEdge(sNode, eNode, edgeId, weight);
				System.out.println("edge id - " + newEdge.getId());
				edges.add(newEdge);
				edgeId = ++edgeId ;
			}
			System.out.println("delimeter");
		}
		
		//enabling source role if node belongs more than 10 edges 
		
		// source - if node id occures more than 10 times at start or end position of edges
		// receiver - if node id occures only 1 time at start or end position of all edges
		// problem - what if there are edges with source and without receiver and vice versa? 
		for(DCNode node : nodes){
			System.out.println("puting role for node - " + node.getId());
			List<DCNode> receiverCandidates = new ArrayList<DCNode>();
			List<DCNode> receiverCandidates2 = new ArrayList<DCNode>();
			
//			int counter = 0;
//			for(DCEdge edge : edges){
//				if(edge.getStartNode().equals(node)){
//					++counter;
//					receiverCandidates.add(edge.getEndNode());
//				}
//			}
//			System.out.println("counter - " + counter);
//			if(counter > 10){
//				System.out.println("set source");
//				node.setSourceRole();
//				for(DCNode dcnode : receiverCandidates){
//					dcnode.setReceiverRole();
//				}
//				receiverCandidates.clear();
//			} else {
//				node.setReceiverRole();
//				System.out.println("set receiver");
//			}
			
			/*
			 * test another variant
			 * */
//			int startCounter = 0;
//			int endCounter = 0;
//			for(DCEdge edge : edges){
//				if(edge.getStartNode().equals(node)){
//					++startCounter;
//					receiverCandidates.add(edge.getEndNode());
//				}
//				if(edge.getEndNode().equals(node)){
//					++endCounter;
//					receiverCandidates2.add(edge.getStartNode());
//				}
//			}
//			if(startCounter == 1 && endCounter == 1){
//				System.out.println("*************************Conflict");
//				continue;
//			}
//			if(startCounter > 10 || endCounter > 10){
//				node.setSourceRole();
//				continue;
//			} else if(startCounter == 1 && endCounter == 0){
//				node.setReceiverRole();
//				System.out.println("*************************Receiver start");
//				continue;
//			} else if(startCounter == 0 && endCounter == 1){
//				node.setReceiverRole();
//				System.out.println("*************************Receiver end");
//				continue;
//			}
			
		}
		
//		int i = 0;
//		int j = 0;
//		for(DCNode node : nodes){
//			if(node.getRole().equals("source")){
//				++i;
//				System.out.println("Source with id - " + node.getId());
//			}
//			if(node.getRole().equals("receiver")){
//				++j;
//				System.out.println("Receiver with id - " + node.getId());
//			}
//		}
//		System.out.println("source num - " + i + " , receiver num - " + j);
	}
}
