package src.pas.battleship.agents;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.game.EnemyBoard;
import edu.bu.battleship.utils.Coordinate;
import edu.bu.battleship.game.Constants.Rendering.Board;
import edu.bu.battleship.game.Constants;
import edu.bu.battleship.game.ships.Ship;
import edu.bu.battleship.game.ships.Ship.ShipType;



public class ProbabilisticAgent
    extends Agent
{

    public ProbabilisticAgent(String name)
    {
        super(name);
        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    @Override
    public Coordinate makeMove(final GameView game)
    {
        // int height = Board.BOARD_HEIGHT;
        // int width = Board.BOARD_WIDTH;
        Constants constants = game.getGameConstants();
        int height = constants.getNumRows();
        int width = constants.getNumCols();

        int boardSize = height*width;
        EnemyBoard.Outcome[][] enemyBoardOutcomes = game.getEnemyBoardView();
        Map<Coordinate, Double> cellProbabilities = new HashMap<>();
        // Initialize all cell probabilities to zero
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Coordinate cell = new Coordinate(row, col);
                cellProbabilities.put(cell, 0.0);
                List<Coordinate> neighbors = getNeighbors(cell);
                String ac = "AIRCRAFT_CARRIER";
                String b = "BATTLESHIP";
                String d = "DESTROYER";
                String pb = "PATROL_BOAT";
                String s = "SUBMARINE";
                double prob = getHitProb(cell, enemyBoardOutcomes, game, ac, 5) + getHitProb(cell, enemyBoardOutcomes, game, b, 4) + getHitProb(cell, enemyBoardOutcomes, game, d, 3) + getHitProb(cell, enemyBoardOutcomes, game, pb, 3) + getHitProb(cell, enemyBoardOutcomes, game, s, 2);
                cellProbabilities.put(cell, prob);
                
                
                    // for (Coordinate neighbor : neighbors){
                    //     //System.out.println("BAng");
                    //     if (game.isInBounds(neighbor)){
                    //         //if (enemyBoardOutcomes[neighbor.getXCoordinate()][neighbor.getYCoordinate()] == "HIT"){
                    //         if ("HIT".equals(enemyBoardOutcomes[neighbor.getXCoordinate()][neighbor.getYCoordinate()].toString())) {
                    //             //System.out.println("Neighbor found");
                    //             cellProbabilities.put(cell, 1.0);

                    //         }

                    //     }
                    // }
                
                

            }
        }

        //create hashmap to calculate probability
        //for every coordinate in the game
        // calculate probability and store it in said hashmap
            // Find the best move based on highest probability
        Coordinate bestMove = null;
        double highestProbability = 0.0;
        for (Map.Entry<Coordinate, Double> entry : cellProbabilities.entrySet()) {
            if (entry.getValue() > highestProbability && game.isInBounds(entry.getKey()) && "UNKNOWN".equals(enemyBoardOutcomes[entry.getKey().getXCoordinate()][entry.getKey().getYCoordinate()].toString())) {
                highestProbability = entry.getValue();
                bestMove = entry.getKey();
                
            }
            if (highestProbability == 0.0) {
                boolean cellFound = false;
                while (cellFound == false){
                    Random rand = new Random();
                    int x = rand.nextInt(width);
                    int y = rand.nextInt(height);
                    Coordinate random = new Coordinate(x, y);
                    if( game.isInBounds(random) && "UNKNOWN".equals(enemyBoardOutcomes[x][y].toString())){
                        //System.out.println("found");
                        bestMove = random;
                        break;
                    }

                }
            }


            //if we have no best move, pick a random cell
            // if (highestProbability == 0.0){
            //     List<Coordinate> unknownCells = new ArrayList<>();
            //     for (Coordinate coordinate : cellProbabilities.keySet()) {
            //         if (game.isInBounds(coordinate) && "UNKNOWN".equals(enemyBoardOutcomes[coordinate.getXCoordinate()][coordinate.getYCoordinate()].toString())) {
            //             unknownCells.add(coordinate);
            //         }
            //     }

            //     // If there are unknown cells, randomly pick one
            //     if (!unknownCells.isEmpty()) {
            //         Random random = new Random();
            //         bestMove = unknownCells.get(random.nextInt(unknownCells.size()));
            //     }
            // }
        }

        // int x = bestMove.getXCoordinate();
        // int y = bestMove.getYCoordinate();
        // System.out.println("MoveMade");
        // System.out.println(x);
        // System.out.println(y);
        return bestMove;
    }
    
    private List<Coordinate> getNeighbors(Coordinate cell) {
        int x = cell.getXCoordinate();
        int y = cell.getYCoordinate();

        List<Coordinate> neighbors = new ArrayList<>();
        neighbors.add(new Coordinate(x - 1, y)); 
        neighbors.add(new Coordinate(x + 1, y)); 
        neighbors.add(new Coordinate(x, y - 1)); 
        neighbors.add(new Coordinate(x, y + 1)); 
        return neighbors;
    }

    private double getHitProb(Coordinate cell, EnemyBoard.Outcome[][] enemyBoardOutcomes, GameView game, String shipType, int shipLength) {
        Map<Ship.ShipType, Integer> enemyShipTypeToNumRemaining = game.getEnemyShipTypeToNumRemaining();
        //System.out.println(shipType);
        Ship.ShipType Type = Ship.ShipType.valueOf(shipType);
        //System.out.println(Type);
        int numRemaining = enemyShipTypeToNumRemaining.get(Type);
        if(numRemaining == 0){
            return 0;
        }

        int hitCount = 0;
        int ProbUp = 0;
        int ProbDown = 0;
        int ProbLeft = 0;
        int ProbRight = 0;
        List<Coordinate> neighbors = getNeighbors(cell);
        for (Coordinate neighbor : neighbors){
            if (game.isInBounds(neighbor)){
                if ("HIT".equals(enemyBoardOutcomes[neighbor.getXCoordinate()][neighbor.getYCoordinate()].toString())) {
                    // Determine relative position of the neighbor
                    if (neighbor.getXCoordinate() < cell.getXCoordinate()) {
                        ProbLeft++; // Neighbor is left of the cell
                    } else if (neighbor.getXCoordinate() > cell.getXCoordinate()) {
                        ProbRight++; // Neighbor is right of the cell
                    }
                    if (neighbor.getYCoordinate() < cell.getYCoordinate()) {
                        ProbUp++; // Neighbor is above the cell
                    } else if (neighbor.getYCoordinate() > cell.getYCoordinate()) {
                        ProbDown++; // Neighbor is below the cell
                    }

                }

            }
        }

        int x = cell.getXCoordinate();
        int y = cell.getYCoordinate();
        if (ProbRight > 1) {
            for (int i = 2; i <= shipLength; i ++){
                Coordinate lookahead = new Coordinate(x, y + i);
                if (game.isInBounds(lookahead) && "HIT".equals(enemyBoardOutcomes[lookahead.getXCoordinate()][lookahead.getYCoordinate()].toString())){
                    ProbRight++;
                }
            }

        } else if (ProbLeft > 1) {
            for (int i = 2; i <= shipLength; i ++){
                Coordinate lookahead = new Coordinate(x, y - i);
                if (game.isInBounds(lookahead) && "HIT".equals(enemyBoardOutcomes[lookahead.getXCoordinate()][lookahead.getYCoordinate()].toString())){
                    ProbLeft++;
                }
            }

        } else if (ProbDown > 1) {
            for (int i = 2; i <= shipLength; i ++){
                Coordinate lookahead = new Coordinate(x - i, y);
                if (game.isInBounds(lookahead) && "HIT".equals(enemyBoardOutcomes[lookahead.getXCoordinate()][lookahead.getYCoordinate()].toString())){
                    ProbDown++;
                }
            }

        } else if (ProbUp > 1) {
            for (int i = 2; i <= shipLength; i ++){
                Coordinate lookahead = new Coordinate(x + 1, y);
                if (game.isInBounds(lookahead) && "HIT".equals(enemyBoardOutcomes[lookahead.getXCoordinate()][lookahead.getYCoordinate()].toString())){
                    ProbUp++;
                }
            }

        }

        int probSum = ProbRight + ProbLeft + ProbUp + ProbDown;
        double probability = probSum;
    
        
        
        return probability;
    }




    @Override
    public void afterGameEnds(final GameView game) {}

}
