package es.usc.citius.servando.android.app.sympthom;

import android.view.View;
import android.widget.EditText;
import es.usc.citius.servando.android.app.R;

public class PillowSymptomMgr implements SymptomViewMgr {

	@Override
	public int getView()
	{
		return R.layout.symptom_pillow;
	}

	@Override
	public void onViewCreated(View v)
	{

	}

	@Override
	public void completeFromView(View v, Symptom symptom)
	{
		String pillowNum = ((EditText) v.findViewById(R.id.pillowEditText)).getText().toString();
		// String comment = ((EditText) v.findViewById(R.id.symptom_comment)).getText().toString();

		pillowNum = pillowNum != null ? pillowNum : "0";
		String description = String.format(v.getResources().getString(R.string.symptom_pillow_description), pillowNum);
		symptom.setDescription(description);
	}

	@Override
	public String getButtonText()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
