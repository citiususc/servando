package es.usc.citius.servando.android.app.sympthom;

import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;

public class EmergencySymptom implements SymptomViewMgr {

	@Override
	public int getView()
	{
		return R.layout.symptom_emergency;
	}

	@Override
	public void onViewCreated(View v)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void completeFromView(View v, Symptom symptom)
	{
		String comment = ((EditText) v.findViewById(R.id.symptom_comment)).getText().toString();

		symptom.setPatientComment(comment);
		symptom.setDescription(v.getResources().getString(R.string.symptom_emergency_description));

	}

	@Override
	public String getButtonText()
	{
		// TODO Auto-generated method stub
		Resources resources = ServandoPlatformFacade.getInstance().getResources();
		if (resources != null)
		{
			String text = resources.getString(R.string.symptom_emergency_button_text);
			if (text != null && text.length() > 0)
			{
				return text;
			}
		}
		return null;

	}

}
