package es.usc.citius.servando.android.app.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.agenda.ProtocolEngineServiceBinder;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.ServandoIntent;
import es.usc.citius.servando.android.app.fragments.MedicalActionFragmentMgr;
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.models.protocol.MedicalAction;
import es.usc.citius.servando.android.models.protocol.MedicalActionExecution;
import es.usc.citius.servando.android.models.protocol.MedicalActionMgr;
import es.usc.citius.servando.android.ui.ActionExecutionViewFactory;
import es.usc.citius.servando.android.ui.FragmentViewMenuItem;
import es.usc.citius.servando.android.ui.NotificationMgr;
import es.usc.citius.servando.android.ui.ServandoService;
import es.usc.citius.servando.android.ui.ServiceFragmentView;
import es.usc.citius.servando.android.ui.ServiceFragmentView.ServiceFragmentCloseListener;

/**
 * TODO: Comment
 * 
 * @author Ángel Piñeiro
 * 
 */
public class SwitcherActivity extends FragmentActivity implements ServiceFragmentCloseListener {

	private static final String DEBUG_TAG = SwitcherActivity.class.getSimpleName();
	/**
	 * Servando paltform logger for this class
	 */
	private static final ILog log = ServandoLoggerFactory.getLogger(SwitcherActivity.class);

	private ServiceFragmentView view;
	private ImageButton notificationsIcon;
	private ImageButton optionsMenuButton;
	private TextView notificationCount;
	private TextView serviceNameTv;
	private NotificationsReceiver notificationReceiver;
	private ApplicationExitReceiver exitReceiver;

	private MedicalActionExecution currentExecution;

	private int currentActionId;

	private String previousUid = "";

	private List<FragmentViewMenuItem> menuItems = null;

	private boolean optionsMenuPrepared;

