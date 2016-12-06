import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class DCGraphExecutor {
	
	private static double SOURCES_FACTOR = 0.2; // represents portion of sources in topology, corresponding factor of receivers = 1 - SOURCES_FACTOR
	private static int NODES_AVERAGE_COST = 40;
	
	private static List<DCNode> nodes = new ArrayList<DCNode>(); 
	private static List<DCEdge> edges = new ArrayList<DCEdge>();
	private static List<String> rawData = new ArrayList<String>();
	
	private static List<DCNode> clients = new ArrayList<DCNode>();
	private static List<DCNode> servers = new ArrayList<DCNode>();
	private static List<DCEdge> clientServerEdges = new ArrayList<DCEdge>();  // edges with a_node - client , b_node - server and vice versa
	
	private static Map<DCNode, List<DCNode>> sourceReceiverMap = new HashMap<>();
	
	private static int edgeId;
	
	private static String fileName1 = "WeightedFile2.txt";
	private static String fileName222 = "oregon1_010331.txt";
	private static String fileName2 = "igba_custom_topo.txt";
//	private static String fileNameOut = "oregon1_010331_Weight.txt";
	private static String fileNameOut = "igba_custom_topo_Weight.txt";
	
	public static void main(String[] args) throws Exception{
		PrintStream out = new PrintStream(new FileOutputStream("logs/output.txt"));
		System.setOut(out);
		
		rawData = graphConfigParserWithWeigtAdding(fileName2, fileNameOut);
		graphDataParser(rawData);
		System.out.println("nodes size " + nodes.size());
		System.out.println("edges size " + edges.size());
//		roleDistributor();
//		searchClients();
		setNeighbors();
		setRoles1();
		findShortestPathesForReceivers();
//        optimizeClients();
		
//		GMLGenerator generator = new GMLGenerator(nodes, edges, fileName2);
//		generator.execute();
	}
	
	
	private static List<String> graphConfigParserWithWeigtAdding(String fileNameIn, String fileNameOut) throws IOException{
		BufferedReader reader = null;
        BufferedWriter writer = null;
		Integer generatedWeight;
		List<String> lines = new ArrayList<String>();
		System.out.println("graphConfigParserWithWeigtAdding starting");
		try {
            reader = new BufferedReader(new FileReader("configs/" + fileNameIn));
            File file = new File("configs/" + fileNameOut);
            if (!file.exists()) {
            	file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
	        String line;
                while ((line = reader.readLine()) != null) {
                	if(!line.startsWith("#")){
                    	generatedWeight = ThreadLocalRandom.current().nextInt(5, 25);
//                    	System.out.println("generatedWeight - " + generatedWeight.toString());
                    	line = line.concat("        ").concat(generatedWeight.toString());
//                    	System.out.println("new line - " + line);
                	}
                    // write into file 
                    writer.write(line);
                    writer.newLine();
                    lines.add(line);
//                    System.out.println("line has written");
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
		
	private static void graphDataParser(List <String> rawData) throws Exception{
//		edgeId = 0;
		for (String line : rawData){
			if(!line.startsWith("#")){
				String [] arr = line.split("\\s+");
				String sNodeId = arr[0]; 
//				System.out.println("snode - " + sNodeId);
				DCNode sNode = new DCNode(sNodeId);
				if(!nodes.contains(sNode)){
					nodes.add(sNode);
				}
				String eNodeId = arr[1];
//				System.out.println("enode - " + eNodeId);
				DCNode eNode = new DCNode(eNodeId);
				if(!nodes.contains(eNode)){
					nodes.add(eNode);
				}
				String weight = arr[2];
//				System.out.println("weight - " + weight);
				
				
				DCEdge newEdge = new DCEdge(sNode, eNode, edgeId, weight);
//				System.out.println("edge id - " + newEdge.getId());
				edges.add(newEdge);
				edgeId = ++edgeId ;
			}
//			System.out.println("delimeter");
		}
		System.out.println("Initial topology: ");
		for(DCEdge edge : edges){
			System.out.println("From - " + edge.getStartNode().getId() + "  to - " + edge.getEndNode().getId() + "  weight - " + edge.getWeight());
		}
		
	}
			
	private static void setRoles1(){
		List<DCNode> internalServers = new ArrayList<>(); // list for both sources and receivers
		List<DCNode> internalSources = new ArrayList<>();
		
		for(DCNode node : nodes){
			int i1 = 0;
			for(DCEdge edge : edges){
				if(edge.getStartNode().equals(node) || edge.getEndNode().equals(node)){
					++ i1; 
				}
			}
			if(i1 < 2)  {
				internalServers.add(node);
				System.out.println("setRoles server node - " + node.getId());
			}
 		}
        // make random order in list
		Collections.shuffle(internalServers);
		// 20% of nodes a sources
		int numOfSources = (int) Math.ceil(internalServers.size() * SOURCES_FACTOR);
		// 80% of nodes are receivers
        int numOfReceivers = internalServers.size() - numOfSources;

        System.out.println("setRoles numOfSources " + numOfSources);
		System.out.println("setRoles numOfReceivers " + numOfReceivers);
		System.out.println("setRoles numOfReceivers/numOfSources " + (numOfReceivers/numOfSources));

		// set sources (randomly)
		for(int i = 0; i <= numOfSources; i ++){
			DCNode source = internalServers.get(i);
			source.setSourceRole();
			internalSources.add(source);
			internalServers.remove(i);
		}
		System.out.println("setRoles sources list size " + internalSources.size());

		// set receivers for each source (randomly)
		for(DCNode source : internalSources){
//			System.out.println("setRoles current source " + source.getId());
			List<DCNode> receiversListForSource = new ArrayList<>();
			int numOfReceiversForList = 0;
			// can't remove elements from for-each cycle so use iterator
			for(Iterator<DCNode> iter = internalServers.iterator(); iter.hasNext(); ){
				if(numOfReceiversForList < numOfReceivers/numOfSources && !internalServers.isEmpty()){
					// should call the next() method only once on each iteration
					DCNode currentReceiver = iter.next();
//					System.out.println("setRoles current receiver " + currentReceiver.getId());
					currentReceiver.setReceiverRole();
					receiversListForSource.add(currentReceiver);
					iter.remove();
					numOfReceiversForList = ++ numOfReceiversForList;
//					System.out.println("setRoles num of receiver " + numOfReceiversForList);
				} else {
					break;
				}
					
			}
			sourceReceiverMap.put(source, receiversListForSource);
			
		}
		System.out.println("setRoles done");
		System.out.print("setRoles sourceReceiverMap: ");
    	for(Map.Entry<DCNode, List<DCNode>> entry : sourceReceiverMap.entrySet()){
    		System.out.print("Source " + entry.getKey().getId() + " has receivers [");
    		if(!entry.getValue().isEmpty()){
    			for(DCNode receiver : entry.getValue()){
    				System.out.print(receiver.getId() + "  ");
    			}
    			System.out.print("]  ");
    		} else{
    			System.out.print(" ]  ");
    		}
    	}
    	System.out.println(" ");
	}
        
    private static void setNeighbors(){
        for(DCNode node : nodes){
            for(DCEdge edge : edges){
                if(edge.getStartNode().equals(node)){
                    node.setNeighbor(edge.getEndNode());
                } else if(edge.getEndNode().equals(node)){
                    node.setNeighbor(edge.getStartNode());
                }
            }
        }
        System.out.println("setNeighbors done");
        for(DCNode node : nodes){
        	System.out.println("setNeighbors - neighbors for node " + node.getId() + " -  " + node.getNeighbors());
        }
        
    }
    
    private static void findShortestPathesForReceivers() throws CloneNotSupportedException{
    	Map<DCNode, List<DCNode>> newSourceReceiverMap = new HashMap<>();
    	for(Map.Entry<DCNode, List<DCNode>> entry : sourceReceiverMap.entrySet()){ 
    		DCNode sourceNode = entry.getKey();
    		sourceNode.setCost(0);
    		List<DCNode> markedReceivers = new ArrayList<>();
    		System.out.println("findShortestPathesForReceivers for source " + sourceNode.toString());
    		if(!entry.getValue().isEmpty()){
    			for(DCNode receiver : entry.getValue()){       			
//        			List<String> ids = new ArrayList<>();
//        			recursiveCall1(initFromNode, receiver, null, 0, ids);
    				
    				DCNode markedReceiver = bfsCall(sourceNode, receiver);
    				System.out.println("findShortestPathesForReceivers marked Receiver = " + markedReceiver.getId() + " , mark " + markedReceiver.getCost());
    				markedReceivers.add(markedReceiver);
        		}
    			sourceReceiverMap.put(sourceNode, markedReceivers);    			
    		}
    		newSourceReceiverMap.put(sourceNode, markedReceivers);
    	}
    	
    	System.out.println("findShortestPathesForReceivers - check calculated marks ...");
    	System.out.println("findShortestPathesForReceivers - sourceReceiverMap " + sourceReceiverMap);
    	System.out.println("findShortestPathesForReceivers - newSourceReceiverMap " + newSourceReceiverMap);
//    	for(Map.Entry<DCNode, List<DCNode>> entry : sourceReceiverMap.entrySet()){
//    		if(!entry.getValue().isEmpty()){
//    			for(DCNode receiver : entry.getValue()){
//    				System.out.println("Mark for node " + receiver.getId() + "  = " + receiver.getMark());
//	    		}
//    		}
//    	}
    	
    	System.out.println("Relocate receivers 1st stage...");
    	List<DCNode> unmovedReceivers = new ArrayList<>();
    	int averageCost = countAverageCost();
    	for(Map.Entry<DCNode, List<DCNode>> entry : sourceReceiverMap.entrySet()){
    		if(!entry.getValue().isEmpty()){
    			DCNode source = entry.getKey();
    			DCNode gateway = source.fetchGateway(edges).getKey();
    			System.out.println("Relocate receivers for source = " + source.getId() + " , with gateway = " + gateway.getId());
    			for(DCNode receiver : entry.getValue()){
    				if(receiver.getCost() > averageCost){
    					System.out.println("Relocate receivers  - receiver " + receiver.getId() + " has overvalued mark = " + receiver.getCost());
    					if(gateway.getAvailablePorts() > 0){
    						// updating DCEdges
    						DCEdge currentEdge = receiver.fetchGateway(edges).getValue();
    						int edgeWithGatewayIndex = edges.indexOf(currentEdge);
    						DCEdge newEdge = new DCEdge(receiver, gateway, currentEdge.getId(), currentEdge.getWeight());
    						edges.set(edgeWithGatewayIndex, newEdge);
    						
    						System.out.println("Relocate receivers - receiver " + receiver.getId() + " was transfered");
    					} else{
    						System.out.println("Relocate receivers - gateway " + gateway.getId() + " doesn't have available ports");
    						unmovedReceivers.add(receiver);
    					}
    					
    				}
	    		}
    		}
    	}
    	
    	System.out.println("Relocate receivers 2st stage...");
    	for(DCNode node : unmovedReceivers){
//    		List <DCNode> gateways = getAvailableGateways();
    		
    	}
    	
        System.out.println("Topology after optimization:");
        for(DCEdge edge : edges){
        	System.out.println("From - " + edge.getStartNode().getId() + "  to - " + edge.getEndNode().getId() + "  weight - " + edge.getWeight());
        }
    	
    }   
    
    private static int countAverageCost(){
    	int costSum = 0;
    	int receiversNumber = 0;
    	int averageCost = 0;
    	for(Map.Entry<DCNode, List<DCNode>> entry : sourceReceiverMap.entrySet()){
    		if(!entry.getValue().isEmpty()){
    			for(DCNode receiver : entry.getValue()){
    				costSum += receiver.getCost();
    				receiversNumber ++;
    			}
    		}
    	}
    	System.out.println("costSum = " + costSum);
    	System.out.println("receiversNumber = " + receiversNumber);
    	averageCost = costSum/receiversNumber;
    	System.out.println("averageCost = " + averageCost);
    	return averageCost;
    }
    
    private static DCNode bfsCall(DCNode fromNode, DCNode toNode) throws CloneNotSupportedException{
    	DCNode updatedReceiverClone = new DCNode();
    	List<String> visitedNodeIds = new ArrayList<>();
    	Queue<DCNode> queue = new LinkedList<>();
    	System.out.println("bfsCall start from " + fromNode.getId() + " to " + toNode.getId());
    	visitedNodeIds.add(fromNode.getId());
    	queue.add(fromNode);
    	fromNode.setCost(0);
    	
    	while(!queue.isEmpty()){
    		DCNode node1 = queue.poll();
    		System.out.println("bfsCall removing from queue node " + node1.getId());
    		if(node1.equals(toNode)) {
    			System.out.println("bfsCall - destination node " + toNode.getId() + " reached with weight = " + node1.getCost());
    			
    			updatedReceiverClone = node1.clone();
    			updatedReceiverClone.setId(node1.getId());
    			updatedReceiverClone.setCost(node1.getCost());
    			updatedReceiverClone.setReceiverRole();
    			
    			return updatedReceiverClone;	
    		}
    		System.out.println("bfsCall visited nodes " + visitedNodeIds);
    		// problem - node1 has problem with searching neighbors
    		DCNode node11 = nodes.get(nodes.indexOf(node1));
    		node11.setCost(node1.getCost());
    		System.out.println("bfsCall node1 " + node1.getId() + " has weight = " + node1.getCost() + " , and neigboors = " + node1.getNeighbors());
    		System.out.println("bfsCall node11 " + node11.getId() + " has weight = " + node11.getCost() + " , and neigboors = " + node11.getNeighbors());
    		if(node11.getCost() > 100000){
    			node11.setCost(0);
    		}
    		for(DCNode node2 : node11.getNeighbors()){
    			if(!visitedNodeIds.contains(node2.getId())){
    				visitedNodeIds.add(node2.getId());
    				System.out.println("bfsCall - add to queue node " + node2.getId());
    				System.out.println("bfsCall - for prev node " + node11.getId() + " weight =" + node11.getCost() + " , weignt between nodes = " + DCNode.getWeightForNodes(node2, node11, edges));
    				int mark = node11.getCost() + DCNode.getWeightForNodes(node2, node11, edges);
    				System.out.println("bfsCall - for current node " + node2.getId() + " weignt = " + mark);
    				node2.setCost(mark);
    				queue.add(node2);
    			}
    		}
    		
    	}
    	return updatedReceiverClone;	
    }
    
    //contains error
    // check visited nodes at every iteration
    private static void recursiveCall1(DCNode fromNode, DCNode toNode, DCNode previousNode, int mark, List<String> visitedNodeIds){
    	System.out.println("recCall calling with params - fromNode=" + fromNode.getId() + " , toNode=" + toNode.getId()
    			+ " , previousNode=" + (previousNode == null ? "null" : previousNode.getId()) + " , mark=" + mark);
    	try{
    		DCNode testNode = nodes.get(nodes.indexOf(fromNode));
        	if(testNode.getNeighbors().contains(toNode)){
            	mark = mark + DCNode.getWeightForNodes(fromNode, toNode, edges);
            	toNode.setCost(mark);
            	System.out.println("recCall final for node " + toNode.toString() + "  , mark = " + mark);
            } else {
            	System.out.println("recCall visitedNodes {" + visitedNodeIds + "}");
            	if(/*previousNode != null && */ !visitedNodeIds.contains(fromNode.getId()) ){
            		System.out.println("recCall inside else");
            		System.out.println("recCall fromNode.getNeighbors() " + fromNode.getNeighbors());
            		System.out.println("recCall testNode.getNeighbors() " + testNode.getNeighbors());
            		for(DCNode neighbor : testNode.getNeighbors()){
            			if(neighbor != previousNode){
            				System.out.println("recCall neighbor " + neighbor.toString());
                            mark = mark + DCNode.getWeightForNodes(fromNode, neighbor, edges);
                            System.out.println("recCall mark = " + mark);
                            visitedNodeIds.add(fromNode.getId());
                            previousNode = fromNode;
                            fromNode = neighbor;
//                            System.out.println("recursion executes with parameters - fromNode=" + fromNode + " , toNode=" + toNode + " ,previousNode=" + previousNode + " ,mark=" + mark);
                            recursiveCall1(fromNode, toNode, previousNode, mark, visitedNodeIds);
//                            Thread.sleep(1000);
            			} else {
            				System.out.println("recCall wrong path");
            				return;
            			}
            			
                    }
            	} else{
            		System.out.println("recCall visited path -> return");
    				return;
            	}
            }
        }catch(Exception e){
        	System.out.println("error  - " + e);
        }
    }
    
        
	// obsolete
	private static void roleDistributor(){
		//enabling source role if node belongs more than 10 edges 
		
		// source - if node id occures more than 10 times at start or end position of edges
		// receiver - if node id occures only 1 time at start or end position of all edges
		// problem - what if there are edges with source and without receiver and vice versa? 
		List<DCNode> sourceCandidates = new ArrayList<DCNode>();
                for(DCNode node : nodes){
			System.out.println("puting role for node - " + node.getId());
			

			
//    			int counter = 0;
//    			for(DCEdge edge : edges){
//    				if(edge.getStartNode().equals(node)){
//    					++counter;
//    					receiverCandidates.add(edge.getEndNode());
//    				}
//    			}
//    			System.out.println("counter - " + counter);
//    			if(counter > 10){
//    				System.out.println("set source");
//    				node.setSourceRole();
//    				for(DCNode dcnode : receiverCandidates){
//    					dcnode.setReceiverRole();
//    				}
//    				receiverCandidates.clear();
//    			} else {
//    				node.setReceiverRole();
//    				System.out.println("set receiver");
//    			}
			
			/*
			 * test another variant
			 * */
			int startCounter = 0;
			int endCounter = 0;
			for(DCEdge edge : edges){
				if(edge.getStartNode().equals(node)){
					++startCounter;
				}
				if(edge.getEndNode().equals(node)){
					++endCounter;
				}
			}
			if(startCounter == 1 && endCounter == 1){
				System.out.println("*************************Conflict");
				continue;
			}
			if(startCounter > 10 || endCounter > 10){
				node.setSourceRole();
                sourceCandidates.add(node);
				continue;
			} else if(startCounter == 1 && endCounter == 0){
				node.setReceiverRole();
				System.out.println("*************************Receiver start");
				continue;
			} else if(startCounter == 0 && endCounter == 1){
				node.setReceiverRole();
				System.out.println("*************************Receiver end");
				continue;
			}
			
		}
		
//    		int i = 0;
//    		int j = 0;
//    		for(DCNode node : nodes){
//    			if(node.getRole().equals("source")){
//    				++i;
//    				System.out.println("Source with id - " + node.getId());
//    			}
//    			if(node.getRole().equals("receiver")){
//    				++j;
//    				System.out.println("Receiver with id - " + node.getId());
//    			}
//    		}
//    		System.out.println("source num - " + i + " , receiver num - " + j);
		
		int edgeCounter = 0;
        List<DCEdge> clientServerEdges = new ArrayList<DCEdge>();  // edges with a_node - client , b_node - server and vice versa
		for(DCEdge edge : edges){
			if((edge.getStartNode().getRole().equals("source") && edge.getEndNode().getRole().equals("receiver"))
				|| (edge.getStartNode().getRole().equals("receiver") && edge.getEndNode().getRole().equals("source"))){
				
				++ edgeCounter;
                clientServerEdges.add(edge);
			}
		}
		System.out.println("edgeCounter - " + edgeCounter); // = 3
		
                for(DCNode node : sourceCandidates){
                    int integralMetric = 0;   // need to store this val in DCNode source object
                    for(DCEdge edge : clientServerEdges){
                    if(edge.getStartNode().equals(node) || edge.getEndNode().equals(node)){
                            integralMetric += Integer.parseInt(edge.getWeight());
                        }
                    }    
                }

    }
    	
    	// for test purpose
    	private static void searchClients(){
    		List<DCNode> singleClients = new ArrayList<DCNode>();
    		
    		for(DCNode node : nodes){
    			int i1 = 0;
    			for(DCEdge edge : edges){
    				if(edge.getStartNode().equals(node) || edge.getEndNode().equals(node)){
    					++ i1; 
    				}
    			}
    			if(i1 < 2)  {
    				singleClients.add(node);
    				System.out.println("single node - " + node.getId());
    			}
     		}
    		
    		System.out.println("singleClients size = " + singleClients.size()); //3720
    		
    	}    
	  
    //obsolete	
	private static void setRoles(){
//		List<DCNode> clients = new ArrayList<DCNode>();   // global
//		Set<DCNode> servers = new HashSet<DCNode>();      // what's wrong with Set?!
//		List<DCNode> servers = new ArrayList<DCNode>();   // global
		
		for(DCNode node : nodes){
			int i1 = 0;
			for(DCEdge edge : edges){
				if(edge.getStartNode().equals(node) || edge.getEndNode().equals(node)){
					++ i1; 
				}
			}
			if(i1 < 2)  {
				node.setReceiverRole();
				clients.add(node);
				System.out.println("client node - " + node.getId());
			}
 		}
		for(DCNode node : clients){
			for(DCEdge edge : edges){
				if(edge.getStartNode().equals(node) && !clients.contains(edge.getEndNode())){
					clientServerEdges.add(edge);
					if(!servers.contains(edge.getEndNode())){
						edge.getEndNode().setSourceRole();
						servers.add(edge.getEndNode());
						System.out.println("server node - " + edge.getEndNode().getId());
					}
					
				} else if(edge.getEndNode().equals(node) && !clients.contains(edge.getStartNode())){
					clientServerEdges.add(edge);
					if(!servers.contains(edge.getStartNode())){
						edge.getStartNode().setSourceRole();
						servers.add(edge.getStartNode());
						System.out.println("server node - " + edge.getStartNode().getId());
					}
					
				}
				
			}
		}
		
		System.out.println("singleClients size = " + clients.size());
		System.out.println("servers size = " + servers.size());
		
	}
	
	//obsolete
	private static void calculateIntegralMetric(){
        for(DCNode node : servers){
            int integralMetric = 0;   // need to store this val in DCNode source object
            int countTimes = 0;
            for(DCEdge edge : clientServerEdges){
	            if(edge.getStartNode().equals(node) || edge.getEndNode().equals(node)){
	                    integralMetric += Integer.parseInt(edge.getWeight());
	                    ++ countTimes;
	            }
            }
            System.out.println("for server " + node.getId() + " integral metric = " + integralMetric + " at " + countTimes + " counts");
        }
	}
}
