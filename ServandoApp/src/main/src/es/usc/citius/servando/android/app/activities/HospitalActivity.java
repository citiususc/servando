package es.usc.citius.servando.android.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.sympthom.SymptomListActivity;

public class HospitalActivity extends Activity implements OnClickListener {

	ImageButton advicesButton = null;
	ImageButton sympthomsButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hospital_activity);

		advicesButton = (ImageButton) findViewById(R.id.hospital_activity_advices);
		sympthomsButton = (ImageButton) findViewById(R.id.hospital_activity_symptoms);
		advicesButton.setOnClickListener(this);
		sympthomsButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		Intent intent = null;
		switch (v.getId()) {

		case R.id.hospital_activity_advices:
			intent = new Intent(getApplicationContext(), HospitalAdvicesActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.hospital_activity_symptoms:
			intent = new Intent(getApplicationContext(), SymptomListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		default:
			break;
		}

	}

	public void onClickHome(View v)
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		final Intent intent = new Intent(this, homeActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
	}
}
