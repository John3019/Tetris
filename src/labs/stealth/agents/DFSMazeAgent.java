package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;


import java.util.HashSet;   // will need for dfs
import java.util.Stack;     // will need for dfs
import java.util.Set;       // will need for dfs


// JAVA PROJECT IMPORTS


public class DFSMazeAgent
    extends MazeAgent
{

    private Path currentPlan;

    public DFSMazeAgent(int playerNum)
    {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {

        //here we will use a stack instead of a que to keep track of paths
        Stack<Path> stack = new Stack<>();
        Set<Vertex> visited = new HashSet<>();

        stack.push(new Path(src)); // Initial path with source vertex src

        while (!stack.isEmpty()) {
            Path currentPath = stack.pop();
            Vertex currentVertex = currentPath.getDestination();

            //if the current vertex is the goal, then we have already found 
            System.out.println(currentVertex);

            // if (currentVertex.equals(goal)) {
            //     currentPlan = currentPath;
            //     System.out.println(currentPath);
            //     return currentPath; // Return the path if we found the goal
            // }

            if (!visited.contains(currentVertex)) {
                visited.add(currentVertex);

                for (Vertex neighbor : getNeighbors(currentVertex, state)) {
                    if (neighbor.equals(goal)) {
                        currentPlan = currentPath;
                        //System.out.println(currentPath);
                        return currentPath; // Return the path if we found the goal
                    }
                    if (!visited.contains(neighbor)) {
                        Path newPath = new Path(neighbor, 1f, currentPath);
                        stack.push(newPath);
                    }

                   
                }
            }
        }
        return null;
    }

    // helper function to returna set of all vertecies above, below, to each side and from each diagonal of the current vertex
    private Set<Vertex> getNeighbors(Vertex vertex, StateView state) {
        Set<Vertex> neighbors = new HashSet<>();

        // Distance threshhold to ensure were close to the current vertex
        int proximityThreshold = 2;

        int currentX = vertex.getXCoordinate();
        int currentY = vertex.getYCoordinate();

        // Iterate over surrounding positions
        for (int x = currentX - 1; x <= currentX + 1; x++) {
            for (int y = currentY - 1; y <= currentY + 1; y++) {
                if (state.inBounds(x, y)) {
                    Vertex neighbor = new Vertex(x, y);
                    // Check if the neighbor is close to the current vertex and not equal to it
                    //if (Math.abs(neighbor.getXCoordinate() - currentX) + Math.abs(neighbor.getYCoordinate() - currentY) <= proximityThreshold
                    //       && !neighbor.equals(vertex)
                    //            && !isResourceOccupied(neighbor, state)
                    //           && !DiagonalBlocked(vertex, neighbor, state))
                    if (!state.isResourceAt(x, y) && (!state.isUnitAt(x, y) || state.unitAt(x, y) == this.getEnemyTargetUnitID()))
                    {
                        neighbors.add(neighbor);
                        //System.out.println(neighbor);
                    }
                }
            }
        }

        return neighbors;
    }

    private boolean DiagonalBlocked(Vertex vertex1, Vertex vertex2, StateView state){
        int deltaX = (vertex2.getXCoordinate() - vertex1.getXCoordinate());
        int deltaY = (vertex2.getYCoordinate() - vertex1.getYCoordinate());
        if (deltaX == 1 && deltaY == 1){
            Vertex above = new Vertex(vertex1.getXCoordinate(), vertex1.getYCoordinate() + 1);
            Vertex right = new Vertex(vertex1.getXCoordinate() + 1, vertex1.getYCoordinate());
            if (isResourceOccupied(above, state) && isResourceOccupied(right, state)){
                return true;
            }

        } else if (deltaX == -1 && deltaY == 1){
            Vertex above = new Vertex(vertex1.getXCoordinate(), vertex1.getYCoordinate() + 1);
            Vertex left = new Vertex(vertex1.getXCoordinate() - 1, vertex1.getYCoordinate());
            if (isResourceOccupied(above, state) && isResourceOccupied(left, state)){
                return true;
            }

        }else if (deltaX == 1 && deltaY == -1){
            Vertex below = new Vertex(vertex1.getXCoordinate(), vertex1.getYCoordinate() - 1);
            Vertex right = new Vertex(vertex1.getXCoordinate() + 1, vertex1.getYCoordinate());
            if (isResourceOccupied(below, state) && isResourceOccupied(right, state)){
                return true;
            }

        }else if (deltaX == -1 && deltaY == -1){
            Vertex below = new Vertex(vertex1.getXCoordinate(), vertex1.getYCoordinate() - 1);
            Vertex left = new Vertex(vertex1.getXCoordinate() - 1, vertex1.getYCoordinate());
            if (isResourceOccupied(below, state) && isResourceOccupied(left, state)){
                return true;
            }

        }

        return false;
    }

    

    // Helper function to check if a given vertex is occupied by a resource
private boolean isResourceOccupied(Vertex vertex, StateView state) {
    return state.isResourceAt(vertex.getXCoordinate(), vertex.getYCoordinate());
}

    public static boolean pathContains(Path path, Vertex vertex) {
        while (path != null) {
            if (path.getDestination().equals(vertex)) {
                return true;
            }
            path = path.getParentPath();
        }
        return false;
    }

    @Override
    public boolean shouldReplacePlan(StateView state)
    {    
        // The code does not run as I ran out of time to implement it, but the idea is there
        
    
        // if (currentPlan != null) {
        //     // Iterate over all unit IDs to see if anything blocks our path
        //     for (Integer unitId : state.getAllUnitIds()) {
        //         if (unitId != playernum){
        //             UnitView unitView = state.getUnit(unitId);
        //              // Check if the current path is blocked
        //             Vertex unitPosition = new Vertex(unitView.getXPosition(), unitView.getYPosition());
        //             System.out.println("Position" + unitPosition);
        //             if (pathContains(currentPlan, unitPosition)) {
        //                 return true; 
        //             }
        //         }
        //     }
        // }

        //getOtherEnemyUnitIDs()

        //use getCurrentPath
    
    
        return false;
    }

}
