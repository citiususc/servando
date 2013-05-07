package es.usc.citius.servando.android.app.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.agenda.ProtocolEngine;
import es.usc.citius.servando.android.agenda.ProtocolEngineListener;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.uiHelper.AgendaUIHelper;
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.models.protocol.MedicalActionExecution;
import es.usc.citius.servando.android.models.protocol.MedicalActionState;
import es.usc.citius.servando.android.ui.Iconnable;
import es.usc.citius.servando.android.ui.animation.AnimationStore;

public class AgendaActivity extends Activity implements ProtocolEngineListener {

	/**
	 * Servando paltform logger for this class
	 */
	private static final ILog log = ServandoLoggerFactory.getLogger(AgendaActivity.class);

	private final int VIEW_GENDA_CALENDAR_DAY = 0;
	private final int VIEW_AGENDA_LIST = 1;
	private final int VIEW_AGENDA_ACTION_DETAILS = 2;

	/**
	 * Displayed view, use {@link #setActiveView(int)} to change
	 */
	private int currentView = VIEW_GENDA_CALENDAR_DAY;

	// Component references
	private ImageButton listViewButton;
	private ImageButton dayViewButton;
	private ViewAnimator animator;
	private GridLayout grid;
	private ScrollView agendaScroll;

	private int screenOrientation;
	private AgendaUIHelper uiHelper;
	private ListView agendaList;
	private Button backButton;

	private TextView eventName;
	// private TextView eventDesc;
	private TextView eventTime;
	private TextView eventTimeWindow;

	private Button goButton;

	Handler h = new Handler();

	MedicalActionExecution current;

	List<MedicalActionExecution> list;

	// Action details component references
	// TODO

	private List<MedicalActionExecution> actions;

	private int lastView = -1;

