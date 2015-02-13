/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.silsglass.coachingviews;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.glass.touchpad.Gesture;
import com.silsglass.taskmanager.R;


/**
 * The concrete, non-tutorial implementation of the game: one minute long with ten randomly
 * selected phrases.
 */
public class TaskDemoActivity extends BaseTemplateTask {


	private static final int VIDEO_PLAY_REQUEST_CODE = 100;

	/** The number of phrases that will be selected for the game. */
    private static final int NUMBER_OF_ACTIVITIES = 5;

    /** The maximum duration of the game. */
    private static final int GAME_TIME_SECONDS = (int) TimeUnit.MINUTES.toSeconds(100);

    /** Handler used to keep the game ticking once per second. */
    private final Handler mHandler = new Handler();

    private int mShowingPicture = 0; // to show that a picture is being shown.

    private int textCtrl = 0 ;   //to show that the text view is opened
    /**
     * Runner that is called once per second during the game to advance the state or end the game
     * when time runs out.
     */
    private final Runnable mTick = new Runnable() {
        @Override
        public void run() {
            mSecondsRemaining--;
            updateTimer();

            if (mSecondsRemaining <= 0) {
                endRun();
            } else {
                nextTick();
            }
        }
    };

    /** Keeps track of the amount of time remaining in the game. */
    private int mSecondsRemaining;

    /** TextView that displays the amount of time remaining in the game. */
    private TextView mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimer = (TextView) findViewById(R.id.timer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSecondsRemaining = GAME_TIME_SECONDS;
        updateTimer();
        nextTick();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mTick);
    }

    /** Overridden to select ten random phrases from the application's resources. */
    @Override
    protected TaskCoachingModel createTaskCoachingModel() {
        List<String> allPhrases = Arrays.asList(getResources().getStringArray(
                R.array.activities));
//        Collections.shuffle(allPhrases);
        return new TaskCoachingModel(allPhrases.subList(0, NUMBER_OF_ACTIVITIES));
    }

    @Override
    protected void handleRunGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
            	if (textCtrl == 1) {
            		// the text screen is shown, switch view back to flipper
               		setUpScreen();
                    updateDisplay();
                    textCtrl = 0 ;
                    // score();
            		pass();
            		break;
            	};
            	
            	if (getTaskCoachingModel().getCurrentPhraseIndex() == 1) { 
                    displayText();     
            	}
            	if (getTaskCoachingModel().getCurrentPhraseIndex() == 2) { 
                    displayVideo(); 
                    // score();
                    
            	}
            	if (getTaskCoachingModel().getCurrentPhraseIndex() == 3) { 
                    playAudio();     		
                    // score();
            	}
            	if (getTaskCoachingModel().getCurrentPhraseIndex() == 4) {
            		if (mShowingPicture == 0) {
            			displayPictures();
            		} else {
            			// is showing the picture, set the view back to text
            	        setUpScreen();
            			updateDisplay();
                		mShowingPicture = 0 ;
                        //score();
                		pass();
            		}
            		break;
            	}
                if (getTaskCoachingModel().areAllPhrasesGuessedCorrectly()) {
                    endRun();
                }
                break;
            case TWO_TAP:
        		Log.v(TAG,  "double tap for action");
        		userAction();
    	        setUpScreen();
    			updateDisplay();
            	break;
            case SWIPE_RIGHT:
                pass();
                break;
            case SWIPE_LEFT:
                goBack();
                break;
        }
    }

    /** Enqueues the next timer tick into the message queue after one second. */
    private void nextTick() {
        mHandler.postDelayed(mTick, 1000);
    }

    /** Updates the timer display with the current number of seconds remaining. */
    private void updateTimer() {
        // The code point U+EE01 in Roboto is the vertically centered colon used in the clock on
        // the Glass home screen.
        String timeString = String.format(
            "%d\uee01%02d", mSecondsRemaining / 60, mSecondsRemaining % 60);
        mTimer.setText(timeString);
    }


    
    /**
     * Called either when the last phrase is guessed correctly or time has run out to finish the
     * game play activity and display the game results screen.
     */
    private void endRun() {
        Intent intent = new Intent(this, RunResultsActivity.class);
        intent.putExtra(RunResultsActivity.EXTRA_MODEL, getTaskCoachingModel());
        startActivity(intent);
        finish();
    }
    
    protected void displayText() {
    	String filepath;
    	final String textToShow;
    	Bundle extras = getIntent().getExtras();
		if (extras != null)
			filepath = extras.getString("filepath");
		else {
			filepath = copyAsset("bowline.txt");
			Log.v(TAG,  "after copyAsset: filepath="+filepath);
		} 
		InputStream inputStream = getResources().openRawResource(R.raw.bowline);

	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

	    int i;
	 try {
	  i = inputStream.read();
	  while (i != -1)
	     {
	      byteArrayOutputStream.write(i);
	      i = inputStream.read();
	     }
	     inputStream.close();
	 } catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	 }

	    //Log.v(TAG, byteArrayOutputStream.toString());
		//Toast.makeText(TaskDemoActivity.this, byteArrayOutputStream.toString(), Toast.LENGTH_LONG).show();
	    textToShow = byteArrayOutputStream.toString();
		setContentView(R.layout.textviewcard);
		mTextViewCard = (TextView) findViewById(R.id.textViewCard);
