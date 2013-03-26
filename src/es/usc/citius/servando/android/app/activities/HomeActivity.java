package es.usc.citius.servando.android.app.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import es.usc.citius.servando.android.agenda.ProtocolEngine;
import es.usc.citius.servando.android.agenda.ProtocolEngineServiceBinder;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.uiHelper.AppManager;
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.models.services.IPlatformService;
import es.usc.citius.servando.android.models.services.ServiceManager;
import es.usc.citius.servando.android.ui.Iconnable;
import es.usc.citius.servando.android.ui.NotificationMgr;
import es.usc.citius.servando.android.ui.ServandoService;
import es.usc.citius.servando.android.util.UiUtils;

/**
 * ServandoPlatform home activity
 */
public class HomeActivity extends Activity {

	/**
	 * Servando paltform logger for this class
	 */
	private static final ILog log = ServandoLoggerFactory.getLogger(HomeActivity.class);

	GridLayout grid;
	ImageButton notificationsIcon;
	TextView notificationCount;

	private NotificationsReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		grid = (GridLayout) findViewById(R.id.grid);
		notificationsIcon = (ImageButton) findViewById(R.id.home_notifications_icon);
		notificationCount = (TextView) findViewById(R.id.home_notifications_count);
		addServiceLaunchers();
		registerNotificationReceiver();

		// NotificationMgr.getInstance().add(new Notification("ECG","Sensor disconnected",
		// "One or more sensors are disconnected"), true);
		// NotificationMgr.getInstance().add(new Notification("ECG","Low battery",
		// "Electrocardiograph battery level is very low"), true);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			updateNotifications();
		}
	}

	private void registerNotificationReceiver()
	{
		// TODO Auto-generated method stub
		IntentFilter notificationFIlter = new IntentFilter(ServandoService.NOTIFICATIONS_UPDATE);
		receiver = new NotificationsReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, notificationFIlter);
	}

	private void updateNotifications()
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

	private void addServiceLaunchers()
	{

		for (IPlatformService service : ServiceManager.getInstance().getRegisteredServices().values())
		{
			if (service instanceof Iconnable)
			{
				addLauncher(((Iconnable) service).getIconResourceId(), ((Iconnable) service).getIconText(), service.getId());
			}
		}
	}

	private void addLauncher(int drawableId, String text, final String serviceId)
	{
		Button launcher = (Button) getLayoutInflater().inflate(R.layout.dashboard_button, null);

		// launcher.setBackgroundDrawable(getResources().getDrawable(drawableId));
		// Drawable[] d = launcher.getCompoundDrawables();
		// d[1] = getResources().getDrawable(drawableId);
		// launcher.setCompoundDrawablePadding(25);
		launcher.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(drawableId), null, null);
		// launcher.setCompoundDrawables(d[0], d[1], d[2], d[3]);
		launcher.setText(text);
		launcher.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				onServiceSelected(serviceId);
			}
		});
		grid.addView(launcher);
	}

	protected void onServiceSelected(String serviceId)
	{
		Intent intent = new Intent(getApplicationContext(), MedicalActionsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("service_id", serviceId);
		startActivity(intent);
	}

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public void onClickAbout(View v)
	{
		Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void onClickAgenda(View v)
	{
		Intent intent = new Intent(getApplicationContext(), AgendaActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void onClickTesting(View v)
	{
		log.info("OnCLickTesting");
		ProtocolEngine agenda = ProtocolEngineServiceBinder.getInstance().getProtocolEngine();
		log.info(agenda.getProtocol().getDescription());
		log.info("Loaded acions: " + agenda.getProtocol().getActions().size());

		try
		{
			agenda.start();

		} catch (Exception e)
		{
			log.error("xDDDDD... era visto!", e);
		}

	}

	public void onClickNotifications(View v)
	{
		Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		int id = item.getItemId();

		// Handle item selection
		if (id == R.id.menu_settings)
		{
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);

		}
		// else if (id == R.id.menu_help)
		// {
		// UiUtils.showToast("Help selected", this);
		// } else if (id == R.id.menu_close)
		// {
		// exit();
		// } else if (id == R.id.menu_agenda)
		// {
		// ProtocolEngine agenda = ProtocolEngineServiceBinder.getInstance().getProtocolEngine();
		// agenda.start();
		// }
		else
		{
			UiUtils.showToast("Unknown option", this);
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void exit()
	{
		AppManager.closeApplication(this);
		finish();
	}

	/**
	 * Build a button
	 * 
	 * @param text
	 * @param resId
	 * @param listener
	 * @return
	 */
	public Button buildButton(String text, int resId, OnClickListener listener)
	{
		Button b = new Button(this, null, R.style.HomeButton);
		b.setText(text);
		b.setOnClickListener(listener);
		return b;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			grid.setColumnCount(4);
			// Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			grid.setColumnCount(2);
			// Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class NotificationsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updateNotifications();
		}
	}

}
