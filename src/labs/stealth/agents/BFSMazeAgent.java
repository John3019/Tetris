package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


// JAVA PROJECT IMPORTS


public class BFSMazeAgent extends MazeAgent {
    //this is so we can acces the plan later on
    private Path currentPlan;

    public BFSMazeAgent(int playerNum) {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
                       Vertex goal,
                       StateView state)
    {
        Queue<Path> queue = new LinkedList<>();
        Set<Vertex> visited = new HashSet<>();
    
        queue.add(new Path(src)); // Initial path with source vertex src
        visited.add(src);

        //System.out.println(state.getAllResourceIds());
    
        while (!queue.isEmpty()) {
            Path currentPath = queue.poll();
            Vertex currentVertex = currentPath.getDestination();
 
            // Explore all neighbors of the current vertex
            for (Vertex neighbor : getNeighbors(currentVertex, state)) {
                if (neighbor.equals(goal)) {
                    currentPlan = currentPath;
                    //System.out.println(currentPath);
                    return currentPath; // Return the path if we found the goal
                }
                if (!visited.contains(neighbor)) {
                    //if we havent visited the neighbor, add it to the path and add the path to the queue
                    visited.add(neighbor);
                    Path newPath = new Path(neighbor, 1f, currentPath); 
                    queue.add(newPath); 
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
            if (x >= 0 && x < state.getXExtent() && y >= 0 && y < state.getYExtent()) {
                Vertex neighbor = new Vertex(x, y);

                // Check if the neighbor is close to the current vertex and not equal to it
                if (Math.abs(neighbor.getXCoordinate() - currentX) + Math.abs(neighbor.getYCoordinate() - currentY) <= proximityThreshold
                        && !neighbor.equals(vertex)
                       && !isResourceOccupied(neighbor, state)) {
                    neighbors.add(neighbor);
                    //System.out.println(neighbor);
                }
            }
        }
    }

    return neighbors;
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
    


    return false;
}

}

