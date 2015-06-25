

# Introduction #

Recently, I work on a warehouse optimization project. On interesting problem is to select an optimal pick tour for the pick agent, which is very much like a simplified path planning problem in a game world. I used the popular [A\* search algorithm](http://en.wikipedia.org/wiki/A*_search_algorithm) and like to share with you about my Java implementation of the A`*` algorithm.

# Java Implementation #

## Data Structure ##

To use the A`*` algorithm, I need to define a Graph data structure as follows.

### Position ###

```
public class Position {
    private double x;

    private double y;
}
```

### Node ###

A node is a vertex of the graph.

```
public class Node {

    protected String id;
}
```

### Edge ###

```
public class Edge {

    protected String from;

    protected String to;

}
```

### Adjacency ###

Adjacency is used to represent the neighbours of the current node.

```
public class Adjacency<N extends Node>{
    protected N node;
    protected Set<N> neighbors;
}
```

### Graph ###

A graph can be defined as follows. Note, the edgeList and adjacency may be redundant and you can use one of them in your application.

```
public class Graph<N extends Node, E extends Edge> {

    protected List<N> nodeList;
    
    protected List<E> edgeList;
    
    //Index for fast access
    private Map<String, Adjacency<N>> adjacency;

    //directed graph or not
    protected boolean diGraph;
}
```

## Navigation Graph ##

In the warehouse application, we need to build a navigation graph to help us to construct the path for the pick agent. In a tiled environment, you can first use the [Flood Fill algorithm](http://en.wikipedia.org/wiki/Flood_fill) to search for the connection in the application world. Then build a navigation graph based on it. For our warehouse application, we can create outlines for aisles to obtain a navigation graph.

The navigation graph is defined as follows.

```
public class NavNode extends Node{

    protected Position position;

    protected List<String> extraData;

}

public class NavEdge extends Edge {

    protected double cost;
}

public class NavGraph extends Graph<NavNode, NavEdge>{

    public void addConnection(String firstId, String secondId){
        NavNode node1 = this.getNode(firstId);
        NavNode node2 = this.getNode(secondId);
        if(node1 != null && node2 != null){
            double cost = this.calcManhattanDistance(node1, node2);
            NavEdge edge1 = new NavEdge(firstId, secondId, cost);
            NavEdge edge2 = new NavEdge(secondId, firstId, cost);
            this.addEdge(edge1);
            this.addEdge(edge2);
        }
    }

    public void removeConnection(String firstId, String secondId){
        NavEdge edge1 = new NavEdge(firstId, secondId);
        NavEdge edge2 = new NavEdge(secondId, firstId);
        this.removeEdge(edge1);
        this.removeEdge(edge2);
    }

    public double calcManhattanDistance(NavNode a, NavNode b){
        return abs(a.getPosition().getX() - b.getPosition().getX())
                + abs(a.getPosition().getY() - b.getPosition().getY());
    }
}
```

In our application, we choose to use the Manhatten distance. To load a predefined navigation from a file, we use a loader class for this purpose.

```
public class NavGraphLoader {

    public NavGraphData load(String filePath) {
        try {
            String json = this.readFileAsString(filePath);
            JSONReader reader = new JSONReader();
            Map map = (Map) reader.read(json);
            NavGraphData data = new NavGraphData();
            data.fromJSON(map);

            return data;
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file " + filePath);
        }
    }

    String readFileAsString(String filePath) throws java.io.IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(filePath)));
        StringBuffer sb = new StringBuffer(4096);

        String line = reader.readLine();
        while(line != null){
            sb.append(line);
            line = reader.readLine();
        }

        reader.close();

        return sb.toString();
    }
}
```

where the file is in JSON format, for example.

```

{"entries": [{"position":{"y":8.0,"x":8.0},"id":"A","linkWith":["B","C"],"extraData":["E01","E03","E05"]},
             {"position":{"y":6.0,"x":8.0},"id":"B","linkWith":["A","C"],"extraData":["E02","E04","E06"]},
             {"position":{"y":6.0,"x":6.0},"id":"C","linkWith":["A","B"],"extraData":["E01A"]}]
}
```

## World Map ##

If the world, i.e., the area for path planning is not very big, navigation map is not really necessary. We can create a world map instead.

A world map is actually a matrix to hold all positions as follows.

```
public class WorldMap {
    int rowNum;
    int columnNum;

    String[][] map;
    Map<String, MatrixPosition> index;

    public WorldMap(int rowNum, int columnNum) {
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        map = new String[rowNum][columnNum];
        index = new HashMap<String, MatrixPosition>();
    }

...
}
```

Where MatrixPosition is defined as follows.

```
public class MatrixPosition {

    private int row;
    private int column;
}

```

Before use the world map, we need a map loader.

```
public interface WorldMapLoader {
    
    WorldMap load(String filePath);
}
```

For example, I used a CSV map loader for our project.

```
public class CsvWorldMapLoader implements WorldMapLoader {
    protected final static String FIELD_DELIMITER = ",";
    protected final static String ESCAPE_START = "\\Q";
    protected final static String ESCAPE_END = "\\E";
    protected final static String QUOTA = "\"";

    protected final static String ERROR_DATA_READ_EXCEPTION = "Read data exception";
    private final static Log log = LogFactory.getLog(CsvWorldMapLoader.class);

    public WorldMap load(String filePath) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(filePath)));
        List<RowVector> data = new ArrayList<RowVector>();
        try {
            String line = reader.readLine();
            while (line != null) {
                String escapedLine = ESCAPE_START + line + ESCAPE_END;

                String[] fields = escapedLine.split(FIELD_DELIMITER);
                //remove \Q for the first record
                if (fields[0].startsWith(ESCAPE_START))
                    fields[0] = fields[0].substring(2);
                //remove \E for the last record
                int fl = fields.length;
                if (fields[fl - 1].endsWith(ESCAPE_END))
                    fields[fl - 1] = fields[fl - 1].substring(0, fields[fl - 1].length() - 2);

                RowVector row = new RowVector();

                List<String> purified = new ArrayList<String>();
                //skip the first element since it is a width
                for(int i=1; i<fields.length; i++){
                    String key = fields[i];
                    if(key.startsWith(QUOTA)){
                        key = key.substring(1);
                    }
                    if(key.endsWith(QUOTA)){
                        key = key.substring(0, key.length() - 1);
                    }
                    purified.add(key);
                }
                row.setVector(purified);

                data.add(row);
                line = reader.readLine();
            }

        } catch (IOException e) {
            throw new CoreException(ERROR_DATA_READ_EXCEPTION + " " + e.getMessage());
        }finally{
            try {
                reader.close();
            } catch (IOException e) {
                log.error("Failed to close file " + filePath + ": " + e.getMessage());
            }
        }

        return WorldMapBuilder.build(data);
    }
}
```

The A`*` Search Algorithm used for the world map is a bit different from the one used for the
navigation map. The difference is in the way the algorithm to construct neighboring nodes. For
a navigation graph, you can only select neighboring nodes defined in the graph, but for a world
map, you can select any neighoring positions that are accessible from the current node.

## A`*` Search Algorithm ##

Before work on the A`*` algorithm, we need to create a wrap node as follows.

```
public class AStarNode {

    private NavNode node;

    //used to construct the path after the search is done
    private AStarNode cameFrom;

    // Distance from source along optimal path
    private double g;

    // Heuristic estimate of distance from the current node to the target node
    private double h;
}
```

and a [comparator](http://java.sun.com/j2se/1.5.0/docs/api/java/util/Comparator.html) for Java [priority queue](http://java.sun.com/j2se/1.5.0/docs/api/java/util/PriorityQueue.html).

```
public class AStarNodeComparator implements Comparator<AStarNode> {

    public int compare(AStarNode first, AStarNode second) {
        if(first.getF() < second.getF()){
            return -1;
        }else if(first.getF() > second.getF()){
            return 1;
        }else{
            return 0;
        }
    }
}
```

The algorithm is not complicated as shown in the following class. For more algorithm details, please refer to [A`\*` search algorithm](http://en.wikipedia.org/wiki/A*_search_algorithm).

```
public class AStarAlgorithm {
   private static Log log = LogFactory.getLog(AStarAlgorithm.class);

   public static List<NavNode> search(NavGraph graph, NavNode source, NavNode target) {
        Map<String, AStarNode> openSet = new HashMap<String, AStarNode>();
        PriorityQueue<AStarNode> pQueue = new PriorityQueue(20, new AStarNodeComparator());
        Map<String, AStarNode> closeSet = new HashMap<String, AStarNode>();
        AStarNode start = new AStarNode(source, 0, graph.calcManhattanDistance(source, target));
        openSet.put(source.getId(), start);
        pQueue.add(start);

        AStarNode goal = null;
        while(openSet.size() > 0){
            AStarNode x = pQueue.poll();
            openSet.remove(x.getId());
            if(x.getId().equals(target.getId())){
                //found
                if(log.isDebugEnabled()){
                    log.debug("Found target node " + x.getId());
                }
                goal = x;
                break;
            }else{
                if(log.isDebugEnabled()){
                    log.debug("Search for node " + x.getId());
                }
                closeSet.put(x.getId(), x);
                Set<NavNode> neighbors = graph.getAdjacentNodes(x.getId());
                for (NavNode neighbor : neighbors) {
                    AStarNode visited = closeSet.get(neighbor.getId());
                    if (visited == null) {
                        double g = x.getG() + graph.calcManhattanDistance(x.getNode(), neighbor);
                        AStarNode n = openSet.get(neighbor.getId());

                        if (n == null) {
                            //not in the open set
                            n = new AStarNode(neighbor, g, graph.calcManhattanDistance(neighbor, target));
                            n.setCameFrom(x);
                            openSet.put(neighbor.getId(), n);
                            pQueue.add(n);
                        } else if (g < n.getG()) {
                            //Have a better route to the current node, change its parent
                            n.setCameFrom(x);
                            n.setG(g);
                            n.setH(graph.calcManhattanDistance(neighbor, target));
                        }
                    }
                }
            }
        }

        //after found the target, start to construct the path 
        if(goal != null){
            Stack<NavNode> stack = new Stack<NavNode>();
            List<NavNode> list = new ArrayList<NavNode>();
            stack.push(goal.getNode());
            AStarNode parent = goal.getCameFrom();
            while(parent != null){
                stack.push(parent.getNode());
                parent = parent.getCameFrom();
            }
            if (log.isDebugEnabled()) {
                log.debug("Constructing search path: ");
            }
            while(stack.size() > 0){
                if (log.isDebugEnabled()) {
                    log.debug("\t" + stack.peek().getId());
                }
                list.add(stack.pop());
            }
            return list;
        }
        
        return null;  
    }
}
```

# Resources #

  * [A\* search algorithm](http://en.wikipedia.org/wiki/A*_search_algorithm)
  * [Flood Fill algorithm](http://en.wikipedia.org/wiki/Flood_fill)
  * [ProgrammingGame AI by Example (book)](http://www.amazon.com/Programming-Game-Example-Mat-Buckland/dp/1556220782)
  * [TelluriumSource on Twitter](http://twitter.com/TelluriumSource)