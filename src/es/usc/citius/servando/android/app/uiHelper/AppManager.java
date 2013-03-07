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
import es.usc.citius.servando.android.agenda.ProtocolEngine;
import es.usc.citius.servando.android.app.RestartReceiver;
import es.usc.citius.servando.android.app.ServandoIntent;
import es.usc.citius.servando.android.medim.ui.MedimBackgroundService;
import es.usc.citius.servando.android.ui.NotificationMgr;
import es.usc.citius.servando.android.ui.ServandoService;

/**
 * 
 * @author Ángel Piñeiro
 * 
 */
public class AppManager {

	private static final String DEBUG_TAG = AppManager.class.getSimpleName();


	private static Context lastContext;
	/**
	 * Close aplication
	 * 
	 * @param ctx
	 */
	public static void closeApplication(Context ctx)
	{
		// Stop platform
		ServandoPlatformFacade.getInstance().stop(ctx);

		// Stop background service
		ctx.stopService(new Intent(ctx, ServandoService.class));
		ctx.stopService(new Intent(ctx, MedimBackgroundService.class));
		ctx.stopService(new Intent(ctx, ProtocolEngine.class));

		NotificationMgr.getInstance().setServandoService(null);
		// Send broadcast to close all open activities
		Log.d(DEBUG_TAG, "Sending exit broadcast");
		ctx.sendBroadcast(new Intent(ServandoIntent.ACTION_APP_EXIT));

		// Schedule app proccess stop in 3 secs
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				Log.d(DEBUG_TAG, "Stopping app task.");
				Process.killProcess(Process.myPid());
			}
		}, 2000);
	}

	public static void restartApplication(Context ctx)
	{

		closeApplication(ctx);
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar objectx
		cal.add(Calendar.SECOND, 5);
		Intent intent = new Intent(ctx, RestartReceiver.class);
		// In reality, you would want to have a static variable for the request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(ctx, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}



}
