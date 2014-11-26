package com.silsglass.coachingmodel;

import java.util.Date ;
/**
public class CoachingTask {


	    private final String mTaskName;
	    private final String mTaskGoal;
	    private final String mTaskDescription;
	    //private final Date mTaskCreated ;
	    private final int mTaskNbrActivities ;
	    private final boolean mTaskActive ; 
	
	    private CoachingActivityInTask mActivity[];
	
	public CoachingTask() {
		// These values should be assigned by the dataStream class from a content source
		// for now, just manually assigning values
		mTaskName = "Tie a Bowline Knot" ;
		mTaskGoal = "How to tie a useful knot" ;
		mTaskDescription = "Step-by-step description for a bowline knot" ;
		mTaskActive = true ;
		// Get number of activities from CoachingActivityInTask
		setActivities(); 
	}

	private String getTaskName() {
		return mTaskName ; 
	}
	private String getTaskGoal() {
		return mTaskGoal ; 
	}

	private String getTaskDescription() {
		return mTaskDescription ; 
	}

	private int getTaskNbrActivities() {
		return mTaskNbrActivities ; 
	}

	private void setActivities() {
		// This method manually create 4 activities as sample for the sample task
		// create activity 1, add to the list in task
		// create activity 2, add to the list in task
		// create activity 3, add to the list in task
		// create activity 4, add to the list in task
		
	}
}
*/