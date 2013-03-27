package es.usc.citius.servando.android.app.activities;

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import es.usc.citius.servando.android.advices.Advice;
import es.usc.citius.servando.android.advices.DailyReport;
import es.usc.citius.servando.android.advices.ServandoAdviceMgr;
import es.usc.citius.servando.android.advices.storage.SQLiteAdviceDAO;
import es.usc.citius.servando.android.app.R;

public class AdvicesListActivity extends ListActivity implements View.OnClickListener {

	private AdviceAdapter adapter;
	private List<Advice> advices;

	private boolean showSeenMessages = false;

	@Override
	public void onCreate(Bundle icicle)
	{

		super.onCreate(icicle);
		setContentView(R.layout.advices_layout);

		advices = DailyReport.getInstance().getAll();
		adapter = new AdviceAdapter(this, R.layout.advice_list_item, advices);
		setListAdapter(adapter);

		getListView().setOnItemClickListener(new AdviceClickListener());

		showOrHideNoAdvicesMsg();
	}

	private void showOrHideNoAdvicesMsg()
	{

		View noAdviceMsg = findViewById(R.id.no_advices_msg);
		if (DailyReport.getInstance().getNotSeen().size() == 0 && !showSeenMessages)
		{
			noAdviceMsg.setVisibility(View.VISIBLE);
		} else
		{
			noAdviceMsg.setVisibility(View.INVISIBLE);
		}

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

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.advices_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		int id = item.getItemId();

		// Handle item selection
		if (id == R.id.menu_advices_show_seen)
		{
			showSeenMessages = !showSeenMessages;
			adapter.notifyDataSetChanged();
			showOrHideNoAdvicesMsg();
		}

		return true;
	}

	private class AdviceAdapter extends ArrayAdapter<Advice> {

		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, HH:mm");

		public AdviceAdapter(Context context, int textViewResourceId, List<Advice> advs)
		{
			super(context, textViewResourceId, advs);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			final Advice o = advices.get(position);
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View v;
			if (!o.isSeen() || showSeenMessages)
			{
				v = vi.inflate(R.layout.advice_list_item, null);
				v.setTag(o);
				Log.d("TAG", "Position: " + position + ", " + o.getSender() + ": " + o.getMsg());

				TextView from = (TextView) v.findViewById(R.id.message_intro);
				TextView msg = (TextView) v.findViewById(R.id.message_text);
				TextView when = (TextView) v.findViewById(R.id.message_time);
				View transparentLayer = (View) v.findViewById(R.id.visible_layer);
				ImageButton seen = (ImageButton) v.findViewById(R.id.seenButton);


				from.setText(o.getSender() + ":");
				msg.setText(o.getMsg());
				when.setText(sdf.format(o.getDate()));

				if (o.isSeen())
				{
					transparentLayer.setVisibility(View.VISIBLE);
					seen.setVisibility(View.INVISIBLE);
				} else
				{
					transparentLayer.setVisibility(View.INVISIBLE);
					seen.setVisibility(View.VISIBLE);
					v.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							onClickAdvice(o);
						}
					});
				}
			}

			else
			{
				v = vi.inflate(R.layout.seen_advice_list_item, null);
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
		Advice a = (Advice) v.getTag();
		a.setSeen(true);
		SQLiteAdviceDAO.getInstance().markAsSeen(a);
		adapter.notifyDataSetChanged();
	}

	private void onClickAdvice(Advice a)
	{
		a.setSeen(true);
		SQLiteAdviceDAO.getInstance().markAsSeen(a);
		adapter.notifyDataSetChanged();
		// se o mensaxe seleccionado é o mesmo ca o do home, eliminámolo.
		if (ServandoAdviceMgr.getInstance().getHomeAdvice() != null && a.getId() == ServandoAdviceMgr.getInstance().getHomeAdvice().getId())
		{
			ServandoAdviceMgr.getInstance().getHomeAdvice().setSeen(true);
		}
		showOrHideNoAdvicesMsg();
	}

}
