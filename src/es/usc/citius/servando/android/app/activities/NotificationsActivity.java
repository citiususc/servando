package es.usc.citius.servando.android.app.activities;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.ui.Notification;
import es.usc.citius.servando.android.ui.NotificationMgr;

public class NotificationsActivity extends ListActivity implements View.OnClickListener {

	private NotificationAdapter adapter;
	private Button clearButton;

	@Override
	public void onCreate(Bundle icicle)
	{

		super.onCreate(icicle);
		setContentView(R.layout.notifications_layout);
		adapter = new NotificationAdapter(this, R.layout.notification_list_item, NotificationMgr.getInstance().getNotifications());
		clearButton = (Button) findViewById(R.id.clear_notifications_button);
		clearButton.setOnClickListener(this);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(new NotificationClickListener());
	}

	private class NotificationClickListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{

			Notification n = NotificationMgr.getInstance().remove(position);
			updateClearButtonVisibility();
			adapter.notifyDataSetChanged();
			if (n.getMedicalActionId() != null)
			{
				Intent intent = new Intent(getApplicationContext(), SwitcherActivity.class);
				intent.putExtra("action_id", n.getMedicalActionId());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
				finish();
			}
		}

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			updateClearButtonVisibility();
			adapter.notifyDataSetChanged();
		}
	}

	private void updateClearButtonVisibility()
	{
		if (NotificationMgr.getInstance().getCount() > 0)
		{
			clearButton.setVisibility(View.VISIBLE);
		} else
		{
			clearButton.setVisibility(View.INVISIBLE);
		}
	}

	private class NotificationAdapter extends ArrayAdapter<Notification> {

		public NotificationAdapter(Context context, int textViewResourceId, List<Notification> notifications)
		{
			super(context, textViewResourceId, notifications);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			View v = convertView;

			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.notification_list_item, null);
			}

			Notification o = NotificationMgr.getInstance().getNotifications().get(position);

			if (o != null)
			{
				TextView actionName = (TextView) v.findViewById(R.id.notification_title);
				if (actionName != null)
				{
					actionName.setText(o.getTitle());
				}
				TextView actionDesc = (TextView) v.findViewById(R.id.notification_description);
				if (actionDesc != null)
				{
					// if(actionDesc.length()>40)
					actionDesc.startAnimation(AnimationUtils.loadAnimation(NotificationsActivity.this, R.anim.anim_text_scroll));
					actionDesc.setText(o.getDescription());
				}
			}

			return v;
		}
	}

	public void onClickHome(View v)
	{
		final Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
		finish();
	}

	@Override
	public void onClick(View v)
	{

		final int viewId = v.getId();

		switch (viewId) {
		case R.id.clear_notifications_button:
			NotificationMgr.getInstance().clear();
			adapter.notifyDataSetChanged();
			break;
		default:
			break;
		}

	}

}
