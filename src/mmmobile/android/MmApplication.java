package mmmobile.android;

import java.util.HashMap;

import android.app.Activity;
import android.app.Application;

public class MmApplication extends Application { 
	private Activity currentActivity;

	private HashMap<Class<?>, Boolean> activityStates;

	@Override
	public void onCreate() { 
		super.onCreate();
		activityStates = new HashMap<Class<?>, Boolean>();
	}

	@Override
	public void onTerminate() { 
		super.onTerminate();
	}

	public void setActivityState(Class<?> klass, boolean visible) {
		this.activityStates.put(klass, visible);
	}

	public boolean isActivityVisible(Class<?> klass) {
		return this.activityStates.get(klass);
	}

	public void setCurrentActivity(Activity activity) {
		currentActivity = activity;
	}

	public Activity getCurrentActivity() {
		return currentActivity;
	}
}
