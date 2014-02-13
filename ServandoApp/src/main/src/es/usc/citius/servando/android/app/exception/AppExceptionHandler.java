package es.usc.citius.servando.android.app.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;
import es.usc.citius.servando.android.app.uiHelper.AppManager;
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.settings.StorageModule;

public class AppExceptionHandler implements UncaughtExceptionHandler {

	public static Throwable lastException = null;
	public static ILog log = ServandoLoggerFactory.getLogger(AppExceptionHandler.class);

	Application app;

	public AppExceptionHandler(Application app)
	{
		this.app = app;
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable exception)
	{
		try
		{
			log.error("An unexpected error ocurred", exception);
			File trace = new File(StorageModule.getInstance().getPlatformLogsPath() + "/crash_trace.txt");

			if (!trace.exists())
			{
				PrintWriter print = new PrintWriter(new FileWriter(trace));
				exception.printStackTrace(print);
				print.flush();
				print.close();
				Log.d("AppExceptionHandler", "Trace wrote to " + trace.getAbsolutePath().toString());
			}

		} catch (Exception e)
		{
			log.error("An exception ocurred handling uncaught error", e);
		} finally
		{
			AppManager.closeApplication(app);
		}
	}
}
