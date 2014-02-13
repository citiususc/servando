package es.usc.citius.servando.android.app.sympthom;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;

public class EdemaSymptomMgr implements SymptomViewMgr {

	private static List<String> selected = new ArrayList<String>();

	@Override
	public int getView()
	{
		return R.layout.symptom_edema;
	}

	@Override
	public void onViewCreated(final View v)
	{

		final View view = v;
		Button b = (Button) v.findViewById(R.id.symptom_edema_location_multichoice);
		selected = new ArrayList<String>();
		if (selected.size() > 0)
		{
			b.setText(getSymptonLocation());
		}
		if (b != null)
		{
			b.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AlertDialog.Builder d = new AlertDialog.Builder(v.getContext());
					final String[] elements = ServandoPlatformFacade.getInstance()
																	.getResources()
																	.getStringArray(R.array.symptom_edema_location);
					boolean marked[] = new boolean[elements.length];
					for (int i = 0; i < elements.length; i++)
					{
						if (selected.contains(elements[i].trim()))
						{
							marked[i] = true;
						}
					}
					d.setMultiChoiceItems(elements, marked, new DialogInterface.OnMultiChoiceClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked)
						{
							String item = elements[which];
							if (isChecked)
							{
								if (!selected.contains(item))
								{
									selected.add(item);
								}
							} else
							{
								if (selected.contains(item))
								{
									selected.remove(item);
								}
							}
							String text = "";
							if (selected.size() > 0)
							{
								text = getSymptonLocation();
							} else
							{
								text = ServandoPlatformFacade.getInstance().getResources().getString(R.string.select_button);
							}
							Button button = (Button) view.findViewById(R.id.symptom_edema_location_multichoice);
							button.setText(text);

						}
					});
					String textButton = ServandoPlatformFacade.getInstance().getResources().getString(R.string.done_button);
					d.setPositiveButton(textButton, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{

						}
					});
					AlertDialog a = d.create();
					a.show();
				}
			});
		}
	}

	private String getSymptonLocation()
	{
		String location = "";
		if (selected != null && selected.size() > 0)
		{
			String nexo = ServandoPlatformFacade.getInstance().getResources().getString(R.string.and);

			if (selected.size() == 1)
			{
				location = selected.get(0);
			} else if (selected.size() == 2)
			{
				location = selected.get(0) + " " + nexo + " " + selected.get(1);
			} else
			{
				for (int i = 0; i < selected.size() - 2; i++)
				{
					location = location + selected.get(i) + ", ";
				}
				location = location + selected.get(selected.size() - 2) + " " + nexo + " " + selected.get(selected.size() - 1);
			}
		}
		return location;
	}

	@Override
	public void completeFromView(View v, Symptom symptom)
	{

		String where = getSymptonLocation();
		if (where.length() == 0)
		{
			where = ServandoPlatformFacade.getInstance().getResources().getString(R.string.unspecified);
		}
		String description = String.format(v.getResources().getString(R.string.symptom_edema_description), where);
		symptom.setDescription(description);
	}

	@Override
	public String getButtonText()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
