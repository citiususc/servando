package es.usc.citius.servando.android.app.activities;

import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.alerts.AlertMsg;
import es.usc.citius.servando.android.alerts.AlertType;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.ui.animation.AnimationStore;

public class HospitalAdvicesActivity extends Activity implements OnClickListener {

	Button sendButton = null;
	EditText comment = null;
	FrameLayout container;
	Spinner spinner = null;
	DatePicker datePicker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hospital_advices_activity);
		sendButton = (Button) findViewById(R.id.hospital_advices_send_button);
		sendButton.setOnClickListener(this);
		comment = (EditText) findViewById(R.id.hospital_advice_comment);
		container = (FrameLayout) findViewById(R.id.advices_view_stub);
		spinner = (Spinner) findViewById(R.id.spinner1);
		datePicker = (DatePicker) findViewById(R.id.datePicker1);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		return true;
	}

	@Override
	public void onClick(View v)
	{
		Log.d("TESTING", "On click....");
		switch (v.getId()) {
		case R.id.hospital_advices_send_button:
			if (sendButton != null && comment != null)
			{
				if (isValidReason())
				{
					AlertMsg alert = getAlertFromView();
					new SendSymptomTask().execute(alert);
				} else
				{
					String text = getResources().getString(R.string.hospital_advices_should_select_reason_msg);
					Toast.makeText(this, text, Toast.LENGTH_LONG).show();
				}
			}
			break;
		}

	}

	/**
	 * Checks if the patient has selected some reason to advice
	 * 
	 * @return
	 */
	private boolean isValidReason()
	{
		if (spinner != null)
		{
			if (spinner.getSelectedItemPosition() == 0)
			{
				return false;
			} else
			{
				return true;
			}

		} else
		{
			return false;
		}
	}

	/**
	 * This function hide the other elements from View before the envelop animation starts
	 */
	private void hideViews()
	{
		spinner.setVisibility(View.INVISIBLE);
		datePicker.setVisibility(View.INVISIBLE);
		comment.setVisibility(View.INVISIBLE);
		TextView selectReason = (TextView) findViewById(R.id.select_reason_text);
		selectReason.setVisibility(View.INVISIBLE);
		TextView date = (TextView) findViewById(R.id.TextView01);
		date.setVisibility(View.INVISIBLE);
		TextView c = (TextView) findViewById(R.id.textView1);
		c.setVisibility(View.INVISIBLE);
	}

	private AlertMsg getAlertFromView()
	{
		if (comment != null && spinner != null && datePicker != null)
		{
			String reason = (String) spinner.getSelectedItem();
			String comment_text = comment.getText() != null ? comment.getText().toString() : "-";
			String date_of_advice = String.valueOf(datePicker.getDayOfMonth()) + "-" + String.valueOf(datePicker.getMonth()) + "-"
					+ String.valueOf(datePicker.getYear());
			String displayName = getResources().getString(R.string.alert_advice_display_name);
			String doctor_msg = String.format(getResources().getString(R.string.alert_advice_doctor_msg), reason, date_of_advice, comment_text);
			return new AlertMsg.Builder().setTimeStamp(new GregorianCalendar())
											.setType(AlertType.ADVICE)
											.setDescription(doctor_msg)
											.setDisplayName(displayName)
											.create();
		}
		return null;
	}

	private void startEnvelopeAnim()
	{
		hideViews();
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View envelopeAnimView = inflater.inflate(R.layout.advice_envelope_send, null);
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
				sendButton.setText(R.string.sending_advice);
				sendButton.setEnabled(false);
				((TextView) findViewById(R.id.sending_advice_message)).setText(R.string.sending_advice);

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
				envelopeAnimView.findViewById(R.id.advice_envelope_success).setVisibility(View.VISIBLE);
				envelopeAnimView.findViewById(R.id.advice_envelope).setVisibility(View.INVISIBLE);
				((TextView) findViewById(R.id.sending_advice_message)).setText(R.string.sending_advice_success);
			}
		});

		findViewById(R.id.advice_envelope).startAnimation(envelopeAnim);

	}

	private class SendSymptomTask extends AsyncTask<AlertMsg, Integer, Boolean> {

		@Override
		protected void onPreExecute()
		{
			startEnvelopeAnim();
		}

		@Override
		protected Boolean doInBackground(AlertMsg... params)
		{
			AlertMsg a = params[0];
			ServandoPlatformFacade.getInstance().alert(a);
			return true;
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
