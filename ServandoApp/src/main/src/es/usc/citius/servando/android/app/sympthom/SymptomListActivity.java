package es.usc.citius.servando.android.app.sympthom;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.activities.HomeActivity;
import es.usc.citius.servando.android.app.activities.PatientHomeActivity;

public class SymptomListActivity extends Activity implements OnClickListener {

	private List<Symptom> symptoms;
	private ListView list;
	private SympthomAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sympthom_list);

		SymptomStore.getInstance().loadSymptoms(this);

		symptoms = SymptomStore.getInstance().getAll();

		list = (ListView) findViewById(R.id.sympthom_list_view);

		adapter = new SympthomAdapter(this, R.layout.agenda_list_item, symptoms);

		list.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.sympthom_list, menu);
		return true;
	}

	public void onClickHome(View v)
	{
		goHome(this);
	}

	public void goHome(Context context)
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		final Intent intent = new Intent(context, homeActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
		finish();
	}

	public void goSymptom(Symptom s, Context context)
	{

		final Intent intent = new Intent(context, SympthomActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra("symptom_id", s.getId());
		context.startActivity(intent);
		finish();
	}

	public void toast(String msg)
	{
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private class SympthomAdapter extends ArrayAdapter<Symptom> {

		public SympthomAdapter(Context context, int textViewResourceId, List<Symptom> sympthoms)
		{
			super(context, textViewResourceId, sympthoms);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final Symptom sympthom = symptoms.get(position);

			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = vi.inflate(R.layout.sympthom_list_item, null);
			v.setTag(sympthom);
			v.setOnClickListener(SymptomListActivity.this);

			TextView name = (TextView) v.findViewById(R.id.sympthom_name);
			name.setText(sympthom.getName());

			return v;
		}
	}

	@Override
	public void onClick(View v)
	{
		if (v != null && v.getTag() != null && v.getTag() instanceof Symptom)
		{
			goSymptom((Symptom) v.getTag(), SymptomListActivity.this);
		}
	}

}
