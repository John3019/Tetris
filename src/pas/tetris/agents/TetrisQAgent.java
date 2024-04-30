package src.pas.tetris.agents;


import java.util.Comparator;
// SYSTEM IMPORTS
import java.util.Iterator;
import java.util.List;
import java.util.Random;


// JAVA PROJECT IMPORTS
import edu.bu.tetris.agents.QAgent;
import edu.bu.tetris.agents.TrainerAgent.GameCounter;
import edu.bu.tetris.game.Board;
import edu.bu.tetris.game.Game.GameView;
import edu.bu.tetris.game.Game;
import edu.bu.tetris.game.minos.Mino;
import edu.bu.tetris.linalg.Matrix;
import edu.bu.tetris.nn.Model;
import edu.bu.tetris.nn.LossFunction;
import edu.bu.tetris.nn.Optimizer;
import edu.bu.tetris.nn.models.Sequential;
import edu.bu.tetris.nn.layers.Dense; // fully connected layer
import edu.bu.tetris.nn.layers.ReLU;  // some activations (below too)
import edu.bu.tetris.nn.layers.Tanh;
import edu.bu.tetris.nn.layers.Sigmoid;
import edu.bu.tetris.training.data.Dataset;
import edu.bu.tetris.utils.Pair;
import edu.bu.tetris.utils.Coordinate;



