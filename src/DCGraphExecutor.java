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
import java.util.concurrent.ThreadLocalRandom;

public class DCGraphExecutor {

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
		
		rawData = graphConfigParserWithWeigtAdding();
		graphDataParser(rawData);
		System.out.println("nodes size " + nodes.size());
		System.out.println("edges size " + edges.size());
//		roleDistributor();
//		searchClients();
		setNeighbors();
		setRoles1();
		findShortestPathesForReceivers();
//        optimizeClients();
		
//		GMLGenerator generator = new GMLGenerator(nodes, edges, fileName222);
//		generator.execute();
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
	}
			
	private static void setRoles1(){
		List<DCNode> internalServers = new ArrayList<>(); // list for both sources and receivers
		List<DCNode> internalSources = new ArrayList<>();
		List<DCNode> internalReceivers = new ArrayList<>();
		
		// make global
//		Map<DCNode, List<DCNode>> sourceReceiverMap = new HashMap<>();

		for(DCNode node : nodes){
			int i1 = 0;
			for(DCEdge edge : edges){
				if(edge.getStartNode().equals(node) || edge.getEndNode().equals(node)){
					++ i1; 
				}
			}
			if(i1 < 2)  {
				internalServers.add(node);
				System.out.println("server node - " + node.getId());
			}
 		}
                // make random order in list
		Collections.shuffle(internalServers);
		// 20% of nodes a sources
		int numOfSources = (int) Math.ceil(internalServers.size() * 0.2);
		// 80% of nodes are receivers
                int numOfReceivers = internalServers.size() - numOfSources;
		System.out.println("numOfSources " + numOfSources);
		System.out.println("numOfReceivers " + numOfReceivers);
		System.out.println("numOfReceivers/numOfSources " + (numOfReceivers/numOfSources));
                // set sources (randomly)
		for(int i = 0; i <= numOfSources; i ++){
			DCNode source = internalServers.get(i);
			source.setSourceRole();
			internalSources.add(source);
			internalServers.remove(i);
		}
		System.out.println("sources list size " + internalSources.size());
                // set receivers for each source (randomly)
		for(DCNode source : internalSources){
			System.out.println("current source " + source.getId());
			List<DCNode> receiversListForSource = new ArrayList<>();
			int numOfReceiversForList = 0;
			// can't remove elements from for-each cycle so use iterator
			for(Iterator<DCNode> iter = internalServers.iterator(); iter.hasNext(); ){
				if(numOfReceiversForList < numOfReceivers/numOfSources && !internalServers.isEmpty()){
					// should call the next() method only once on each iteration
					DCNode currentReceiver = iter.next();
					System.out.println("current receiver " + currentReceiver.getId());
					currentReceiver.setReceiverRole();
					receiversListForSource.add(currentReceiver);
					iter.remove();
					numOfReceiversForList = ++ numOfReceiversForList;
					System.out.println("num of receiver " + numOfReceiversForList);
				} else {
					break;
				}
					
			}
			System.out.println("done");
			sourceReceiverMap.put(source, receiversListForSource);
			
		}
		System.out.println("mapa " + sourceReceiverMap);	
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
        System.out.println("Neighbors were set");
        for(DCNode node : nodes){
        	System.out.println("Neighbors for node " + node.getId() + " -  " + node.getNeighbors());
        }
        
    }
	
	private static void optimizeClients(){
		DCNode localServerGateway = null;
		for(Map.Entry<DCNode, List<DCNode>> entry : sourceReceiverMap.entrySet()){
			DCNode server = entry.getKey();
			//searching gateway
            localServerGateway = server.fetchGateway(edges).getKey();
                        
			//searching over clients - if client gateway not equal source's gateway - relocate client's edge
			for(DCNode client : entry.getValue()){
                Map.Entry<DCNode, DCEdge> gatewayWithLink = client.fetchGateway(edges);
                DCNode localClientGateway = gatewayWithLink.getKey();
                DCEdge clientEdge = gatewayWithLink.getValue();
				// TODO - enable checking of local gateway ports - limit 20
				if(!localClientGateway.equals(localServerGateway)){
					// createNewEdgeAndTransmitNode
					DCEdge newClientEdge = new DCEdge(client, localServerGateway, clientEdge.getId(), clientEdge.getWeight());
					edges.remove(clientEdge);
					edges.add(newClientEdge);
                    System.out.println("Client " + client.toString() + " has been transfered to edge " + newClientEdge.toString());
				}
			}
		}
        System.out.println("Edges after optimization:");
        for(DCEdge edge : edges){
            System.out.println("edge " + edge.toString());
        }
		
	}
    
    private static void findShortestPathesForReceivers(/*Map<DCNode, List<DCNode>> sourcesReceiversMap*/){
//    	setNeighbors();
    	/*for(Map.Entry<DCNode, List<DCNode>> entry : sourceReceiverMap.entrySet()){*/ Map.Entry<DCNode, List<DCNode>> entry = sourceReceiverMap.entrySet().iterator().next();
    		DCNode initFromNode = entry.getKey();
    		initFromNode.setMark(0);
    		System.out.println("findShortestPathesForReceivers for source " + initFromNode.toString());
    		if(!entry.getValue().isEmpty()){
    			for(DCNode receiver : entry.getValue()){
//        			recursiveCall(initFromNode, receiver, null, 0);
        			
        			List<String> ids = new ArrayList<>();
//        			recursiveCall1(initFromNode, receiver, null, 0, ids);
        			bfsCall(initFromNode, receiver);
        		}
//        		break;
    		}
//    	}
    	
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
            	toNode.setMark(mark);
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
    
//    private static void recursiveCall2(DCNode fromNode, DCNode toNode, List<String> visitedNodeIds){
//    	for(DCNode node : fromNode.getNeighbors()){
//    		if(node.getId().equals(toNode.getId())){
//    			System.out.println("path was found");
//    		}else{
//    			visitedNodeIds.add(node.getId());
//    		}
//    	}
//    	
//    }
    
    private static void bfsCall(DCNode fromNode, DCNode toNode){
    	List<String> visitedNodeIds = new ArrayList<>();
    	Queue<DCNode> queue = new LinkedList<>();
    	System.out.println("BFS start from " + fromNode.getId() + " to " + toNode.getId());
    	visitedNodeIds.add(fromNode.getId());
    	queue.add(fromNode);
    	fromNode.setMark(0);
    	
    	while(!queue.isEmpty()){
    		DCNode node1 = queue.poll();
    		System.out.println("BFS removing from queue node1 " + node1.getId());
    		if(node1.equals(toNode)) {
    			System.out.println("Destination node " + toNode.getId() + " reached with weight = " + node1.getMark());
    		}
    		System.out.println("BFS visited nodes " + visitedNodeIds);
    		DCNode node11 = nodes.get(nodes.indexOf(node1));
    		node11.setMark(node1.getMark());
    		System.out.println("BFS node1 weight = " + node1.getMark() + " , neigboors " + node1.getNeighbors());
    		System.out.println("BFS node11 weight = " + node11.getMark() + " , neigboors " + node11.getNeighbors());
    		if(node11.getMark() > 100000){
    			node11.setMark(0);
    		}
    		for(DCNode node2 : node11.getNeighbors()){
    			if(!visitedNodeIds.contains(node2.getId())){
    				visitedNodeIds.add(node2.getId());
    				System.out.println("BFS add to queue node2 " + node2.getId());
    				System.out.println("BFS weight for prev node =" + node11.getMark());
    				System.out.println("BFS weignt between edges = " + DCNode.getWeightForNodes(node2, node11, edges));
    				int mark = node11.getMark() + DCNode.getWeightForNodes(node2, node11, edges);
    				System.out.println("BFS weignt for current node = " + mark);
    				node2.setMark(mark);
    				queue.add(node2);
    			}
    		}
    		
    	}
    		
    }
    
    //deprecated
    private static void recursiveCall(DCNode fromNode, DCNode toNode, DCNode previousNode, int mark){
    	System.out.println("recursiveCall calling with params - fromNode=" + fromNode.getId() + " , toNode=" + toNode.getId()
    			+ " , previousNode=" + (previousNode == null ? "null" : previousNode.getId()) + " , mark=" + mark);
    	try{
    		DCNode testNode = nodes.get(nodes.indexOf(fromNode));
        	if(testNode.getNeighbors().contains(toNode)){
            	mark = mark + DCNode.getWeightForNodes(fromNode, toNode, edges);
            	toNode.setMark(mark);
            	System.out.println("For node " + toNode.toString() + "  , mark = " + mark);
            } else {
            	if(/*previousNode != null && */true){
            		System.out.println("recursiveCall inside else");
            		System.out.println("fromNode.getNeighbors() " + fromNode.getNeighbors());
//            		DCNode testNode = nodes.get(nodes.indexOf(fromNode));
            		System.out.println("testNode.getNeighbors() " + testNode.getNeighbors());
            		for(DCNode neighbor : testNode.getNeighbors()){
//            			System.out.println("current neighbor " + neighbor.getId());
            			if(neighbor != previousNode){
            				System.out.println("neighbor " + neighbor.toString());
                            mark = mark + DCNode.getWeightForNodes(fromNode, neighbor, edges);
                            System.out.println("mark = " + mark);
                            previousNode = fromNode;
                            fromNode = neighbor;
//                            System.out.println("recursion executes with parameters - fromNode=" + fromNode + " , toNode=" + toNode + " ,previousNode=" + previousNode + " ,mark=" + mark);
                            recursiveCall(fromNode, toNode, previousNode, mark);
//                            Thread.sleep(1000);
            			} else {
            				System.out.println("wrong path");
            				return;
            			}
            			
                    }
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
		// ���� ������������ ������ ������������� ����� , �.�. ��� ������� ������ ������ 3 ����� � ���������� � ����������
		
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