	private float heightInPx;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agenda_dayview_layout);
		setupUI();
	}

	private void setupUI()
	{
		initializeComponents();
		initializeEvents();
		actions = loadDayActions();
		uiHelper = new AgendaUIHelper(actions, false);
		updateUiActions(actions);
		setActiveView(currentView);

		int actionId = getIntent().getIntExtra("action_id", -1);

		log.debug("OnNewIntent, actionId: " + actionId);

		if (actionId != -1)
		{
			MedicalActionExecution e = getMedicalAction(actionId);
			if (e != null)
			{
				onClickAction(e);
			} else
			{
				Toast.makeText(getApplicationContext(), "Action not found", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{

	}

	/**
	 * Initialize view components
	 */
	private void initializeComponents()
	{
		animator = (ViewAnimator) findViewById(R.id.pager);
		listViewButton = (ImageButton) findViewById(R.id.agenda_listview_button);
		dayViewButton = (ImageButton) findViewById(R.id.agenda_day_view_button);
		grid = (GridLayout) findViewById(R.id.grid);
		agendaList = (ListView) findViewById(R.id.agenda_list_view);
		backButton = (Button) findViewById(R.id.backButton);
		goButton = (Button) findViewById(R.id.goButton);
		agendaScroll = (ScrollView) findViewById(R.id.agenda_calendar_day_view);
		eventName = (TextView) findViewById(R.id.event_details_name);
		// eventDesc = (TextView) findViewById(R.id.event_details_description);
		eventTime = (TextView) findViewById(R.id.event_detail_time);
		eventTimeWindow = (TextView) findViewById(R.id.evant_detail_timewindow);

		screenOrientation = getResources().getConfiguration().orientation;

		agendaList.setEmptyView(findViewById(R.id.empty));
	}

	/**
	 * Initialize view event listeners and actions
	 */
	private void initializeEvents()
	{
		listViewButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setActiveView(VIEW_AGENDA_LIST);
			}
		});
		dayViewButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setActiveView(VIEW_GENDA_CALENDAR_DAY);
			}
		});
		backButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				setActiveView(lastView != -1 ? lastView : VIEW_GENDA_CALENDAR_DAY);
			}
		});
	}

	private MedicalActionExecution getMedicalAction(int id)
	{
		for (MedicalActionExecution e : actions)
		{
			log.debug("Action: " + e.getUniqueId());
			if (e.getUniqueId() == id)
			{
				return e;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param v the view to set
	 */
	private void setActiveView(int v)
	{
		if (v != currentView)
		{

			if (currentView == VIEW_AGENDA_ACTION_DETAILS)
			{
				animator.setInAnimation(null);// AnimationStore.getInstance().getComeIn());
				animator.setOutAnimation(null);// AnimationStore.getInstance().getGoBack());
			} else
			{
				boolean moveToRight = v > currentView;

				if (moveToRight && v != VIEW_AGENDA_ACTION_DETAILS)
				{
					animator.setInAnimation(AnimationStore.getInstance().getSlideInFromRigth());
					animator.setOutAnimation(AnimationStore.getInstance().getSlideOutToLeft());
				} else if (v != VIEW_AGENDA_ACTION_DETAILS)
				{
					animator.setInAnimation(AnimationStore.getInstance().getSlideInFromLeft());
					animator.setOutAnimation(AnimationStore.getInstance().getSlideOutToRigth());
				} else
				{
					animator.setInAnimation(null);// AnimationStore.getInstance().getComeIn());
					animator.setOutAnimation(null);// AnimationStore.getInstance().getGoBack());
				}
			}

			animator.setDisplayedChild(v);
			lastView = currentView;
			currentView = v;
		}
	}

	@Override
	protected void onResume()
	{
		ProtocolEngine.getInstance().addProtocolListener(this);
		updateList();
		scrollToNow();
		super.onResume();
	}

	private void updateUiActions(List<MedicalActionExecution> actions)
	{
		float[][] actionLayoutInfo = uiHelper.getActionLayoutInfo();

		// Gat a layout inflater
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Current hour of day
		int hour = DateTime.now().getHourOfDay();
		// Screen width in pixels
		int widthPixels = getResources().getDisplayMetrics().widthPixels;
		// Screen height in pixels
		heightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.day_view_height),
				getResources().getDisplayMetrics());

		float innerHeightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.day_view_inner_height),
				getResources().getDisplayMetrics());

		float widthInPx = widthPixels * 0.78f / (uiHelper.getColumns() - 1);

		float widthIn1Row = widthPixels * 0.18f;

		grid.setRowCount(uiHelper.getRows());
		grid.setColumnCount(uiHelper.getColumns());

		View now = null;
		// add hour labels to first column
		for (int i = 0; i < uiHelper.getRows(); i++)
		{
			if (i % 2 == 0)
			{
				Button tv = (Button) li.inflate(R.layout.agenda_grid_time_labe_item, null);
				tv.setMinimumHeight((int) heightInPx);
				tv.setMinimumWidth((int) widthIn1Row);
				tv.setWidth((int) widthIn1Row);

				if (i / 2 == hour)
				{
					now = tv;
				}

				tv.setText((i < 20 ? "0" : "") + i / 2 + ":00");
				uiHelper.addView(grid, tv, i / 2, 0);
			}
			uiHelper.addHorizontalSpacer(this, li, grid, i, heightInPx);

		}

		// add events
		for (int i = 0; i < actionLayoutInfo[0].length; i++)
		{
			MedicalActionExecution action = actions.get(i);

			int START_MINS = action.getStartDate().get(Calendar.MINUTE);
			int END_MINS = new DateTime(action.getStartDate()).plusSeconds((int) action.getTimeWindow()).getMinuteOfHour();

			// int marginTop = (int) ((float) START_MINS / 60 * heightInPx);
			// int marginBottom = (int) ((float) (60 - END_MINS) / 60 * heightInPx);

			float eventRow = actionLayoutInfo[0][i];
			float eventDuration = actionLayoutInfo[1][i];

			LinearLayout item = (LinearLayout) li.inflate(R.layout.agenda_grid_item, null);

			item.setId(Integer.parseInt(10 + "" + i));
			item.setLayoutParams(new LinearLayout.LayoutParams((int) widthInPx, (int) (eventDuration * heightInPx)));
			// item.setMinimumHeight((int) (eventDuration * heightInPx) + marginBottom + marginTop);
			item.setMinimumHeight((int) (eventDuration * heightInPx));
			item.setMinimumWidth((int) widthInPx);
			item.setTag(actions.get(i));

			// TextView textview = (TextView) item.findViewById(R.id.title);
			ImageButton button = (ImageButton) item.findViewById(R.id.icon);
			button.setTag(action);
			//
			// Ellipsize action text
//			String text = action.getTitle().length() <= 10 ? action.getTitle() : action.getTitle().substring(0, 10) + "...";

			// textview.setText("");
			button.setImageDrawable(getResources().getDrawable(action.getIcon()));

			button.setMinimumHeight((int) (eventDuration * innerHeightInPx));
			button.setMinimumWidth((int) widthInPx);

			button.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onClickAction((MedicalActionExecution) v.getTag());
				}
			});

			log.debug("StartMin: " + START_MINS + ", endMin:" + END_MINS);

			if (END_MINS > 5 && END_MINS < 55)
			{
				// eventDuration++;
			}

			uiHelper.addViewToRow(grid, item, (int) eventRow, (int) eventDuration);

			item.invalidate();

		}
		grid.invalidate();

		final View v = now;
		((TextView) v).setTextColor(getResources().getColor(R.color.servando_blue));
		((TextView) v).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		grid.postDelayed(new Runnable()
		{

			@Override
			public void run()
			{
				Animation anim = AnimationStore.getInstance().getComeIn();
				anim.setFillAfter(true);
				v.startAnimation(anim);

			}

		}, 200);

	}

	void scrollToNow()
	{
		if (currentView == VIEW_GENDA_CALENDAR_DAY)
		{
			final int px = (int) (DateTime.now().getHourOfDay() * heightInPx);
			agendaScroll.post(new Runnable()
			{
				@Override
				public void run()
				{
					agendaScroll.scrollBy(0, px);
				}
			});

		}
	}

	private void onClickAction(MedicalActionExecution target)
	{
		// TODO Uncomment to show action details
		updateActionDetailsView(target);
		setActiveView(VIEW_AGENDA_ACTION_DETAILS);
	}

	private void updateActionDetailsView(final MedicalActionExecution target)
	{
		current = target;

		DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");

		log.debug("Targe: " + target.toString());

		eventName.setText(target.getAction().getDisplayName());

		eventName.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				Toast.makeText(getApplicationContext(), "Status: " + target.getState(), Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		((TextView) findViewById(R.id.finish_at)).setText(target.getState() == MedicalActionState.NotStarted ? getString(R.string.action_start_at)
				: getString(R.string.action_finish_at));

		if (target.getState() == MedicalActionState.NotStarted)
		{
			Duration timeToStart = new Duration(DateTime.now(), new DateTime(target.getStartDate()).plusMinutes(1));
			eventTime.setText(new DateTime(target.getStartDate()).toString(fmt) + " h");
			eventTimeWindow.setText(formatDuration(timeToStart));
		} else
		{
			Duration timeToFinish = new Duration(DateTime.now(), new DateTime(target.getStartDate()).plusSeconds((int) target.getTimeWindow() + 60));
			eventTime.setText(new DateTime(target.getStartDate()).plusSeconds((int) target.getTimeWindow()).toString(fmt) + " h");
			eventTimeWindow.setText(formatDuration(timeToFinish));
		}

		if (target.getAction().getProvider() instanceof Iconnable)
		{

			((ImageView) findViewById(R.id.event_icon)).setImageDrawable(getResources().getDrawable(target.getIcon()));
		}

		if (!(target.getState() == MedicalActionState.Uncompleted))
		{
			goButton.setVisibility(View.INVISIBLE);
		} else
		{
			goButton.setVisibility(View.VISIBLE);
			goButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					showMedicalActionActivity(target.getUniqueId());
					finish();
				}
			});
		}

	}

	private String formatDuration(Duration d)
	{

		String duration = "";
		long hours = d.getStandardHours();
		long minutes = d.getStandardMinutes();
		if (hours > 0)
		{
			duration += hours + "h ";
			minutes -= hours * 60;
		}
		duration += minutes + "min ";
		return duration;
	}

	private void showMedicalActionActivity(int actionId)
	{
		Intent intent = new Intent(getApplicationContext(), SwitcherActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("action_id", actionId);
		startActivity(intent);
	}

	private List<MedicalActionExecution> loadDayActions()
	{
		List<MedicalActionExecution> loaded = ProtocolEngine.getInstance().getFilteredDayActions(new GregorianCalendar());
		log.debug("Agenda:Loaded actions: " + loaded.size());
		return loaded;

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	public void onClickHome(View v)
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		final Intent intent = new Intent(this, homeActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}

	/*** AGENDA LIST MODE ***/

	private void updateList()
	{
		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				new AsyncTask<String, Integer, String>()
				{

					@Override
					protected String doInBackground(String... params)
					{
						list = ServandoPlatformFacade.getInstance().getProtocolEngine().getFilteredDayActions(new GregorianCalendar());
						return null;
					}

					@Override
					protected void onPostExecute(String result)
					{
						setupAgendaList(list);
					}
				}.execute();
			}
		});

	}

	private void setupAgendaList(List<MedicalActionExecution> list)
	{

		List<AgendaListElement> elements = new ArrayList<AgendaListElement>();

		for (MedicalActionExecution a : list)
		{
			elements.add(createElement(a.getAction().getProvider().getId(), a.getAction().getDisplayName(), a.getStartDate()));
		}

		AgendaListElementAdapter adapter = new AgendaListElementAdapter(this, R.layout.agenda_list_item, elements);

		agendaList.setAdapter(adapter);
	}

	private AgendaListElement createSeparator(GregorianCalendar cal)
	{
		AgendaListDayIndicator day = new AgendaListDayIndicator();
		day.date = cal;
		return day;
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

	private class AgendaListElement {
		public MedicalActionExecution execution;
	}

	private class AgendaListElementAdapter extends ArrayAdapter<AgendaListElement> {

		private List<AgendaListElement> items;

		public AgendaListElementAdapter(Context context, int textViewResourceId, List<AgendaListElement> items)
		{
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
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

							if (position >= 0 && position < list.size())
							{
								onClickAction(list.get(position));
							}

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
	public void onExecutionStart(MedicalActionExecution target)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onExecutionAbort(MedicalActionExecution target)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onExecutionFinish(MedicalActionExecution target)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadDayActions()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onProtocolChanged()
	{
		h.post(new Runnable()
		{
			@Override
			public void run()
			{
				((ViewGroup) findViewById(R.id.relativeLayoutAgenda)).removeAllViewsInLayout();
				setContentView(R.layout.agenda_dayview_layout);
				setupUI();
			}
		});

	}

	@Override
	public void onProtocolEngineStart()
	{

	}

	@Override
	public void onReminder(long minutes)
	{
		if (currentView == VIEW_AGENDA_ACTION_DETAILS && current != null)
		{
			h.post(new Runnable()
			{
				@Override
				public void run()
				{
					updateActionDetailsView(current);
				}
			});
		}

	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		ProtocolEngine.getInstance().removeProtocolListener(this);
	}

}
