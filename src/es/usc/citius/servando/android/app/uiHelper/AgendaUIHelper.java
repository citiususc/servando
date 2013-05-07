package es.usc.citius.servando.android.app.uiHelper;

import java.util.Calendar;
import java.util.List;

import org.joda.time.Duration;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.models.protocol.MedicalActionExecution;

public class AgendaUIHelper {

	private static final String TAG = AgendaUIHelper.class.getSimpleName();

	public static int GRID_ROWS = 24 * 2;
	public static int GRID_COLS = 6;
	public static int GRID_COLS_LANDSCAPE = 10;

	private int rows;
	private int columns;
	private boolean[][] cells;
	private boolean distribute;
	private int startCoulumn = 0;
	private float[][] actionLayoutInfo;

	public AgendaUIHelper(List<MedicalActionExecution> actions, boolean distribute)
	{

		actionLayoutInfo = getActionLayoutInfo(actions);
		rows = GRID_ROWS;
		columns = getNeedColumns(actionLayoutInfo);
		cells = new boolean[rows][];
		this.distribute = distribute;

		// initialize row cells to false
		for (int i = 0; i < rows; i++)
		{
			cells[i] = new boolean[columns];
		}
	}

	/**
	 * 
	 * @param row
	 * @param height
	 * @return
	 */
	private int getFirstFreeColumnInRow(int row, int height)
	{

		boolean[] hourRow = cells[row];

		int freeColumn = -1;

		for (int rcolumn = startCoulumn; rcolumn < startCoulumn + columns; rcolumn++)
		{

			int column = rcolumn % columns;

			if (!hourRow[column])
			{
				freeColumn = column;
				int size = Math.min(row + height, rows);
				for (int currentRow = row; currentRow < size; currentRow++)
				{
					setCelloccupied(currentRow, freeColumn);
				}
				return freeColumn;
			}
		}
		return freeColumn;
	}

	/**
	 * 
	 * @param row
	 * @param height
	 * @return
	 */
	private int getRandomFreeColumnInRow(int row, int height)
	{

		boolean[] hourRow = cells[row];

		int freeColumn = -1;

		int start = (int) (Math.random() * 100 * columns) / 100;

		for (int rcolumn = start; rcolumn < start + columns; rcolumn++)
		{

			int column = rcolumn % columns;

			if (!hourRow[column])
			{
				freeColumn = column;
				int size = Math.min(row + height, rows);
				for (int currentRow = row; currentRow < size; currentRow++)
				{
					setCelloccupied(currentRow, freeColumn);
				}
				return freeColumn;
			}
		}
		return freeColumn;
	}

	public int getRow(int row, int height)
	{

		startCoulumn = (startCoulumn + 1) % columns;

		return distribute ? getRandomFreeColumnInRow(row, height) : getFirstFreeColumnInRow(row, height);
	}

	public void addView(GridLayout grid, View v, int row, int col)
	{
		setCelloccupied(row, col);
		GridLayout.Spec rowSpec = GridLayout.spec(row, 1);
		GridLayout.Spec colSpec = GridLayout.spec(col, 1);
		LayoutParams params = new LayoutParams(rowSpec, colSpec);
		params.setMargins(1, 1, 1, 0);
		grid.addView(v, params);
	}

	public void addHorizontalSpacer(Context ctx, LayoutInflater vi, GridLayout grid, int at, float colWidth)
	{
		Button b = (Button) vi.inflate(R.layout.agenda_grid_row_spacer, null);
		b.setWidth((grid.getColumnCount() * 1000));
		b.setHeight(1);

		GridLayout.Spec rowSpec = GridLayout.spec(at, 0);
		GridLayout.Spec colSpec = GridLayout.spec(0, grid.getColumnCount());

		LayoutParams params = new LayoutParams(rowSpec, colSpec);
		params.setMargins(0, 0, 0, 0);
		grid.addView(b, params);
	}

	public void addViewToRow(GridLayout grid, View v, int row, int duration)
	{
		System.out.println("Add view to row: " + row + ", " + duration);
		int col = getRow(row, duration);

		if (col != -1)
		{
			GridLayout.Spec rowSpec = GridLayout.spec(row, duration);
			GridLayout.Spec colSpec = GridLayout.spec(col, 1);
			LayoutParams params = new LayoutParams(rowSpec, colSpec);
			params.setMargins(1, 0, 1, 0);
			grid.addView(v, params);
		} else
		{
			Log.d(TAG, "Cannot add view to grid. No available free columns");
		}
	}

	private void addViewToRow(GridLayout grid, View v, int row, int duration, int topMargin, int bottomMargin)
	{
		// System.out.println("Add view to row: " + row + ", duration: " + duration + ", top: " + topMargin +
		// ", bottom: " + bottomMargin);
		// int col = getRow(row, duration);
		//
		// if (col != -1)
		// {
		// GridLayout.Spec rowSpec = GridLayout.spec(row, duration);
		// GridLayout.Spec colSpec = GridLayout.spec(col, 1);
		// LayoutParams params = new LayoutParams(rowSpec, colSpec);
		// params.setMargins(1, 0, 1, 0);
		//
		// if (v.findViewById(R.id.topMargin) != null)
		// {
		// View marginTop = v.findViewById(R.id.topMargin);
		// marginTop.setMinimumHeight(topMargin);
		//
		// View marginBottom = v.findViewById(R.id.bottomMargin);
		// marginBottom.setMinimumHeight(bottomMargin);
		// }
		//
		// grid.addView(v, params);
		// } else
		// {
		// Log.d(TAG, "Cannot add view to grid. No available free columns");
		// }
	}

	private void setCelloccupied(int row, int col)
	{
		cells[row][col] = true;
	}

	private float[][] getActionLayoutInfo(List<MedicalActionExecution> actions)
	{

		float[][] actionLayoutInfo = new float[2][actions.size()];

		for (int i = 0; i < actions.size(); i++)
		{
			MedicalActionExecution e = actions.get(i);
			int eventRow = e.getStartDate().get(Calendar.HOUR_OF_DAY);
			long eventDuration = Duration.standardSeconds((int) e.getTimeWindow()).getStandardHours();
			eventDuration = eventDuration > 0 ? eventDuration : 1;
			actionLayoutInfo[0][i] = eventRow;
			actionLayoutInfo[1][i] = (int) eventDuration;
		}

		return actionLayoutInfo;
	}

	private int getNeedColumns(float[][] actionLayoutInfo)
	{

		int max = 0;
		int hourEvents = 0;

		// para cada hora
		for (int hour = 0; hour < 24; hour++)
		{
			// recorro os eventos e miro se coinciden
			for (int evt = 0; evt < actionLayoutInfo[0].length; evt++)
			{
				float evtStart = actionLayoutInfo[0][evt]; // 2
				float evtEnd = evtStart + actionLayoutInfo[1][evt]; // 5

				if (hour >= evtStart && hour < evtEnd)
				{
					hourEvents++;
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

	public int getRows()
	{
		return rows;
	}

	public int getColumns()
	{
		return columns;
	}

	public float[][] getActionLayoutInfo()
	{
		return actionLayoutInfo;
	}

}
