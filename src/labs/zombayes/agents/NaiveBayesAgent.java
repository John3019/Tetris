package src.labs.zombayes.agents;


// SYSTEM IMPORTS
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// JAVA PROJECT IMPORTS
import edu.bu.labs.zombayes.agents.SurvivalAgent;
import edu.bu.labs.zombayes.features.Features.FeatureType;
import edu.bu.labs.zombayes.linalg.Matrix;
import edu.bu.labs.zombayes.utils.Pair;
import edu.bu.labs.zombayes.linalg.Shape;



public class NaiveBayesAgent
    extends SurvivalAgent
{

    public static class NaiveBayes
        extends Object
    {

        //add maps tp store our means and standard deviations
        private Map<Integer, List<Double>> classMeans;
        private Map<Integer, List<Double>> classStdDevs;
        public static final FeatureType[] FEATURE_HEADER = {FeatureType.CONTINUOUS,
                                                            FeatureType.CONTINUOUS,
                                                            FeatureType.DISCRETE,
                                                            FeatureType.DISCRETE};

        // TODO: complete me!
        public NaiveBayes()
        {
            classMeans = new HashMap<>();
            classStdDevs = new HashMap<>();

        }

        // TODO: complete me!
        public void fit(Matrix X, Matrix y_gt)
        {
            //use shape to get num rows and columns 
            Shape size = X.getShape();
            int numSamples = size.getNumRows();
            int numFeatures = size.getNumCols();

            // Loop through each class (0 for humans, 1 for zombies)
            for (int c = 0; c < 2; c++) {
                // Filter samples belonging to the current class
                try {
                    Matrix X_class = X.filterRows(y_gt.getRowMaskEq(c, 0));

                    // Calculate mean and standard deviation for each feature
                    List<Double> means = new ArrayList<>();
                    List<Double> stdDevs = new ArrayList<>();
                    for (int j = 0; j < numFeatures; j++) {
                        Matrix featureColumn = X_class.getCol(j);
                        Shape featureSize = X_class.getShape();
                        double mean = featureColumn.sum().item() / featureSize.getNumRows();
                        double stdDev = Math.sqrt((featureColumn.pow(2).sum().item() / featureSize.getNumRows()) - Math.pow(mean, 2));
                        means.add(mean);
                        stdDevs.add(stdDev);
                    }

                    // Store mean and standard deviation for the current class
                    this.classMeans.put(c, means);
                    this.classStdDevs.put(c, stdDevs);
                } catch (Exception e) {
                    // Handle exceptuon thrown by filterrows
                    e.printStackTrace();
                }
            }
            return;
        }

        // TODO: complete me!
        public int predict(Matrix x) 
        {
            // Initialize variables to store probabilities for each class
            double[] classProbabilities = new double[2]; // 0 for humans, 1 for zombies
            Shape size = x.getShape();
            int numFeatures = size.getNumCols();

            // Calculate probabilities for each class using Naive Bayes formula
            for (int c = 0; c < 2; c++) {
                double classProbability = 1.0; 
                try {
                    for (int j = 0; j < numFeatures; j++) {
                        double mean = this.classMeans.get(c).get(j);
                        double stdDev = this.classStdDevs.get(c).get(j);
                        double value = x.get(0, j);
        
                        // Calculate probability density function for continuous features
                        double pdf = Math.exp(-Math.pow(value - mean, 2) / (2 * Math.pow(stdDev, 2)))
                                        / (Math.sqrt(2 * Math.PI) * stdDev);
                        classProbability *= pdf;
                    }
                    classProbabilities[c] = classProbability;
                } catch (Exception e) {
                    // Handle the exception thwon 
                    e.printStackTrace();
                }
            }

            int predictedClass = classProbabilities[0] > classProbabilities[1] ? 0 : 1;
            return predictedClass;
        }

    }
    
    private NaiveBayes model;

    public NaiveBayesAgent(int playerNum, String[] args)
    {
        super(playerNum, args);
        this.model = new NaiveBayes();
    }

    public NaiveBayes getModel() { return this.model; }

    @Override
    public void train(Matrix X, Matrix y_gt)
    {
        System.out.println(X.getShape() + " " + y_gt.getShape());
        this.getModel().fit(X, y_gt);
    }

    @Override
    public int predict(Matrix featureRowVector)
    {
        return this.getModel().predict(featureRowVector);
    }

}
