/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package facetracker.controller;

import facetracker.model.*;
import facetracker.view.*;
import facetracker.view.FaceTrackerActivity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

//import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;
//import facetracker.Face;
import java.util.ArrayList;

//test comment marcus

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    ArrayList<FaceData> storeData = new ArrayList<FaceData>();
    ArrayList<FaceData> storeDataLarge = new ArrayList<FaceData>();

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    public void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);


        if(FatigueScore.getNeedNewBaseline() == true) {
            FatigueScore.setBaseline(bottom);
            FaceTrackerActivity.playFatigueReadySound();
            storeData.clear();

            if(face.getIsLeftEyeOpenProbability() < .10 && face.getIsRightEyeOpenProbability() < .10){
                FatigueScore.setCanEyesBeSeen(false);
            }
            FatigueScore.setNeedNewBasline(false);
        }
        
        //stores face data using FaceData object
        storeData.add(new FaceData(face.getIsLeftEyeOpenProbability(), face.getIsRightEyeOpenProbability(), bottom, top));
        storeDataLarge.add(new FaceData(face.getIsLeftEyeOpenProbability(), face.getIsRightEyeOpenProbability(), bottom, top));

        if(storeData.size() > 25 && FatigueScore.getBaseline() != 0) {     //if size of data array is greater than 25
            int temp = FatigueScore.getTotalFatigueScore();
            Fatigue test = new Fatigue(storeData);              //send data to Fatigue class for processing
            //test.printData();
            test.checkIfFatigued();
            if(FatigueScore.getTotalFatigueScore() == temp && FatigueScore.getTotalFatigueScore() > 0){
                FatigueScore.setTotalFatigueScore(-100);
            }
            storeData.clear();
            //ADD if fatigue score above blah do call AlertDriver
            //ADD if baseline = to null restart or something
        }

        //Large amounts of data are needed for detecting nodding
        if(storeDataLarge.size() > 100 && FatigueScore.getBaseline() != 0) {     //if size of data array is greater than 100
            int temp = FatigueScore.getTotalFatigueScore();
            Fatigue test = new Fatigue(storeDataLarge);              //send data to Fatigue class for processing
            //test.printData();
            test.checkIfFatigued();
            if(FatigueScore.getTotalFatigueScore() == temp && FatigueScore.getTotalFatigueScore() > 0){
                FatigueScore.setTotalFatigueScore(-100);
            }
            storeDataLarge.clear();
        }

    }

    /**
     * Internal class helper
     * Allows the storing of face data in simple manner
     */
    public class FaceData {

        private float leftEye;
        private float rightEye;
        private float bottom;
        private float top;

        /**
         * Class constructor
         * @param leftEye, the leftEye value
         * @param rightEye, the rightEye value
         * @param bottom, the Bottom box coordinates
         */
        public FaceData(float leftEye, float rightEye, float bottom, float top){
            this.leftEye = leftEye;
            this.rightEye = rightEye;
            this.bottom = bottom;
            this.top = top;
        }

        /**
         * Overridden toString method to print stored data
         * @return a string consisting of a tuple of data sets
         */
        public String toString() {
            return "(" + leftEye + ", " + rightEye + ", " + bottom + ", " + top + ")\n";
        }

        /**
         * Gets the value of the left eye
         * @return the value of the left eye
         */
        public float getLeftEye(){
            return this.leftEye;
        }

        /**
         * Gets the value of the right eye
         * @return the value of the right eye
         */
        public float getRightEye(){
            return this.rightEye;
        }

        /**
         * Gets the value of the bottom line tracking the chin
         * @return the bottom line
         */
        public float getBottom(){
            return this.bottom;
        }

        public float getTop() { return this.top; }

    }
}
