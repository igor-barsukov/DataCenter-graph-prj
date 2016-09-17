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
	private static String fileNameOut = "oregon1_010331_Weight.txt";
	
	public static void main(String[] args) throws Exception{
		PrintStream out = new PrintStream(new FileOutputStream("logs/output.txt"));
		System.setOut(out);
		
		rawData = graphConfigParserWithWeigtAdding();
		graphDataParser(rawData);
		System.out.println("nodes size " + nodes.size());
		System.out.println("edges size " + edges.size());
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
		
		// проблема - как искать кратчайшие расстояния до каждого источника? ведь есть линки, в которые не входят источники, куда их относить? 
		
		for(DCNode node : nodes){
			System.out.println("puting role for node - " + node.getId());
			int counter = 0;
			for(DCEdge edge : edges){
				if(edge.getStartNode().equals(node)){
					counter ++;
				}
			}
			System.out.println("counter - " + counter);
			if(counter > 10){
				System.out.println("true");
				node.setSourceRole();
			} else {
				node.setReceiverRole();
			}
		}
	}
}
