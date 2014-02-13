package es.usc.citius.servando.android.app.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.activities.SplashActivity;

public class UpdateWidgetService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		int count = intent.getIntExtra("notification_count", -1);

		// String aName = intent.getStringExtra("next_action_name");
		// String aTime = intent.getStringExtra("next_action_time");

		updateWidget(count);
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateWidget(int count) {

		// Toast.makeText(this, "Intent: " + count, Toast.LENGTH_SHORT).show();
		String notifications;

		if (count < 0) {
			notifications = "(Stopped)";
		} else if (count == 0) {
			notifications = "";
		} else {
			notifications = count + (count != 1 ? " notifications" : " notification");
		}

		AppWidgetManager manager = AppWidgetManager.getInstance(this);
		RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget_initial_layout);
		view.setTextViewText(R.id.widget_notification_count, notifications);
		ComponentName thisWidget = new ComponentName(this, WidgetProvider.class);
		Intent clickIntent = new Intent(this.getApplicationContext(), SplashActivity.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

		manager.updateAppWidget(thisWidget, view);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
