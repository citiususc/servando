package es.usc.citius.servando.android.app.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.ui.NotificationMgr;

public abstract class ServandoActivity extends Activity {

	private View content;

	private ImageButton notificationsIcon;
	private TextView notificationCount;
	private NotificationsReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.servando_activity_default);
		notificationsIcon = (ImageButton) findViewById(R.id.home_notifications_icon);
		notificationCount = (TextView) findViewById(R.id.home_notifications_count);

		if (getActionBarTitle() != null)
		{
			setActionBarTitle(getActionBarTitle());
		}

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		content = inflater.inflate(getViewId(), null);
		FrameLayout container = (FrameLayout) findViewById(R.id.content_frame);
		container.addView(content);

		registerNotificationReceiver();

		onBaseCreated(getIntent());
	}

	protected abstract int getViewId();

	protected abstract String getActionBarTitle();

	protected void setActionBarTitle(String title)
	{
		((TextView) findViewById(R.id.service_title_tv)).setText(title);
	}

	protected void onBaseCreated(Intent intent)
	{
	};

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	public void onClickHome(View v)
	{
		goHome(this);
	}

	public void onClickAbout(View v)
	{
		startActivity(new Intent(getApplicationContext(), AboutActivity.class));
	}

	public void goHome(Context context)
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		final Intent intent = new Intent(context, homeActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public void toast(String msg)
	{
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	} // end toast

	private void registerNotificationReceiver()
	{
		// TODO Auto-generated method stub
		IntentFilter notificationFIlter = new IntentFilter(ServandoPlatformFacade.NOTIFICATIONS_UPDATE);
		receiver = new NotificationsReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, notificationFIlter);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			updateNotificationsInfo();
		}
	}

	public void onClickNotifications(View v)
	{
		Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
		startActivity(intent);
	}

	private void updateNotificationsInfo()
	{
		int count = NotificationMgr.getInstance().getCount();

		if (count > 0)
		{
			notificationCount.setVisibility(View.VISIBLE);
			notificationsIcon.setVisibility(View.VISIBLE);
			notificationCount.setText("" + count);
		} else
		{
			notificationCount.setVisibility(View.INVISIBLE);
			notificationsIcon.setVisibility(View.INVISIBLE);
			notificationCount.setText("0");
		}
	}

	public class NotificationsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updateNotificationsInfo();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

}
