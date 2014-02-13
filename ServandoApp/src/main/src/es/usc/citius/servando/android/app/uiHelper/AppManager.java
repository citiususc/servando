package es.usc.citius.servando.android.app.uiHelper;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.agenda.ServandoBackgroundService;
import es.usc.citius.servando.android.alerts.AlertMsg;
import es.usc.citius.servando.android.alerts.AlertMsg.Builder;
import es.usc.citius.servando.android.alerts.AlertType;
import es.usc.citius.servando.android.app.RestartReceiver;
import es.usc.citius.servando.android.app.ServandoIntent;
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.medim.ui.MedimBackgroundService;

/**
 * 
 * @author Ángel Piñeiro
 * 
 */
public class AppManager {

	private static final String DEBUG_TAG = AppManager.class.getSimpleName();
	private static ILog log = ServandoLoggerFactory.getLogger(AppManager.class);

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
			log.debug("Closing application...");

			Builder builder = new AlertMsg.Builder();
			AlertMsg a = builder.setType(AlertType.SYSTEM_EVENT).setDisplayName("Cierre").setDescription("Servando se ha detenido").create();
			// uncomment for sending
			log.debug("Sending close alert...");
			ServandoPlatformFacade.getInstance().alert(a);

			ServandoPlatformFacade.getInstance().stop(ctx);
			// Stop background service
			ctx.stopService(new Intent(ctx, ServandoBackgroundService.class));
			ctx.stopService(new Intent(ctx, MedimBackgroundService.class));
			// Send broadcast to close all open activities
			log.debug("Sending exit broadcast to all open activities");
			ctx.sendBroadcast(new Intent(ServandoIntent.ACTION_APP_EXIT));

		} catch (Exception e)
		{
			log.error("An error ocurred closing app", e);
		}

		// Schedule app proccess stop in 3 secs
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				log.debug("Application process closed.");
				Process.killProcess(Process.myPid());
			}
		}, 3000);


	}

	public static void restartApplication(Context ctx)
	{

		log.debug("Restarting application...");

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
