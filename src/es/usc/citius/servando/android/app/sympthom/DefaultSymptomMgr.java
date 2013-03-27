package es.usc.citius.servando.android.app.sympthom;

import android.view.View;
import android.widget.TextView;
import es.usc.citius.servando.android.app.R;

public class DefaultSymptomMgr implements SymptomViewMgr {

	private static DefaultSymptomMgr instance = new DefaultSymptomMgr();

	private DefaultSymptomMgr()
	{
	}

	public static DefaultSymptomMgr getInstance()
	{
		return instance;
	}

	@Override
	public int getView()
	{
		return R.layout.symptom_default;
	}

	@Override
	public void completeFromView(View v, Symptom symptom)
	{
		String comment = ((TextView) v.findViewById(R.id.symptom_comment)).getText().toString();
		symptom.setPatientComment(comment);
	}

	@Override
	public void onViewCreated(View v)
	{
		// TODO Auto-generated method stub

	}

}
