package facetracker.model;

import facetracker.controller.*;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;


public class Fatigue {

    protected ArrayList<FaceGraphic.FaceData> data;
    protected List<Float> y = new ArrayList<Float>();
    protected List<Integer> x = new ArrayList<Integer>();
    /**
     * Constructor for class
     * @param data, ArrayList passed to the class
     * fill the lisys y and x which are used for detecting nodding.
     */
    public Fatigue(ArrayList data) {
        this.data = data;


        for(int i = -12; i <= 12; i++){
            x.add(i);
        }

        for(FaceGraphic.FaceData temp : this.data) {
            y.add(temp.getBottom());
            Log.i("TEST", "Y ==> : " + temp.getBottom());
        }
    }

    /**
     * Setter for data
     * @param data, ArrayList of data being passed to class
     */
    public void setData(ArrayList data) {
        this.data = data;
    }

    /**
     * Getter for data
     * @return the ArrayList
     */
    public ArrayList getData() {
        return this.data;
    }

    /**
     * Prints the data using toString
     */

    public void printData() {
        for (Object info : data) {
            Log.i("TESTIINNGG", info.toString());
        }
    }

    /**
     * A method that choices which helper methods for detecting fatigue will run
     */
    public void checkIfFatigued(){
        if(FatigueScore.getFatigueDetectionActive()) {
            if (this.data.size() > 70) {
                checkNodding();
            } else {
                if (FatigueScore.getCanEyesBeSeen()) {
                    checkEye();
                    checkHeadUpDown();
                } else {
                    checkHeadUpDown();
                }
            }
        }

    }

    /**
     * A method that checks to see if a driver has there eyes closed for an unsafe
     * amount of time. A counter is used to keep track of how long the eyes are closed
     * and closed for too long, the fatigue score increases.
     */

    public void checkEye(){
        int eyeCounter = 3;
        for(FaceGraphic.FaceData temp : data) {

            float leftEyeTemp = temp.getLeftEye();
            float rightEyeTemp = temp.getRightEye();

            if (leftEyeTemp < .30 && rightEyeTemp < .30){
                eyeCounter++;
                if (eyeCounter >= 20){
                    FatigueScore.setTotalFatigueScore(25);
                }
            }
            else{
                eyeCounter = 0;
            }

        }

    }

    /**
     *  A Function that starts the check to see if the driver is nodding.
     *  First it  call tyhe helper method fillMatrixVaribles, then it uses
     *  those values with the helper method computeParabulaConstants to
     *  calcluate the needed a,b,c values. Then c along with the
     *  value returned from the helper method computerRsquared
     *  is compared to check if nodding has accured.
     */

    public void checkNodding(){
        MatrixVarible tempMaxtrixVarible = new MatrixVarible();
        ParabulaConstants tempabc = new ParabulaConstants();

        List<ParabulaConstants> abcArrayList = new ArrayList<ParabulaConstants>();

        for(int i = 0; i < 25; i = i + 5) {
            tempMaxtrixVarible = fillMatrixVaribles(x, y, i);
            tempabc = computeParabulaConstants(tempMaxtrixVarible);

            Log.i("TEST", "A value ==> : " + tempabc.A);
            Log.i("TEST", "B value ==> : " + tempabc.B);
            Log.i("TEST", "C value ==> : " + tempabc.C);

            if (Math.abs(tempabc.C) >= .15) {
                //FaceTrackerActivity.playSound();
                if(computerRsquared(tempabc,y,i)) {
                    //FaceTrackerActivity.playReadySound();
                    FatigueScore.setTotalFatigueScore(300);
                }
            }
            abcArrayList.add(tempabc);
        }
        Log.i("TEST","THIS IS ABCLISTSIZE ==> " + abcArrayList.size());
    }

