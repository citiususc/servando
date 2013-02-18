package es.usc.citius.servando.android.app.activities;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.models.protocol.MedicalAction;
import es.usc.citius.servando.android.models.services.ServiceManager;

public class MedicalActionsActivity extends ListActivity {

	List<MedicalAction> actions;

	private String currentServiceId;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.medical_actions_layout);
		initializeActionList(getIntent());
		getListView().setOnItemClickListener(new MedicalActionClickListener());
	}

	private void initializeActionList(Intent intent)
	{

		if (intent == null)
		{
			return;
		}

		String serviceId = intent.getStringExtra("service_id");
		if (serviceId != null && !serviceId.equals(currentServiceId))
		{
			actions = ServiceManager.getInstance().getRegisteredService(serviceId).getProvidedActions();
			MedicalActionAdapter adapter = new MedicalActionAdapter(this, R.layout.medical_action_list_item, actions);
			setListAdapter(adapter);
			currentServiceId = serviceId;
		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		initializeActionList(intent);
	}

	private class MedicalActionClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			MedicalAction action = actions.get(position);
			Intent intent = new Intent(getApplicationContext(), SwitcherActivity.class);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("action_type", action.getId());
			startActivity(intent);
			finish();
		}
	}

	private class MedicalActionAdapter extends ArrayAdapter<MedicalAction> {

		private List<MedicalAction> items;

		public MedicalActionAdapter(Context context, int textViewResourceId, List<MedicalAction> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;

			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.medical_action_list_item, null);
			}

			MedicalAction o = items.get(position);
			if (o != null)
			{
				TextView actionName = (TextView) v.findViewById(R.id.action_name);

				if (actionName != null)
				{
					actionName.setText(o.getDisplayName());
				}
			}
			return v;
		}
	}

	public void onClickHome(View v)
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		final Intent intent = new Intent(this, homeActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
		finish();
	}

}
