package es.usc.citius.servando.android.app.activities;

import java.util.ArrayList;
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
import android.widget.ImageView;
import android.widget.TextView;
import es.usc.citius.servando.android.app.Advice;
import es.usc.citius.servando.android.app.R;

public class AdvicesListActivity extends ListActivity implements View.OnClickListener {

	private AdviceAdapter adapter;

	@Override
	public void onCreate(Bundle icicle)
	{
		String message = "Ola Ángel, últimamente estas tomando demasiado sal. Por favor, procura reducir o uso de sal nas comidas";

		List<Advice> advices = new ArrayList<Advice>();
		advices.add(new Advice(message, true));
		advices.add(new Advice(message, true));
		advices.add(new Advice(
				"Ángel, onte esqueciches tomar a medicación pola mañá. Recorda que debes tomar as pastillas sempre que Servando cho indique.", true));
		advices.add(new Advice(message, false));

		super.onCreate(icicle);
		setContentView(R.layout.advices_layout);
		adapter = new AdviceAdapter(this, R.layout.advice_list_item, advices);

		setListAdapter(adapter);
		getListView().setOnItemClickListener(new AdviceClickListener());
	}

	private class AdviceClickListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
		}

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			adapter.notifyDataSetChanged();
		}
	}

	private class AdviceAdapter extends ArrayAdapter<Advice> {

		List<Advice> mAdvices;

		public AdviceAdapter(Context context, int textViewResourceId, List<Advice> advs)
		{
			super(context, textViewResourceId, advs);
			mAdvices = advs;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			View v = convertView;

			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.advice_list_item, null);
			}

			Advice o = mAdvices.get(position);

			if (o != null)
			{
				TextView msg = (TextView) v.findViewById(R.id.message_text);
				msg.setText(o.getContent());

				ImageView visibility = (ImageView) v.findViewById(R.id.visible_layer);
				if (!o.isViewed())
				{
					visibility.setVisibility(View.INVISIBLE);
				}

			}
			return v;
		}
	}

	public void onClickHome(View v)
	{
		final Intent intent = new Intent(this, PatientHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}

	@Override
	public void onClick(View v)
	{

		// final int viewId = v.getId();
		//
		// switch (viewId) {
		// case R.id.clear_notifications_button:
		// NotificationMgr.getInstance().clear();
		// adapter.notifyDataSetChanged();
		// break;
		// default:
		// break;
		// }

	}

}
