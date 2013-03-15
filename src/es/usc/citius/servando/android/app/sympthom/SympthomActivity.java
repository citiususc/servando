package es.usc.citius.servando.android.app.sympthom;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.alerts.AlertMsg;
import es.usc.citius.servando.android.alerts.AlertType;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.activities.ServandoActivity;
import es.usc.citius.servando.android.ui.animation.AnimationStore;

public class SympthomActivity extends ServandoActivity {

	Symptom symptom;
	View concreteSymptomView;
	FrameLayout container;
	Handler h;
	private Button sendButton;

	@Override
	protected void onBaseCreated(Intent intent)
	{

		container = (FrameLayout) findViewById(R.id.symptom_view_stub);

		String symptomId = intent.getStringExtra("symptom_id");

		if (symptomId != null)
		{
			symptom = SymptomStore.getInstance().getById(symptomId);
			setConcreteSymptomView(symptom);
			setActionBarTitle(symptom.getName());
		}

		// toast("Sympthom:" + symptomId + ", " + symptom.getName());

		sendButton = (Button) findViewById(R.id.symptom_send_button);

		sendButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickSymptomSend();
			}
		});

		h = new Handler();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sympthom, menu);
		return true;
	}

	@Override
	protected int getViewId()
	{
		return R.layout.activity_sympthom;
	}

	@Override
	protected String getActionBarTitle()
	{
		return getString(R.string.symptoms);
	}

	void setConcreteSymptomView(Symptom s)
	{
		Log.d("Symptom", "Setting concrete sympthom view");

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		concreteSymptomView = inflater.inflate(s.getViewMgr().getView(), null);
		container.removeAllViews();
		container.addView(concreteSymptomView);
		container.invalidate();
	}

	private void onClickSymptomSend()
	{
		symptom.getViewMgr().completeFromView(concreteSymptomView, symptom);
		Log.d("TAG", "Symptom desc: " + symptom.getDescription());
		new SendSymptomTask().execute(symptom);
	}

	private void startEnvelopeAnim()
	{
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View envelopeAnimView = inflater.inflate(R.layout.symptom_envelope_send, null);
		container.removeAllViews();
		envelopeAnimView.setVisibility(View.INVISIBLE);
		container.addView(envelopeAnimView);
		Animation a = AnimationStore.getInstance().getComeIn();
		a.setDuration(500);
		container.startAnimation(a);
		envelopeAnimView.setVisibility(View.VISIBLE);

		Animation envelopeAnim = getEnvelopeAnim();
		envelopeAnim.setDuration(1000);
		envelopeAnim.setStartOffset(200);
		envelopeAnim.setRepeatCount(2);
		envelopeAnim.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
				sendButton.setText(R.string.sending_symptom);
				sendButton.setEnabled(false);
				((TextView) findViewById(R.id.sending_sympton_message)).setText(R.string.sending_symptom);

			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				sendButton.setEnabled(true);
				sendButton.setText(R.string.accept);
				sendButton.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						finish();

					}
				});
				envelopeAnimView.findViewById(R.id.envelope_success).setVisibility(View.VISIBLE);
				envelopeAnimView.findViewById(R.id.envelope).setVisibility(View.INVISIBLE);
				((TextView) findViewById(R.id.sending_sympton_message)).setText(R.string.sending_symptom_success);
			}
		});

		findViewById(R.id.envelope).startAnimation(envelopeAnim);

	}

	private class SendSymptomTask extends AsyncTask<Symptom, Integer, Boolean> {

		@Override
		protected void onPreExecute()
		{
			startEnvelopeAnim();
		}

		@Override
		protected Boolean doInBackground(Symptom... params)
		{
			Symptom s = params[0];
			AlertMsg a = alertFromSymptom(s);
			ServandoPlatformFacade.getInstance().alert(a);
			return true;
		}

		private AlertMsg alertFromSymptom(Symptom s)
		{
			String comment = getResources().getString(R.string.alert_patient_comment);
			String alertDesc = s.getDescription() + (s.getPatientComment() != null ? (". " + comment + s.getDescription()) : "");
			return new AlertMsg.Builder().setType(AlertType.SYMPTOM).setDescription(alertDesc).setDisplayName(s.getName()).create();
		}

		@Override
		protected void onPostExecute(Boolean result)
		{

		}

	}

	Animation getEnvelopeAnim()
	{
		Animation outtoRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(600);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}

}