	int uniqueId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swither_layout);
		initializeActionView(getIntent());
		registerBroadcastReceivers();
		checkVoiceRecognition();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		Log.d(DEBUG_TAG, "onNewIntent: " + String.valueOf(intent != null));

		super.onNewIntent(intent);
		initializeActionView(intent);
	}

	private void initializeActionView(Intent intent)
	{

		if (intent == null)
		{
			return;
		}

		log.debug("Intent " + intent.toString());

		uniqueId = intent.getIntExtra("action_id", -1);
		String actionType = intent.getStringExtra("action_type");

		log.debug("Action type " + actionType);
		log.debug("Action id " + uniqueId);

		if (uniqueId != -1 && uniqueId != currentActionId)
		{
			currentExecution = ProtocolEngineServiceBinder.getInstance().getProtocolEngine().getExecutingAction(uniqueId);

			if (currentExecution != null)
			{
				// Log.d(DEBUG_TAG, "Current execution: " + currentExecution.toString());
				// new Timer().schedule(new TimerTask()
				// {
				// @Override
				// public void run()
				// {
				// currentExecution.setResources(PlatformResources.with(Available.NONE));
				// }
				// }, 10000);

				actionType = currentExecution.getAction().getId();
				initContainer(actionType, currentExecution);
				currentActionId = uniqueId;

			} else
			{
				Log.d(DEBUG_TAG, "no executiong action was found with id " + uniqueId);
			}

			return;
		}

		if (actionType != null)
		{
			initContainer(actionType, null);
		}
	}

	private void initContainer(String actionType, MedicalActionExecution exec)
	{

		MedicalAction a = MedicalActionMgr.getInstance().getMedicalAction(actionType);

		log.debug("init container");

		String uid = "" + (exec != null ? exec.getUniqueId() : actionType);

		notificationsIcon = (ImageButton) findViewById(R.id.home_notifications_icon);
		notificationCount = (TextView) findViewById(R.id.home_notifications_count);
		serviceNameTv = (TextView) findViewById(R.id.service_title_tv);
		optionsMenuButton = (ImageButton) findViewById(R.id.action_menu_button);

		optionsMenuButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				openOptionsMenu();
			}
		});

		// Remove ready notification if any
		if (uniqueId != -1)
		{
			// MedicalActionExecutor.removeActionReadyNotification(uniqueId, this);
		}

		if (!uid.equals(previousUid))
		{

			if (!MedicalActionFragmentMgr.getInstance().contains(uid))
			{
				ActionExecutionViewFactory factory = a.getViewFactory();
				log.debug("UID: " + uid + ", id: " + a.getId() + ", factory: " + (factory != null));

				ServiceFragmentView v = factory.createView(a.getId());

				log.debug("View: " + (v != null));

				MedicalActionFragmentMgr.getInstance().addFragment(uid, v);
			}

			ServiceFragmentView newView = MedicalActionFragmentMgr.getInstance().getFragment(uid);
			newView.setOnCloseListener(this);
			newView.setExecution(exec);
			menuItems = newView.getMenuItems();

			FragmentManager fMgr = getSupportFragmentManager();
			FragmentTransaction ft = fMgr.beginTransaction();
			ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			if (view != null)
			{
				ft.remove(view);
			}
			ft.add(R.id.container, newView);
			ft.commitAllowingStateLoss();

			setServiceTitle(a.getDisplayName());
			view = newView;
			previousUid = uid;
		}
	}

	private void registerBroadcastReceivers()
	{
		IntentFilter notificationFIlter = new IntentFilter(ServandoService.NOTIFICATIONS_UPDATE);
		notificationReceiver = new NotificationsReceiver();
		this.registerReceiver(notificationReceiver, notificationFIlter);

		IntentFilter exitFilter = new IntentFilter(ServandoIntent.ACTION_NOTIFICATIONS_UPDATE);
		exitReceiver = new ApplicationExitReceiver();
		this.registerReceiver(exitReceiver, exitFilter);
	}

	public void setServiceTitle(String title)
	{
		serviceNameTv.setText(title);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			updateNotificationsInfo();
			view.setFocus(true);
		} else
		{
			view.setFocus(false);
		}
	}

	public void onClickNotifications(View v)
	{
		Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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

	@Override
	protected void onDestroy()
	{
		this.unregisterReceiver(notificationReceiver);
		this.unregisterReceiver(exitReceiver);
		super.onDestroy();
	}

	public class NotificationsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updateNotificationsInfo();
		}
	}

	public class ApplicationExitReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Log.d(DEBUG_TAG, "Exit broadcast received");
			finish();
		}
	}

	public void onClickHome(View v)
	{
		goHome(this);
	}

	public void onClickAbout(View v)
	{
		Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
	}

	public void goHome(Context context)
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		final Intent intent = new Intent(context, homeActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	@Override
	public void onCloseFragment()
	{
		// TODO MedicalActionFragmentMgr.getInstance().removeFragment(actionId);
		finish();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (!optionsMenuPrepared)
		{
			menu.clear();

			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.actions_menu, menu);

			if (menuItems != null)
			{
				for (FragmentViewMenuItem m : menuItems)
				{
					MenuItem i = menu.add(0, m.getItemId(), menuItems.indexOf(m), m.getTitle());
					i.setOnMenuItemClickListener(m.getOnClickListener());
					if (m.hasIcon())
					{
						i.setIcon(m.getIconId());
					}
				}
			} else
			{
				System.out.println("No menuitems found!");
			}
			optionsMenuPrepared = true;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			if (!view.onCloseByUser())
			{
				return super.onKeyDown(keyCode, event);
			} else
			{
				return true;
			}
		} else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	// private boolean isNumber(String word)
	// {
	// boolean isNumber = false;
	// try
	// {
	// Integer.parseInt(word.toLowerCase());
	// isNumber = true;
	// } catch (NumberFormatException e)
	// {
	// isNumber = false;
	// }
	// return isNumber;
	// }

	/**
	 * @return the currentExecution
	 */
	public MedicalActionExecution getCurrentExecution()
	{
		return currentExecution;
	}

	/**
	 * @param currentExecution the currentExecution to set
	 */
	public void setCurrentExecution(MedicalActionExecution currentExecution)
	{
		this.currentExecution = currentExecution;
	}

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

	public void checkVoiceRecognition()
	{
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0)
		{

			Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void speak()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Specify the calling package to identify your application
		intent.putExtra("calling_package", "es.usc.citius.servando.android.app");
		// Specify the calling package to identify your application
		// Display an hint to the user about what he should say.
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Fala");

		// Given an hint to the recognizer about what the user is going to say
		// There are two form of language model available
		// 1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		// 2.LANGUAGE_MODEL_FREE_FORM : If not sure about the words or phrases and its domain.
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		int noOfMatches = 2;

		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
		// Start the Voice recognizer activity for the result.
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
		{
			// If Voice recognition is successful then it returns RESULT_OK
			if (resultCode == RESULT_OK)
			{

				ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				if (!textMatchList.isEmpty())
				{
					// If first Match contains the 'search' word
					// Then start web search.
					if (textMatchList.get(0).contains("search"))
					{

						String searchQuery = textMatchList.get(0);
						searchQuery = searchQuery.replace("search", "");
						Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
						search.putExtra(SearchManager.QUERY, searchQuery);
						startActivity(search);
					} else
					{

						showToastMessage(textMatchList.get(0) != null ? textMatchList.get(0) : "");
					}

				}
				// Result code for various error.
			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR)
			{
				showToastMessage("Audio Error");
			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR)
			{
				showToastMessage("Client Error");
			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR)
			{
				showToastMessage("Network Error");
			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH)
			{
				showToastMessage("No Match");
			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR)
			{
				showToastMessage("Server Error");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	void showToastMessage(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

}
