package es.usc.citius.servando.android.app.uiHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import es.usc.citius.servando.android.app.StartServandoActivityInBg;

public class PlatformBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			final Intent launchApp = new Intent(context, StartServandoActivityInBg.class);
			launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			context.startActivity(launchApp);
        }
    }
}