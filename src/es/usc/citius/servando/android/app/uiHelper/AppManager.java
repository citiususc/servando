package es.usc.citius.servando.android.app.uiHelper;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.agenda.ServandoBackgroundService;
import es.usc.citius.servando.android.app.RestartReceiver;
import es.usc.citius.servando.android.app.ServandoIntent;
import es.usc.citius.servando.android.medim.ui.MedimBackgroundService;

/**
 * 
 * @author Ángel Piñeiro
 * 
 */
public class AppManager {

	private static final String DEBUG_TAG = AppManager.class.getSimpleName();

	// private static Context lastContext;

	/**
	 * Close aplication
	 * 
	 * @param ctx
	 */
	public static void closeApplication(Context ctx)
	{
		// Stop platform
		try
		{
			Log.d(DEBUG_TAG, "Closing application...");
			ServandoPlatformFacade.getInstance().stop(ctx);
			// Stop background service
			ctx.stopService(new Intent(ctx, ServandoBackgroundService.class));
			ctx.stopService(new Intent(ctx, MedimBackgroundService.class));
			// Send broadcast to close all open activities
			Log.d(DEBUG_TAG, "Sending exit broadcast");
			ctx.sendBroadcast(new Intent(ServandoIntent.ACTION_APP_EXIT));

		} catch (Exception e)
		{
			Log.e("AppManager", "Error closing app", e);
		}

		// Schedule app proccess stop in 3 secs
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				Log.d(DEBUG_TAG, "Stopping app task.");
				Process.killProcess(Process.myPid());
			}
		}, 2500);


	}

	public static void restartApplication(Context ctx)
	{

		Log.d(DEBUG_TAG, "Restarting application...");

		closeApplication(ctx);
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar objectx
		cal.add(Calendar.SECOND, 4);
		Intent intent = new Intent(ctx, RestartReceiver.class);
		// In reality, you would want to have a static variable for the request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(ctx, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}

}
