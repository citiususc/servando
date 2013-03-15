package es.usc.citius.servando.android.app.sympthom;

import android.view.View;
import android.widget.EditText;
import es.usc.citius.servando.android.app.R;

public class DizzinessSymptomMgr implements SymptomViewMgr {

	@Override
	public int getView()
	{
		return R.layout.symptom_dizziness;
	}

	@Override
	public void completeFromView(View v, Symptom symptom)
	{
		String comment = ((EditText) v.findViewById(R.id.symptom_comment)).getText().toString();

		symptom.setPatientComment(comment);
		symptom.setDescription(v.getResources().getString(R.string.symptom_dizziness_description));
	}



}