    /**
     * A method that checks if the drivers head is in a down or up position for an
     * unsafe amount of time. A counter is used to count if the data entry the head is up or
     * it is down, over a 11 times consecutively. IF that happens, starts adding to the fatigue score.
     * If at anytime the head is back to normal the count is reset to 0.
     */
    public void checkHeadUpDown(){
        int headDownUpCounter = 2;
        for(FaceGraphic.FaceData temp : data) {
            float sizeOfSquare = temp.getBottom() - temp.getTop();
            sizeOfSquare = sizeOfSquare * (float).3;
            if(temp.getBottom() > FatigueScore.getBaseline() + sizeOfSquare * .5){
                headDownUpCounter++;
                if(headDownUpCounter > 11){
                    FatigueScore.setTotalFatigueScore(50);
                }
            }
            else if(temp.getBottom() < FatigueScore.getBaseline()- sizeOfSquare * .9){
                headDownUpCounter++;
                if(headDownUpCounter > 11){
                    FatigueScore.setTotalFatigueScore(50);
                }
            }
            else{
                headDownUpCounter = 0;
            }
        }

    }
    /**
     * This function compute the three constants in a parabula.
     * The first set is to compute the all of the constanst from the normalized system of equations.
     * note: n is the number of inputs, and sum is the sumation of all of the given inputs.
     * sum(y)= n * a + sum(x) * b sum(x^2) * c
     * sum(x * y)= sum(x) * a + sum(x^2) * b sum(x^3) * c
     * sum(x^2 * y)= sum(x^2) * a + sum(x^3) * b sum(x^4) * c
     */
    public MatrixVarible fillMatrixVaribles(List<Integer> x, List<Float> y,int yOffset){
        MatrixVarible matrixVaribles = new MatrixVarible();

        for(int i = 0; i < x.size(); i++){
            matrixVaribles.sumx2 += (x.get(i) * x.get(i) * 3);
            matrixVaribles.sumx4 += x.get(i) * x.get(i) * x.get(i) * x.get(i);
        }

        for(int i = 0; i < x.size() * 3; i++) {
            matrixVaribles.sumx2y += x.get(i % x.size()) * x.get(i % x.size()) * y.get(i + yOffset);
            matrixVaribles.sumxy += x.get(i % x.size()) * y.get(i + yOffset);
            matrixVaribles.sumy += y.get(i + yOffset);
        }

        matrixVaribles.sizeOfList = x.size() * 3;

        return matrixVaribles;
    }

    /**
     * This function computes the a, b, and c from the normalized equation:
     * y= a + b * x + c * x^2
     * this use cramers law to calculate a, b, and c.
     * http://www.purplemath.com/modules/cramers.htm
     */
    public ParabulaConstants computeParabulaConstants(MatrixVarible matrixMaribles){
        ParabulaConstants abc = new ParabulaConstants();

        double numeratorA = 0.0;
        double numeratorB = 0.0;
        double numeratorC = 0.0;
        double denomiator = 0.0;

        denomiator = (matrixMaribles.sizeOfList * matrixMaribles.sumx2 * matrixMaribles.sumx4) - (matrixMaribles.sumx2 * matrixMaribles.sumx2 * matrixMaribles.sumx2);
        numeratorA = (matrixMaribles.sumy * matrixMaribles.sumx2 * matrixMaribles.sumx4) - (matrixMaribles.sumx2 * matrixMaribles.sumx2 * matrixMaribles.sumx2y);
        numeratorB = (matrixMaribles.sizeOfList * matrixMaribles.sumxy * matrixMaribles.sumx4) - (matrixMaribles.sumx2 * matrixMaribles.sumx2 * matrixMaribles.sumxy);
        numeratorC = (matrixMaribles.sizeOfList * matrixMaribles.sumx2 * matrixMaribles.sumx2y) - (matrixMaribles.sumy * matrixMaribles.sumx2 * matrixMaribles.sumx2);

        abc.A = (numeratorA/denomiator);
        abc.B = (numeratorB/denomiator);
        abc.C = (numeratorC/denomiator);

        return abc;
    }

    /**
     * This function computes R Squared by using the formula (1- (SSres/SStot)
     * R-Square is used to determine how closely the data that we pull matches the intended graph
     * this allows us to exclude things that are not nods :)
     * please refer to the wiki for more information https://en.wikipedia.org/wiki/Coefficient_of_determination
     */
    public boolean computerRsquared(ParabulaConstants abc, List<Float> y,int yOffset){
        boolean isResonable = false;
        float total = 0;
        float avg = 0;
        float numerator = 0;
        float denomiator = 0;

        for(int i = 0; i < x.size() * 3; i++){
            total += y.get(i + yOffset);
        }

        avg = (total) / (x.size() * 3);

        for(int i = 0; i < x.size() * 3; i++)
        {
            numerator += Math.pow(y.get(i+yOffset) - (abc.A + abc.B * ((i % 25) - 12) + abc.C * (((i % 25) - 12) * ((i % 25) - 12))), 2);
            denomiator += Math.pow(((y.get(i + yOffset)) - avg),2);
        }

        if(denomiator != 0) {
            Log.i("TEST", "THIS IS R SQUARED VALUE ==> " + (1 - (numerator / denomiator)));
            Log.i("TEST", "THIS IS NUMERATOR VALUE ==> " + numerator);
            Log.i("TEST", "THIS IS DENOMIATOR VALUE ==> " + denomiator);

            if (1 - (numerator / denomiator) > -.25) {
                isResonable = true;
            }
        }
        return isResonable;
    }
}

//Object for storing ParabulaConstants
class ParabulaConstants {
    public double A = 0.0;
    public double B = 0.0;
    public double C = 0.0;
}

//Object for storing MatrixVarible
class MatrixVarible{
    double sumx2 = 0.0;
    double sumx4 = 0.0;
    double sumx2y = 0.0;
    double sumxy = 0.0;
    double sumy = 0.0;
    int sizeOfList = 0;
}

//17.6
//-26.5