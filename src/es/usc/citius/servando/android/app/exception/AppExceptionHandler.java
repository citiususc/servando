package es.usc.citius.servando.android.app.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.util.Log;
import es.usc.citius.servando.android.app.uiHelper.AppManager;
import es.usc.citius.servando.android.settings.StorageModule;

public class AppExceptionHandler implements UncaughtExceptionHandler {

	public static Throwable lastException = null;

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
			Log.d("AppExceptionHandler", "Handling uncaught error", exception);

			File trace = new File(StorageModule.getInstance().getPlatformLogsPath() + "/crash_trace.txt");

			// error in bucle
			if (trace.exists())
			{
				AppManager.closeApplication(app);
			} else
			{
				PrintWriter print = new PrintWriter(new FileWriter(trace));
				exception.printStackTrace(print);
				print.flush();
				print.close();
				Log.d("AppExceptionHandler", "Trace wrote to " + trace.getAbsolutePath().toString());
			}

			// lastException = exception;
			// Intent intent = new Intent(app, CrashActivity.class);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// app.startActivity(intent);

			// Looper.loop();
			// }
			// }.start();
		} catch (Exception e)
		{
			Log.e("AppExceptionHandler", "Uncaught error", e);
		}
	}
	//
	// void showToast(Throwable exception)
	// {
	// Toast.makeText(app, "Uncaught error", Toast.LENGTH_SHORT).show();
	// Log.d("AppExceptionHandler", "Uncaught error", exception);
	// }
	//
	// void sendMail(final Throwable exception)
	// {
	// try
	// {
	// File servandoLogFile = new File(StorageModule.getInstance().getPlatformLogsPath() + "/servando.log");
	// //
	// // Intent email = new Intent(Intent.ACTION_SEND);
	// // email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// // email.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// // email.putExtra(Intent.EXTRA_EMAIL, new String[] { "servandoplatform@gmail.com" });
	// // email.putExtra(Intent.EXTRA_SUBJECT, "[CRASH_REPORT]");
	// // email.putExtra(Intent.EXTRA_TEXT, "Device ID: " +
	// // ServandoPlatformFacade.getInstance().getPatient().getName());
	// // app.startActivity(Intent.createChooser(email, "Send report"));
	//
	// StringWriter sw = new StringWriter();
	// exception.printStackTrace(new PrintWriter(sw));
	// sw.flush();
	// sw.close();
	//
	// Intent intent = new Intent(Intent.ACTION_SENDTO);
	// intent.setType("text/plain");
	// intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "servandoplatform@gmail.com" });
	// intent.putExtra(Intent.EXTRA_SUBJECT, "[SERVANDO_CRASH_REPORT]");
	// intent.putExtra(Intent.EXTRA_TEXT, "Patient ID: " + ServandoPlatformFacade.getInstance().getPatient().getName() +
	// "\n\nStackTrace: \n\n"
	// + sw.getBuffer().toString());
	//
	//
	// File trace = File.createTempFile("trace", ".log");
	// PrintWriter print = new PrintWriter(new FileWriter(trace));
	// exception.printStackTrace(print);
	// print.flush();
	// print.close();
	//
	// // intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(trace));
	//
	// if (servandoLogFile.exists())
	// {
	// intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(servandoLogFile));
	// }
	//
	// Intent chooserIntent = Intent.createChooser(intent, "Send report");
	// chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	// chooserIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
	//
	// app.startActivity(chooserIntent);
	//
	// } catch (Exception e)
	// {
	// Log.e("AppExceptionHandler", "Uncaught error", e);
	// }
	// }
}