//	    getCurrentTextView().setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
//	    getCurrentTextView().setText(textToShow);
	    mTextViewCard.setText(textToShow);
	    mTextViewCard.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
//     	 how to tap to return to previous screen
		
	    textCtrl = 1;
    }

     
    protected void displayVideo() {
    	String filepath;
    	Bundle extras = getIntent().getExtras();
		if (extras != null)
			filepath = extras.getString("filepath");
		else {
			filepath = copyAsset("video.mp4");
			Log.v(TAG,  "after copyAsset: filepath="+filepath);
		}        
		
		// play video using the Glass video player
		Intent i = new Intent();
		i.setAction("com.google.glass.action.VIDEOPLAYER");
		//i.putExtra("video_url", new File(Environment.getExternalStorageDirectory(), "in.mp4").getAbsolutePath());
		//i.putExtra("video_url", "https://www.youtube.com/watch?v=YXRnPES0Qec"); 		
		i.putExtra("video_url", filepath);
		startActivityForResult(i, VIDEO_PLAY_REQUEST_CODE);
    }
    
    protected void playAudio() {
    	String filepath;
    	Bundle extras = getIntent().getExtras();
		if (extras != null)
			filepath = extras.getString("filepath");
		else {
			filepath = copyAsset("audio.mp3");
			Log.v(TAG,  "after copyAsset: filepath="+filepath);
		}        
		Toast.makeText(TaskDemoActivity.this, "Playing Audio ...", Toast.LENGTH_LONG).show();

		mPlayer = new MediaPlayer();
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);							
			mPlayer.setDataSource(filepath);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(TAG, "prepare() failed");
		}
	}
    
    protected void displayPictures() {
    	String filepath;
		setContentView(R.layout.imageview);
    	Bundle extras = getIntent().getExtras();
		if (extras != null)
			filepath = extras.getString("filepath");
		else {
			filepath = copyAsset("bowlineSample.jpg");
			Log.v(TAG,  "after copyAsset: filepath="+filepath);
		}        
		
//		mPictureFilePath = (File)extras.get("picturefilepath");
		mShowingPicture = 1 ;
//		Log.v(TAG, "pictureFilePath=" + mPictureFilePath.getAbsolutePath());
		mImageview =  (ImageView) findViewById(R.id.picture);

		// need to scale down the image to avoid the error of loading a bitmap too big
		//Bitmap myBitmap = BitmapFactory.decodeFile(mPictureFilePath.getAbsolutePath());
		Bitmap myBitmap = BitmapFactory.decodeFile(filepath);
		int h = (int) ( myBitmap.getHeight() * (640.0 / myBitmap.getWidth()) );

		Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 640, h, true);
		Log.v(TAG,  "about to show image");
		mImageview.setImageBitmap(scaled); 		
		Log.v(TAG,  "after showing image?");

		
    }

  
}
