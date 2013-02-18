package es.usc.citius.servando.android.app.activities;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;
import es.usc.citius.servando.android.agenda.ProtocolEngineServiceBinder;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.uiHelper.AgendaUIHelper;
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.models.protocol.MedicalActionExecution;
import es.usc.citius.servando.android.ui.animation.AnimationStore;

public class AgendaActivity extends Activity {

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
	private Button listViewButton;
	private Button dayViewButton;
	private ViewAnimator animator;
	private GridLayout grid;

	private int screenOrientation;
	private AgendaUIHelper uiHelper;

	// Action details component references
	// TODO

	private List<MedicalActionExecution> actions;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agenda_dayview_layout);
		initializeComponents();
		initializeEvents();
		actions = loadDayActions();
		uiHelper = new AgendaUIHelper(actions, false);
		updateUiActions(actions);
		setActiveView(currentView);
	}

	/**
	 * Initialize view components
	 */
	private void initializeComponents()
	{
		animator = (ViewAnimator) findViewById(R.id.pager);
		listViewButton = (Button) findViewById(R.id.agenda_listview_button);
		dayViewButton = (Button) findViewById(R.id.agenda_day_view_button);
		grid = (GridLayout) findViewById(R.id.grid);
		screenOrientation = getResources().getConfiguration().orientation;
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
	}

	/**
	 * 
	 * @param v the view to set
	 */
	private void setActiveView(int v)
	{
		if (v != currentView)
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
				animator.setInAnimation(AnimationStore.getInstance().getComeIn());
				animator.setOutAnimation(AnimationStore.getInstance().getGoBack());
			}

			animator.setDisplayedChild(v);

			currentView = v;
		}
	}

	private void updateUiActions(List<MedicalActionExecution> actions)
	{
		int[][] actionLayoutInfo = uiHelper.getActionLayoutInfo();

		System.out.println(Arrays.toString(actionLayoutInfo[0]));
		System.out.println(Arrays.toString(actionLayoutInfo[1]));
		// Gat a layout inflater
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Current hour of day
		int hour = GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY);
		// Screen width in pixels
		int widthPixels = getResources().getDisplayMetrics().widthPixels;
		// Screen height in pixels
		float heightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.day_view_height),
				getResources().getDisplayMetrics());

		float innerHeightInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.day_view_inner_height),
				getResources().getDisplayMetrics());

		float widthInPx = widthPixels * 0.78f / (uiHelper.getColumns() - 1);

		float widthIn1Row = widthPixels * 0.18f;

		grid.setRowCount(uiHelper.getRows());
		grid.setColumnCount(uiHelper.getColumns());

		// add hour labels to first column
		for (int i = 0; i < uiHelper.getRows(); i++)
		{
			if (i % 2 == 0)
			{
				Button tv = (Button) li.inflate(R.layout.agenda_grid_time_labe_item, null);
				tv.setMinimumHeight((int) heightInPx);
				tv.setMinimumWidth((int) widthIn1Row);
				tv.setWidth((int) widthIn1Row);

				tv.setText((i < 10 ? "0" : "") + i / 2 + ":00");
				uiHelper.addView(grid, tv, i / 2, 0);
			}
			uiHelper.addHorizontalSpacer(this, li, grid, i, heightInPx);

		}

		// add events
		for (int i = 0; i < actionLayoutInfo[0].length; i++)
		{
			int eventRow = actionLayoutInfo[0][i];
			int eventDuration = actionLayoutInfo[1][i];

			LinearLayout item = (LinearLayout) li.inflate(R.layout.agenda_grid_item, null);
			item.setId(Integer.parseInt(10 + "" + i));
			item.setLayoutParams(new LinearLayout.LayoutParams((int) widthInPx, (int) (eventDuration * heightInPx)));
			item.setMinimumHeight((int) (eventDuration * heightInPx));
			item.setMinimumWidth((int) widthInPx);
			item.setTag(actions.get(i));

			TextView textview = (TextView) item.findViewById(R.id.title);
			ImageButton button = (ImageButton) item.findViewById(R.id.icon);

			// Ellipsize action text
			String text = actions.get(i).getTitle().length() <= 10 ? actions.get(i).getTitle() : actions.get(i).getTitle().substring(0, 10) + "...";

			textview.setText(text);
			button.setImageDrawable(getResources().getDrawable(actions.get(i).getIcon()));

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

			uiHelper.addViewToRow(grid, item, eventRow, eventDuration);
			item.invalidate();

		}
		grid.invalidate();

	}

	private void onClickAction(MedicalActionExecution target)
	{
		updateActionDetailsView(target);
		setActiveView(VIEW_AGENDA_ACTION_DETAILS);
	}

	private void updateActionDetailsView(MedicalActionExecution target)
	{

	}

	private List<MedicalActionExecution> loadDayActions()
	{
		List<MedicalActionExecution> loaded = ProtocolEngineServiceBinder.getInstance().getProtocolEngine().getDayActions(new GregorianCalendar());
		log.debug("Agenda:Loaded actions: " + loaded.size());
		return loaded;

	}

}
