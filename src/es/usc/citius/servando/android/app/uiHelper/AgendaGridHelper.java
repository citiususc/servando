package es.usc.citius.servando.android.app.uiHelper;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.GridLayout.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import es.usc.citius.servando.android.app.R;

public class AgendaGridHelper {

	private static final String TAG = AgendaGridHelper.class.getSimpleName();

	private int rows;
	private int columns;
	private boolean[][] cells;
	private boolean distribute;
	private int startCoulumn = 0;

	public AgendaGridHelper(int rows, int columns, boolean distribute)
	{
		this.rows = rows;
		this.columns = columns;
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

	//
	// public void addView(GridLayout grid, View v, int row, int col, int width, int height) {
	//
	//
	//
	// GridLayout.Spec rowSpec = GridLayout.spec(row, width);
	// GridLayout.Spec colSpec = GridLayout.spec(col, height);
	// LayoutParams params = new LayoutParams(rowSpec, colSpec);
	// grid.addView(v, params);
	// }

	public void addView(GridLayout grid, View v, int row, int col)
	{
		setCelloccupied(row, col);
		GridLayout.Spec rowSpec = GridLayout.spec(row, 1);
		GridLayout.Spec colSpec = GridLayout.spec(col, 1);
		LayoutParams params = new LayoutParams(rowSpec, colSpec);
		params.setMargins(1, 1, 1, 0);
		grid.addView(v, params);
	}

	public void addHorizontalSpacer(Context ctx, LayoutInflater vi, GridLayout grid, int at, float colWidth, int height)
	{
		Button b = (Button) vi.inflate(height == 1 ? R.layout.agenda_grid_row_spacer : R.layout.agenda_grid_row_spacer_big, null);
		b.setWidth((grid.getColumnCount() * 1000));
		b.setHeight(height);

		GridLayout.Spec rowSpec = GridLayout.spec(at, 0);
		GridLayout.Spec colSpec = GridLayout.spec(0, grid.getColumnCount());

		LayoutParams params = new LayoutParams(rowSpec, colSpec);
		params.setMargins(0, 0, 0, 0);
		grid.addView(b, params);
	}

	public void addViewToRow(GridLayout grid, View v, int row, int duration)
	{

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

	private void setCelloccupied(int row, int col)
	{
		cells[row][col] = true;
	}

}