public class TetrisQAgent
    extends QAgent
{

    public static final double EXPLORATION_PROB = 0.05;

    private Random random;

    public TetrisQAgent(String name)
    {
        super(name);
        this.random = new Random(12345); // optional to have a seed
    }

    public Random getRandom() { return this.random; }

    @Override
    public Model initQFunction()
    {
        // build a single-hidden-layer feedforward network
        // this example will create a 3-layer neural network (1 hidden layer)
        // in this example, the input to the neural network is the
        // image of the board unrolled into a giant vector
        // final int numPixelsInImage = Board.NUM_ROWS * Board.NUM_COLS;
        // final int hiddenDim = 2 * numPixelsInImage;
        // final int outDim = 1;

        // Use our new dimensions
        final int inputDim = 4; // Height ratio, bumpiness, numHoles, linesCleared

        // Define the dimensions of the hidden layer and output layer
        final int hiddenDim = 2 * inputDim; // Adjust as needed
        final int outDim = 1;

        Sequential qFunction = new Sequential();
        // qFunction.add(new Dense(numPixelsInImage, hiddenDim));
        // qFunction.add(new Tanh());
        // qFunction.add(new Dense(hiddenDim, outDim));

        // Add the input layer
        qFunction.add(new Dense(inputDim, hiddenDim));
        qFunction.add(new Tanh());
        qFunction.add(new Dense(hiddenDim, outDim));

        return qFunction;
    }

    /**
        This function is for you to figure out what your features
        are. This should end up being a single row-vector, and the
        dimensions should be what your qfunction is expecting.
        One thing we can do is get the grayscale image
        where squares in the image are 0.0 if unoccupied, 0.5 if
        there is a "background" square (i.e. that square is occupied
        but it is not the current piece being placed), and 1.0 for
        any squares that the current piece is being considered for.
        
        We can then flatten this image to get a row-vector, but we
        can do more than this! Try to be creative: how can you measure the
        "state" of the game without relying on the pixels? If you were given
        a tetris game midway through play, what properties would you look for?
     */
    @Override
    public Matrix getQFunctionInput(final GameView game,
                                    final Mino potentialAction)
    {
        Matrix flattenedImage = null;
        Matrix flattenedInput = null;
        //Board originalImage = null;
        //Matrix originalImage = null;
        Matrix inputMatrix = Matrix.zeros(1, 4);
        try
        {
        // Step 1: Obtain grayscale image representation of the game board
        // flattenedImage = game.getGrayscaleImage(potentialAction).flatten();
        // originalImage = game.getGrayscaleImage(potentialAction);
        Board originalImage = new Board(game.getBoard());
        originalImage.addMino(potentialAction);
        // Step 2: Calculate the height of the current board divided by the maximum height of the board
        double maxHeight = Board.NUM_ROWS - 2;
        double heightRatio = getHeight(originalImage) / maxHeight;
        //System.out.print(getHeight(originalImage));

        // Step 3: Compute the bumpiness of the top layer
        double bumpiness = calculateBumpiness(originalImage) / maxHeight;

        // Step 4: Count the number of holes present on the board divided by the total number of squares
        double numHoles = countHoles(originalImage) / (double)(Board.NUM_ROWS * Board.NUM_COLS);

        // Step 5: Determine the number of lines cleared if the move is chosen
        double linesCleared = getLinesCleared(originalImage) / maxHeight;
        // Construct the input matrix

        inputMatrix.set(0, 0, heightRatio);
        inputMatrix.set(0, 1, bumpiness);
        inputMatrix.set(0, 2, numHoles);
        inputMatrix.set(0, 3, linesCleared);

        flattenedInput = inputMatrix.flatten();
        } catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        //System.out.print(flattenedInput);
        return flattenedInput;
    }

    private int getHeight(Board originalImage) {
        int Max_Height = 0;
        int rows = Board.NUM_ROWS;
        int cols = Board.NUM_COLS;
    
        for (int i = 0; i < cols; i++) {
            for (int j = 2; j < rows; j++) {
                if (originalImage.isCoordinateOccupied(i, j)) {
                    int height = rows - j;
                    Max_Height = Math.max(Max_Height, height);

                    break;
                }
            }
        }
        return Max_Height;
    }

    // Helper method to calculate the bumpiness of the top layer
    private int calculateBumpiness(Board originalImage) {
        int rows = originalImage.NUM_ROWS;
        int cols = originalImage.NUM_COLS;
        int[] columnHeights = new int[cols]; // Array to store the heights of each column
        int bumpiness = 0;

        // Calculate column heights
        for (int col = 0; col < cols; col++) {
            for (int row = 2; row < rows; row++) {
                if (originalImage.isCoordinateOccupied(col, row)) {
                    columnHeights[col] = rows - row;
                    break;
                }
            }
        }

        // Calculate bumpiness
        for (int i = 0; i < cols - 1; i++) {
            bumpiness += Math.abs(columnHeights[i] - columnHeights[i + 1]);
        }

        return bumpiness;
    }

    // Helper method to count the number of holes present on the board
    private int countHoles(Board originalImage) {
        int rows = originalImage.NUM_ROWS;
        int cols = originalImage.NUM_COLS;
        int holes = 0;

        for (int col = 0; col < cols; col++) {
            boolean blockFound = false;
            for (int row = 2; row < rows; row++) {
                if (originalImage.isCoordinateOccupied(col, row)) {
                    blockFound = true; // Found a block
                } else if (blockFound) {
                    // If there's a block above, count this as a hole
                    holes++;
                }
            }
        }

        return holes;
    }

    // Helper method to get the number of lines cleared
    private int getLinesCleared(Board originalImage) {
        int rows = originalImage.NUM_ROWS;
        int cols = originalImage.NUM_COLS;
        int linesCleared = 0;

        for (int row = 2; row < rows; row++) {
            boolean rowFilled = true;
            for (int col = 0; col < cols; col++) {
                if (!originalImage.isCoordinateOccupied(col, row)) {
                    // Empty square found, row is not completely filled
                    rowFilled = false;
                    break;
                }
            }
            if (rowFilled) {
                // All squares in the row are filled, increment linesCleared
                linesCleared++;
            }
        }

        return linesCleared;
    }



    /**
     * This method is used to decide if we should follow our current policy
     * (i.e. our q-function), or if we should ignore it and take a random action
     * (i.e. explore).
     *
     * Remember, as the q-function learns, it will start to predict the same "good" actions
     * over and over again. This can prevent us from discovering new, potentially even
     * better states, which we want to do! So, sometimes we should ignore our policy
     * and explore to gain novel experiences.
     *
     * The current implementation chooses to ignore the current policy around 5% of the time.
     * While this strategy is easy to implement, it often doesn't perform well and is
     * really sensitive to the EXPLORATION_PROB. I would recommend devising your own
     * strategy here.
     */
    @Override
    public boolean shouldExplore(final GameView game, final GameCounter gameCounter) {
        // Exploration parameters
        double baseProb = 0.05;
        double decayRate = 0.005;
        double phaseFactor = Math.exp(-decayRate * gameCounter.getCurrentPhaseIdx());
        double scoreFactor = game.getScoreThisTurn() < 1 ? 0.1 : 0.02;

        // Check if the game state is ripe for exploration
        if (shouldForceExploration(game)) {
            return true; // Force exploration for a specific condition
        }

        // Regular exploration probability calculation
        double dynamicProb = baseProb * phaseFactor + scoreFactor;
        return getRandom().nextDouble() < dynamicProb;
    }

    private boolean shouldForceExploration(GameView game) {
        // Check for specific conditions that warrant exploration
        List<Mino> minos = game.getFinalMinoPositions();

        for (Mino mino : minos) {
            // Simulate adding the current mino to the board
            Board simulatedBoard = new Board(game.getBoard());
            simulatedBoard.addMino(mino);

            // Check for a T-spin if the mino is of type T
            if (mino.getType() == Mino.MinoType.T && checkForTSpin(simulatedBoard, mino)) {
                return true; // T-spin detected, force exploration
            }
            //Check for an L shaped mino piece and if we can spin it
            if (mino.getType() == Mino.MinoType.L && checkLSpin(simulatedBoard, mino)) {
                return true; // L-spin detected
            }

            // Check for clearing four lines (Tetris)
            if (simulatedBoard.clearFullLines().size() >= 4) {
                return true; // Tetris detected, force exploration
            }


        }

        return false; // No specific condition for forced exploration
    }

    public boolean checkForTSpin(Board board, Mino mino) {
        // Check if the given mino results in a T-spin on the board
        if (mino.getType() != Mino.MinoType.T) {
            return false; // Not a T mino, no T-spin
        }

        Coordinate pivot = mino.getPivotBlockCoordinate();
        Mino.Orientation orientation = mino.getOrientation();

        // Offsets for the three corners around the pivot based on the orientation
        int[][] cornerOffsets;
        switch (orientation) {
            case A:
                cornerOffsets = new int[][]{{-1, -1}, {-1, 1}, {1, -1}};
                break;
            case B:
                cornerOffsets = new int[][]{{-1, -1}, {-1, 1}, {1, 1}};
                break;
            case C:
                cornerOffsets = new int[][]{{-1, 1}, {1, -1}, {1, 1}};
                break;
            case D:
                cornerOffsets = new int[][]{{-1, -1}, {1, -1}, {1, 1}};
                break;
            default:
                return false;
        }
        int occupiedCorners = 0;
        for (int[] offset : cornerOffsets) {
            int checkX = pivot.getXCoordinate() + offset[0];
            int checkY = pivot.getYCoordinate() + offset[1];
            if (checkX >= 0 && checkX < Board.NUM_COLS && checkY >= 0 && checkY < Board.NUM_ROWS) {
                if (board.isCoordinateOccupied(new Coordinate(checkX, checkY))) {
                    occupiedCorners++;
                }
            }
        }

        // A valid T-Spin requires at least three corners to be occupied
        return occupiedCorners >= 3;
    }

    private boolean checkLSpin(Board board, Mino mino) {
        if (mino.getType() != Mino.MinoType.L) {
            return false;  // Not an L Mino
        }
    
        Coordinate pivot = mino.getPivotBlockCoordinate();
        Mino.Orientation orientation = mino.getOrientation();
    
        // Coordinates of the three corners around the pivot depending on the orientation
        int[][] cornerOffsets;
        switch (orientation) {
            case A:
                cornerOffsets = new int[][]{{-1, -1}, {-1, 1}, {1, -1}};
                break;
            case B:
                cornerOffsets = new int[][]{{-1, -1}, {-1, 1}, {1, 1}};
                break;
            case C:
                cornerOffsets = new int[][]{{-1, 1}, {1, -1}, {1, 1}};
                break;
            case D:
                cornerOffsets = new int[][]{{-1, -1}, {1, -1}, {1, 1}};
                break;
            default:
                return false;
        }
        int occupiedCorners = 0;
        for (int[] offset : cornerOffsets) {
            int checkX = pivot.getXCoordinate() + offset[0];
            int checkY = pivot.getYCoordinate() + offset[1];
            if (checkX >= 0 && checkX < Board.NUM_COLS && checkY >= 0 && checkY < Board.NUM_ROWS) {
                if (board.isCoordinateOccupied(new Coordinate(checkX, checkY))) {
                    occupiedCorners++;
                }
            }
        }
    
        // A valid L-Spin requires at least two corners to be occupied
        return occupiedCorners >= 2;
    }


    /**
     * This method is a counterpart to the "shouldExplore" method. Whenever we decide
     * that we should ignore our policy, we now have to actually choose an action.
     *
     * You should come up with a way of choosing an action so that the model gets
     * to experience something new. The current implemention just chooses a random
     * option, which in practice doesn't work as well as a more guided strategy.
     * I would recommend devising your own strategy here.
     */
    @Override
    public Mino getExplorationMove(final GameView game)
    {
        List<Mino> positions = game.getFinalMinoPositions();
        if (positions.isEmpty()) return null;

        return positions.stream()
            .max(Comparator.comparingInt((Mino mino) -> {
                Board simulatedBoard = new Board(game.getBoard());
                simulatedBoard.addMino(mino);
                int linesCleared = simulatedBoard.clearFullLines().size();
                int bumpiness = calculateBumpiness(simulatedBoard);
                return linesCleared * 100 - bumpiness;  
            }))
            .orElse(null);
    }

    /**
     * This method is called by the TrainerAgent after we have played enough training games.
     * In between the training section and the evaluation section of a phase, we need to use
     * the exprience we've collected (from the training games) to improve the q-function.
     *
     * You don't really need to change this method unless you want to. All that happens
     * is that we will use the experiences currently stored in the replay buffer to update
     * our model. Updates (i.e. gradient descent updates) will be applied per minibatch
     * (i.e. a subset of the entire dataset) rather than in a vanilla gradient descent manner
     * (i.e. all at once)...this often works better and is an active area of research.
     *
     * Each pass through the data is called an epoch, and we will perform "numUpdates" amount
     * of epochs in between the training and eval sections of each phase.
     */
    @Override
    public void trainQFunction(Dataset dataset,
                               LossFunction lossFunction,
                               Optimizer optimizer,
                               long numUpdates)
    {
        for(int epochIdx = 0; epochIdx < numUpdates; ++epochIdx)
        {
            dataset.shuffle();
            Iterator<Pair<Matrix, Matrix> > batchIterator = dataset.iterator();

            while(batchIterator.hasNext())
            {
                Pair<Matrix, Matrix> batch = batchIterator.next();

                try
                {
                    Matrix YHat = this.getQFunction().forward(batch.getFirst());

                    optimizer.reset();
                    this.getQFunction().backwards(batch.getFirst(),
                                                  lossFunction.backwards(YHat, batch.getSecond()));
                    optimizer.step();
                } catch(Exception e)
                {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * This method is where you will devise your own reward signal. Remember, the larger
     * the number, the more "pleasurable" it is to the model, and the smaller the number,
     * the more "painful" to the model.
     *
     * This is where you get to tell the model how "good" or "bad" the game is.
     * Since you earn points in this game, the reward should probably be influenced by the
     * points, however this is not all. In fact, just using the points earned this turn
     * is a **terrible** reward function, because earning points is hard!!
     *
     * I would recommend you to consider other ways of measuring "good"ness and "bad"ness
     * of the game. For instance, the higher the stack of minos gets....generally the worse
     * (unless you have a long hole waiting for an I-block). When you design a reward
     * signal that is less sparse, you should see your model optimize this reward over time.
     */
    @Override
    public double getReward(final GameView game)
    {
         // Get the score earned in this turn
        double scoreThisTurn = game.getScoreThisTurn();
        double maxHeight = Board.NUM_ROWS -2;
        double heightRatio = getHeight(game.getBoard()) / maxHeight;
        double bumpiness = calculateBumpiness(game.getBoard()) / maxHeight;
        double numHoles = countHoles(game.getBoard()) / (double)(Board.NUM_ROWS * Board.NUM_COLS);
        double linesCleared = getLinesCleared(game.getBoard()) / maxHeight;

        // Define penalty coefficients (adjust as needed)
        double heightPenaltyCoeff = -0.2;
        double bumpinessPenaltyCoeff = -0.2;
        double numHolesPenaltyCoeff = -0.1;
        double linesClearedPenaltyCoeff = 0.5;

        // Calculate penalties based on descriptive attributes
        double heightPenalty = heightPenaltyCoeff * heightRatio;
        double bumpinessPenalty = bumpinessPenaltyCoeff * bumpiness;
        double numHolesPenalty = numHolesPenaltyCoeff * numHoles;
        double linesClearedPenalty = linesClearedPenaltyCoeff * linesCleared;

        // Calculate total penalty
        double totalPenalty = heightPenalty + bumpinessPenalty + numHolesPenalty + linesClearedPenalty;

        // Combine the score earned in this turn with the penalties
        double reward = scoreThisTurn - totalPenalty;

        return reward;
    }

}
