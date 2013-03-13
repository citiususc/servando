package es.usc.citius.servando.android.app.sympthom;

import android.view.View;
import es.usc.citius.servando.android.app.R;

public class PillowSymptomMgr implements SymptomViewMgr {

	@Override
	public int getView()
	{
		return R.layout.symptom_pillow;
	}

	@Override
	public void completeFromView(View v, Symptom symptom)
	{

	}



}
