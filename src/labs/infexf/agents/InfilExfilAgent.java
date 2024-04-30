package src.labs.infexf.agents;

// SYSTEM IMPORTS
import edu.bu.labs.infexf.agents.SpecOpsAgent;
import edu.bu.labs.infexf.distance.DistanceMetric;
import edu.bu.labs.infexf.graph.Vertex;
import edu.bu.labs.infexf.graph.Path;


import edu.cwru.sepia.environment.model.state.State.StateView;

import edu.cwru.sepia.environment.model.state.Unit.UnitView;


// JAVA PROJECT IMPORTS


import java.util.Stack; 



public class InfilExfilAgent
    extends SpecOpsAgent
{

    public InfilExfilAgent(int playerNum)
    {
        super(playerNum);
    }

    // if you want to get attack-radius of an enemy, you can do so through the enemy unit's UnitView
    // Every unit is constructed from an xml schema for that unit's type.
    // We can lookup the "range" of the unit using the following line of code (assuming we know the id):
    //     int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
    @Override
    public float getEdgeWeight(Vertex src,
                               Vertex dst,
                               StateView state)
    {
        float dist = 1;
        //float dist = DistanceMetric.euclideanDistance(src, dst);
        for (Integer enemyUnitID : getOtherEnemyUnitIDs()){
            int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
            UnitView Enemy_View = state.getUnit(enemyUnitID);
            Vertex Enemy_Vertex = new Vertex(Enemy_View.getXPosition(), Enemy_View.getYPosition());
            float Danger_Dist = DistanceMetric.chebyshevDistance(dst, Enemy_Vertex);
            if (Danger_Dist <= attackRadius + 1){
                dist = 1000;
            }
            dist = dist/Danger_Dist;



        }
        return dist;
    }





    @Override
    public boolean shouldReplacePlan(StateView state)
    {  

        Stack<Vertex> currentPlan = getCurrentPlan();
        if (currentPlan != null) {
            // Iterate over all unit IDs to see if anything blocks our path
            for (Integer unitId : getOtherEnemyUnitIDs()) {
                    UnitView unitView = state.getUnit(unitId);
                    if (unitView != null){
                        int attackRadius = state.getUnit(unitId).getTemplateView().getRange();
                        // Check if the current path is blocked
                        Vertex Enemy_Position = new Vertex(unitView.getXPosition(), unitView.getYPosition());
                        //System.out.println("Enemy Position" + Enemy_Position);

                        for (Vertex v : currentPlan) {
                            float Danger_Dist = DistanceMetric.chebyshevDistance(v, Enemy_Position);
                            if (Danger_Dist <= attackRadius + 2){
                                //System.out.println("Replan");
                                return true;
                            }

                        }


                    }


            }
        }

        return false;
    }

}
