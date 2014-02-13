package es.usc.citius.servando.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import es.usc.citius.servando.android.app.activities.SplashActivity;

public class RestartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			Intent newIntent = new Intent(context, SplashActivity.class);
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(newIntent);

		} catch (Exception e)
		{

			Log.e("RestartReceiver", "An error ocurred restarting app ", e);

		}
	}

}
