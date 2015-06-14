package mmmobile.android;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class MmActivity extends Activity implements OnClickListener {

	MmApplication mmApplication;

	public void onCreate(Bundle savedInstanceState, int layout, String title) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mmApplication = (MmApplication) getApplication();
		setContentView(layout);
		mmApplication.setCurrentActivity(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		mmApplication.setCurrentActivity(this);
		mmApplication.setActivityState(this.getClass(), true);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mmApplication.setActivityState(this.getClass(), false);
	}

	public void onClick(View arg0) {
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
	}
}
