package es.usc.citius.servando.android.app.activities;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import es.usc.citius.servando.android.app.R;

/**
 * ServandoPlatform about activity
 * 
 * @author Ángel Piñeiro
 * 
 */
public class AboutActivity extends ServandoActivity {

	private TextView usc_link;
	private TextView citius_link;
	private TextView servando_link;

	/**
	 *
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		usc_link = (TextView) findViewById(R.id.usc_link);
		citius_link = (TextView) findViewById(R.id.citius_link);
		servando_link = (TextView) findViewById(R.id.servando_link);

		usc_link.setMovementMethod(LinkMovementMethod.getInstance());
		citius_link.setMovementMethod(LinkMovementMethod.getInstance());
		servando_link.setMovementMethod(LinkMovementMethod.getInstance());

	}

	@Override
	protected int getViewId() {
		return R.layout.activity_about;
	}

	@Override
	protected String getActionBarTitle() {
		return "About";
	}

}
