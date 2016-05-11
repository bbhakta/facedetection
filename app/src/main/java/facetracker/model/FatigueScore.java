package facetracker.model;

import facetracker.view.*;
import facetracker.controller.*;
/**
 * A singleton design pattern inspired class,to keep track of the current fatigue score
 * along with other elements needed to detect fatigue.
 */
public class FatigueScore {

    private static FatigueScore fatigueScore = new FatigueScore( );
    private static int totalFatigueScore = 0;
    private static boolean needNewBaseline = false;
    private static float baseline = 0;
    private static boolean canEyesBeSeen = true;
    private static int maxFatigueScore = 2000;
    private static boolean fatigueDetectionActive = false;

    /* Static 'instance' method */
    public static FatigueScore getInstance( ) {
        return fatigueScore;
    }
    /* Other methods protected by singleton-ness */

    public static int getTotalFatigueScore(){
        return totalFatigueScore;
    }

    public static void setTotalFatigueScore(int score){
        if(score < 0)
            totalFatigueScore -= Math.abs(score);
        else
            totalFatigueScore += score;
    }

    public static void resetTotalFatigueScore(){
        totalFatigueScore = 0;
    }

    public static boolean getNeedNewBaseline(){
        return needNewBaseline;
    }

    public static void setNeedNewBasline(boolean tempNeedNewBaseline){
        needNewBaseline = tempNeedNewBaseline;
    }

    public static float getBaseline(){
        return baseline;
    }

    public static void setBaseline(float newBaseline){
        baseline = newBaseline;
    }

    public static boolean getCanEyesBeSeen(){
        return canEyesBeSeen;
    }

    public static void setCanEyesBeSeen(boolean eyesSeen){
        canEyesBeSeen = eyesSeen;
    }

    public static int getMaxFatigueScore(){
        return maxFatigueScore;
    }

    public static void setMaxFatigueScore(int newMaxScore){
        maxFatigueScore = newMaxScore;
    }

    public static boolean getFatigueDetectionActive(){
        return fatigueDetectionActive;
    }

    public static void setFatigueDetectionActive(boolean isActive){
        fatigueDetectionActive = isActive;
    }
}
