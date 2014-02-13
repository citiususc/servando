package es.usc.citius.servando.android.app.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.Duration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewAnimator;
import android.widget.ViewSwitcher;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.agenda.ProtocolEngine;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.uiHelper.AgendaGridHelper;
import es.usc.citius.servando.android.models.protocol.MedicalActionExecution;
import es.usc.citius.servando.android.ui.animation.AnimationStore;

public class AgendaListActivity extends Activity {

	private static int GRID_ROWS = 24 * 2;
	private static int GRID_COLS = 6;
	private static int GRID_COLS_LANDSCAPE = 10;

	String[] actions;
	ListView agendaList;
	ListView dayList;

	Button listViewButton;
	Button dayViewButton;
	Button backButton;

	int screenWidth;

	GridLayout grid;

	ViewAnimator pager;

	private AgendaGridHelper gridHelper;

	int orientation;

	private List<MedicalActionExecution> executions;

	// int[] events = new int[] { 0, 8, 8, 9, 9, 11, 13, 13, 16, 16, 17, 20, 23 };
	// float[] durations = new float[] { 8, 1, 3, 1, 2, 1, 3, 2, 1, 2, 4, 2, 1 };
	int[] events = new int[] { 0, 9, 9, 12 };
	float[] durations = new float[] { 8, 1, 1, 1 };

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.agenda_list_layout);
		agendaList = (ListView) findViewById(R.id.agenda_list);
		dayList = (ListView) findViewById(R.id.agenda_day);
		pager = (ViewAnimator) findViewById(R.id.pager);
		listViewButton = (Button) findViewById(R.id.agenda_listview_button);
		dayViewButton = (Button) findViewById(R.id.agenda_day_view_button);
		backButton = (Button) findViewById(R.id.backButton);

		grid = (GridLayout) findViewById(R.id.grid);
		orientation = getResources().getConfiguration().orientation;

		executions = ProtocolEngine.getInstance().getFilteredDayActions(new GregorianCalendar());
		initializeEventDurations();

		int needColumns = getNeedColumns();
		// Toast.makeText(this, "Columns: " + needColumns, Toast.LENGTH_SHORT).show();

		GRID_COLS = needColumns;

		gridHelper = new AgendaGridHelper(GRID_ROWS, GRID_COLS, false);// , orientation ==
																		// Configuration.ORIENTATION_LANDSCAPE ?
																		// GRID_COLS_LANDSCAPE : GRID_COLS, false);

		listViewButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (pager.getDisplayedChild() != 1)
				{
					pager.setInAnimation(AnimationStore.getInstance().getSlideInFromRigth());
					pager.setOutAnimation(AnimationStore.getInstance().getSlideOutToLeft());
					pager.setDisplayedChild(1);
				}
			}
		});
		dayViewButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (pager.getDisplayedChild() != 0)
				{
					pager.setInAnimation(AnimationStore.getInstance().getSlideInFromLeft());
					pager.setOutAnimation(AnimationStore.getInstance().getSlideOutToRigth());
					pager.setDisplayedChild(0);
				}
			}
		});
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (pager.getDisplayedChild() != 0)
				{
					pager.setInAnimation(AnimationStore.getInstance().getComeIn());
					pager.setOutAnimation(AnimationStore.getInstance().getGoBack());
					pager.setDisplayedChild(0);
				}
			}
		});

		dayList.setOnScrollListener(new OnScrollListener()
		{

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				Log.d("SCROLL", "scrollState: " + scrollState);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				Log.d("SCROLL", firstVisibleItem + " " + visibleItemCount);

			}
		});

		initializeAgendaList(getIntent());
		initializeAgendaDayList(getIntent());
		initializeGridLayout();

	}

	private void initializeEventDurations()
	{

		System.out.println("Initializing event durations: " + executions.size());

		int hour = GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY);

		events = new int[executions.size()];
		durations = new float[events.length];

		int i = 0;
		for (MedicalActionExecution exec : executions)
		{

			// int eventRow = Math.max(exec.getStartDate().get(Calendar.HOUR_OF_DAY), hour);
			int eventRow = exec.getStartDate().get(Calendar.HOUR_OF_DAY);
			long eventDuration = Duration.standardSeconds((int) exec.getTimeWindow()).getStandardHours();

			System.out.println(eventDuration);

			eventDuration = eventDuration > 0 ? eventDuration : 1;

			events[i] = eventRow;
			durations[i] = eventDuration;

			i++;
		}

		System.out.println(Arrays.toString(events));
		System.out.println(Arrays.toString(durations));
	}

	private int getNeedColumns()
	{

		int max = 0;
		int hourEvents = 0;

		// para cada hora
		for (int hour = 0; hour < 24; hour++)
		{
			// recorro os eventos e miro se coinciden
			for (int evt = 0; evt < events.length; evt++)
			{
				int evtStart = events[evt]; // 2
				int evtEnd = evtStart + (int) durations[evt]; // 5

				Log.d("COLUMNS", "Hour: " + hour + ", evtStart: " + evtStart + ", evtEnd: " + evtEnd);

				if (hour >= evtStart && hour < evtEnd)
				{
					hourEvents++;
					Log.d("COLUMNS", "Count :" + hourEvents);
				}
			}

			if (hourEvents > max)
			{
				max = hourEvents;
			}
			hourEvents = 0;
		}
		return max + 1;
	}

	private void initializeGridLayout()
	{

		int hour = GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY);

		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		int widthPixels = getResources().getDisplayMetrics().widthPixels;

		float heightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.day_view_height),
				getResources().getDisplayMetrics());

		float innerHeightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.day_view_inner_height),
				getResources().getDisplayMetrics());

		float widthInPx = widthPixels * 0.78f / (GRID_COLS - 1);

		float widthIn1Row = widthPixels * 0.18f;

		Log.d("WIDTH", "Px: " + widthPixels);

		// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.day_view_height),
		// getResources().getDisplayMetrics());

		grid.setRowCount(GRID_ROWS);
		grid.setColumnCount(orientation == Configuration.ORIENTATION_LANDSCAPE ? GRID_COLS_LANDSCAPE : GRID_COLS);

		// add hour labels to first column
		for (int i = 0; i < GRID_ROWS; i++)
		{
			if (i % 2 == 0)
			{
				Button tv = (Button) vi.inflate(R.layout.agenda_grid_time_labe_item, null);
				tv.setMinimumHeight((int) heightInPx);
				tv.setMinimumWidth((int) widthIn1Row);
				tv.setWidth((int) widthIn1Row);

				tv.setText((i < 10 ? "0" : "") + i / 2 + ":00");
				gridHelper.addView(grid, tv, i / 2, 0);
			}
			gridHelper.addHorizontalSpacer(this, vi, grid, i, heightInPx, (i == hour * 2) ? 5 : 1);

		}

		// add events
		for (int i = 0; i < events.length; i++)
		{
			int eventRow = events[i];
			float eventDuration = durations[i];

			LinearLayout item = (LinearLayout) vi.inflate(R.layout.agenda_grid_item, null);
			item.setId(Integer.parseInt(10 + "" + i));
			item.setLayoutParams(new LinearLayout.LayoutParams((int) widthInPx, (int) (eventDuration * heightInPx)));
			item.setMinimumHeight((int) (eventDuration * heightInPx));
			item.setMinimumWidth((int) widthInPx);

			ImageButton button = (ImageButton) item.findViewById(R.id.icon);

			// if (i == 0)
			// {
			// button.setImageDrawable(getResources().getDrawable(R.drawable.ic_checked_white));
			// }
			//
			// if (i == 1)
			// {
			// button.setImageDrawable(getResources().getDrawable(R.drawable.ic_checked_white));
			// }

			if (i == 2)
			{
				button.setImageDrawable(getResources().getDrawable(R.drawable.ic_checked_white));
			}

			if (i == 3)
			{
				button.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_white));
			}

			button.setMinimumHeight((int) (eventDuration * innerHeightInPx));
			button.setMinimumWidth((int) widthInPx);
			button.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (pager.getDisplayedChild() != 2)
					{
						pager.setInAnimation(AnimationStore.getInstance().getComeIn());
						pager.setOutAnimation(AnimationStore.getInstance().getGoBack());
						pager.setDisplayedChild(2);

					}
				}
			});

			gridHelper.addViewToRow(grid, item, eventRow, (int) eventDuration);
			item.invalidate();

		}

		// add events
		// int i = 0;
		// for (MedicalActionExecution exec : executions)
		// {
		// int eventRow = exec.getStartDate().get(Calendar.HOUR_OF_DAY);
		// float eventDuration = Duration.standardSeconds((int) exec.getTimeWindow()).getStandardHours();
		//
		// LinearLayout item = (LinearLayout) vi.inflate(R.layout.agenda_grid_item, null);
		// item.setId(Integer.parseInt(10 + "" + (i++)));
		//
		// item.setLayoutParams(new LinearLayout.LayoutParams((int) widthInPx, (int) (eventDuration * heightInPx)));
		// item.setMinimumHeight((int) (eventDuration * heightInPx));
		// item.setMinimumWidth((int) widthInPx);
		// item.setTag(exec);
		//
		// ImageButton button = (ImageButton) item.findViewById(R.id.icon);
		//
		// if (i == 2)
		// {
		// button.setImageDrawable(getResources().getDrawable(R.drawable.ic_checked_white));
		// }
		//
		// if (i == 3)
		// {
		// button.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_white));
		// }
		//
		// button.setMinimumHeight((int) (eventDuration * innerHeightInPx));
		// button.setMinimumWidth((int) widthInPx);
		// button.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// if (pager.getDisplayedChild() != 2)
		// {
		// pager.setInAnimation(AnimationStore.getInstance().getComeIn());
		// pager.setOutAnimation(AnimationStore.getInstance().getGoBack());
		//
		// pager.setDisplayedChild(2);
		// }
		// }
		// });
		//
		// gridHelper.addViewToRow(grid, item, eventRow, (int) eventDuration);
		// item.invalidate();
		//
		// }

		grid.invalidate();

	}

	// private void initializeRelativeLayout()
	// {
	// //
	// // LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// //
	// // for (int i = 0; i < 24; i++) {
	// //
	// // View v = vi.inflate(R.layout.agenda_listday_item, null);
	// // v.setId(i);
	// //
	// // ((TextView) v.findViewById(R.id.day_indicator_dayname)).setText((i < 10 ? "0" : "") + i + ":00");
	// //
	// // RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
	// // RelativeLayout.LayoutParams.WRAP_CONTENT);
	// //
	// // if (i == 0) {
	// // p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	// // } else {
	// // p.addRule(RelativeLayout.BELOW, i - 1);
	// // }
	// // relative.addView(v, p);
	// // }
	// //
	// // int[] events = new int[] { 2, 2, 2, 3, 4, 5, 6, 6, 8, 9 };
	// // int[] durations = new int[] { 3, 2, 8, 1, 4, 2, 1, 2, 3, 5 };
	// //
	// // for (int i = 0; i < events.length; i++) {
	// //
	// // int h = events[i];
	// // int d = durations[i];
	// //
	// // int column = getFreeColumn(h, d);
	// //
	// // Log.d("COLUMN", "Column: " + column);
	// //
	// // ImageButton b = (ImageButton) vi.inflate(R.layout.dayelement, null);
	// // b.setId(Integer.parseInt(10 + "" + i));
	// //
	// // b.setPadding(10, 10, 10, (35 * d));
	// //
	// // RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
	// // RelativeLayout.LayoutParams.WRAP_CONTENT);
	// //
	// // p.addRule(RelativeLayout.BELOW, h - 1);
	// //
	// // if (i != 0 && h == events[i - 1]) {
	// // p.addRule(RelativeLayout.RIGHT_OF, Integer.parseInt(10 + "" + (i - 1)));
	// // } else {
	// // p.addRule(RelativeLayout.BELOW, h - 1);
	// // }
	// //
	// // p.setMargins(60 * column, 0, 0, 0);
	// //
	// // relative.addView(b, p);
	// // }
	// //
	// // for (boolean[] b : cells) {
	// // Log.d("CELLS", Arrays.toString(b));
	// // }
	// //
	// // relative.invalidate();
	//
	// }

	private void initializeAgendaList(Intent intent)
	{
		List<AgendaListElement> elements = new ArrayList<AgendaListActivity.AgendaListElement>();
		elements.add(createSeparator(new GregorianCalendar(2012, Calendar.SEPTEMBER, 24)));
		elements.add(createElement("ECG", "Nightly ECG monitoring", createCalendar(23, 0)));
		elements.add(createElement("Weight", "Daily weight measurement", createCalendar(8, 0)));
		elements.add(createElement("Blood Pressure", "Daily blood pressure measurement", createCalendar(8, 5)));
		elements.add(createSeparator(new GregorianCalendar(2012, Calendar.SEPTEMBER, 25)));
		elements.add(createElement("ECG", "Nightly ECG monitoring", createCalendar(23, 0)));
		elements.add(createElement("Weight", "Daily weight measurement", createCalendar(8, 0)));
		elements.add(createElement("Blood Pressure", "Daily blood pressure measurement", createCalendar(8, 5)));

		AgendaListElementAdapter adapter = new AgendaListElementAdapter(this, R.layout.agenda_list_item, elements);
		// actions = new String[] { "Action 1", "Action 2", "Action 3", "Action 4" };
		// agendaList.setAdapter(new ArrayAdapter<String>(this, R.layout.agenda_list_item, R.id.event_name, actions));
		agendaList.setAdapter(adapter);
		// agendaList.setOnItemClickListener(new AgendaItemClickListener());
	}

	private GregorianCalendar createCalendar(int hour, int minute)
	{
		return new GregorianCalendar(2012, Calendar.SEPTEMBER, 1, hour, minute);
	}

	private void initializeAgendaDayList(Intent intent)
	{
		List<AgendaListElement> elements = new ArrayList<AgendaListActivity.AgendaListElement>();
		for (int i = 0; i < 24; i++)
		{
			elements.add(new AgendaListElement());
		}

		AgendaListDayElementAdapter adapter = new AgendaListDayElementAdapter(this, R.layout.agenda_listday_item, elements);
		// actions = new String[] { "Action 1", "Action 2", "Action 3", "Action 4" };
		// agendaList.setAdapter(new ArrayAdapter<String>(this, R.layout.agenda_list_item, R.id.event_name, actions));
		dayList.setAdapter(adapter);
		// dayList.setOnItemClickListener(new AgendaItemClickListener());
	}

	private void initializeAgendaWeekList(Intent intent)
	{
		List<AgendaListElement> elements = new ArrayList<AgendaListActivity.AgendaListElement>();
		elements.add(createSeparator(new GregorianCalendar(2012, Calendar.SEPTEMBER, 24)));
		elements.add(createElement("ECG", "Nightly ECG monitoring", createCalendar(23, 0)));
		elements.add(createElement("Weight", "Daily weight measurement", createCalendar(8, 0)));
		elements.add(createElement("Blood Pressure", "Daily blood pressure measurement", createCalendar(8, 5)));
		elements.add(createSeparator(new GregorianCalendar(2012, Calendar.SEPTEMBER, 25)));
		elements.add(createElement("ECG", "Nightly ECG monitoring", createCalendar(23, 0)));
		elements.add(createElement("Weight", "Daily weight measurement", createCalendar(8, 0)));
		elements.add(createElement("Blood Pressure", "Daily blood pressure measurement", createCalendar(8, 5)));

		AgendaListElementAdapter adapter = new AgendaListElementAdapter(this, R.layout.agenda_list_item, elements);
		// actions = new String[] { "Action 1", "Action 2", "Action 3", "Action 4" };
		// agendaList.setAdapter(new ArrayAdapter<String>(this, R.layout.agenda_list_item, R.id.event_name, actions));
		agendaList.setAdapter(adapter);
		// agendaList.setOnItemClickListener(new AgendaItemClickListener());
	}

	private AgendaListElement createElement(String title, String desc, GregorianCalendar calendar)
	{
		AgendaListAction action = new AgendaListAction();
		action.title = title;
		action.description = desc;
		// action.date = new GregorianCalendar();
		action.date = calendar;
		return action;
	}

	private AgendaListElement createSeparator(GregorianCalendar cal)
	{
		AgendaListDayIndicator day = new AgendaListDayIndicator();
		day.date = cal;
		return day;
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		initializeAgendaList(intent);
	}

	// private class AgendaItemClickListener implements AdapterView.OnItemClickListener {
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	// UiUtils.showToast("Click at " + position, AgendaListActivity.this);
	// }
	// }

	public void onClickHome(View v)
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		final Intent intent = new Intent(this, homeActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}

	private class AgendaListElementAdapter extends ArrayAdapter<AgendaListElement> {

		private List<AgendaListElement> items;

		public AgendaListElementAdapter(Context context, int textViewResourceId, List<AgendaListElement> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			View v = convertView;
			AgendaListElement o = items.get(position);

			if (o != null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				if (o instanceof AgendaListDayIndicator)
				{
					v = vi.inflate(R.layout.agenda_list_dayindicator_item, null);
					TextView dtv = (TextView) v.findViewById(R.id.day_indicator_dayname);
					TextView dntv = (TextView) v.findViewById(R.id.day_indicator_daynum);

					dtv.setText(((AgendaListDayIndicator) o).getDay());
					dntv.setText(((AgendaListDayIndicator) o).getNum());

				} else if (o instanceof AgendaListAction)
				{
					v = vi.inflate(R.layout.agenda_list_item, null);

					TextView title = (TextView) v.findViewById(R.id.event_name);
					TextView desc = (TextView) v.findViewById(R.id.event_description);
					TextView date = (TextView) v.findViewById(R.id.evant_date);

					v.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{

							ViewSwitcher vs = ((ViewSwitcher) v);
							if (vs.getDisplayedChild() == 0)
							{

								ListView list = (ListView) v.getParent();
								for (int i = 0; i < list.getChildCount(); i++)
								{
									View current = list.getChildAt(i);
									if (current.findViewById(R.id.main_view) != null)
									{
										ViewSwitcher sw = (ViewSwitcher) current;
										if (sw.getDisplayedChild() == 1)
										{
											sw.setInAnimation(AnimationStore.getInstance().getSlideInFromLeft());
											sw.setOutAnimation(AnimationStore.getInstance().getSlideOutToRigth());
											sw.setDisplayedChild(0);
										}
									}
								}
								vs.setInAnimation(AnimationStore.getInstance().getSlideInFromRigth());
								vs.setOutAnimation(AnimationStore.getInstance().getSlideOutToLeft());
							} else
							{
								vs.setInAnimation(AnimationStore.getInstance().getSlideInFromLeft());
								vs.setOutAnimation(AnimationStore.getInstance().getSlideOutToRigth());
							}
							vs.showNext();
						}
					});

					title.setText(((AgendaListAction) o).title);
					desc.setText(((AgendaListAction) o).description);
					date.setText(((AgendaListAction) o).getTime());
				}

			}
			return v;
		}
	}

	private class AgendaListDayElementAdapter extends ArrayAdapter<AgendaListElement> {

		private List<AgendaListElement> items;

		public AgendaListDayElementAdapter(Context context, int textViewResourceId, List<AgendaListElement> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			View v = convertView;
			AgendaListElement o = items.get(position);

			if (o != null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.agenda_listday_item, null);
			}
			return v;
		}
	}

	/**
	 * 
	 * @author Ángel Piñeiro *
	 */
	public class AgendaListElement {

	}

	public class AgendaListAction extends AgendaListElement {

		// "yyyy-MM-dd HH:mm:ss.SSSZ",

		private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");

		public String title;
		public String description;
		public Calendar date;

		public String getTime()
		{
			return sdf.format(date.getTime()).toLowerCase();
		}
	}

	public class AgendaListDayIndicator extends AgendaListElement {

		private SimpleDateFormat sdfDay = new SimpleDateFormat("E");
		private SimpleDateFormat sdfNum = new SimpleDateFormat("dd MMM y");

		public Calendar date;

		public String getDay()
		{
			return capitalize(sdfDay.format(date.getTime()));
		}

		public String getNum()
		{
			return sdfNum.format(date.getTime());
		}

		private String capitalize(String s)
		{
			return s.substring(0, 1).toUpperCase().concat(s.substring(1));
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

}
