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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.silsglass.taskmanager.R;

/**
 * The concrete, non-tutorial implementation of the game: one minute long with ten randomly
 * selected phrases.
 */
public class TaskRunActivity extends BaseTemplateTask {

    /** The number of phrases that will be selected for the game. */
    private static final int NUMBER_OF_PHRASES = 10;

    /** The maximum duration of the game. */
    private static final int GAME_TIME_SECONDS = (int) TimeUnit.MINUTES.toSeconds(1);

    /** Handler used to keep the game ticking once per second. */
    private final Handler mHandler = new Handler();

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
        Collections.shuffle(allPhrases);
        return new TaskCoachingModel(allPhrases.subList(0, NUMBER_OF_PHRASES));
    }

    @Override
    protected void handleRunGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
                score();
                if (getTaskCoachingModel().areAllPhrasesGuessedCorrectly()) {
                    endRun();
                }
                break;
            case SWIPE_RIGHT:
                pass();
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
}
