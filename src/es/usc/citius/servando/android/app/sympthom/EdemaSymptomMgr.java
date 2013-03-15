package es.usc.citius.servando.android.app.sympthom;

import android.view.View;
import android.widget.Spinner;
import es.usc.citius.servando.android.app.R;

public class EdemaSymptomMgr implements SymptomViewMgr {

	@Override
	public int getView()
	{
		return R.layout.symptom_edema;
	}

	@Override
	public void completeFromView(View v, Symptom symptom)
	{

		String where = ((Spinner) v.findViewById(R.id.symptom_edema_location)).getSelectedItem().toString();
		String description = String.format(v.getResources().getString(R.string.symptom_edema_description), where);

		symptom.setDescription(description);
	}



}
