package es.usc.citius.servando.android.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.agenda.ServandoBackgroundService;
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
	private TextView version;
	private Button versionNotesButton;
	private View logo;

	/**
	 *
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		usc_link = (TextView) findViewById(R.id.usc_link);
		citius_link = (TextView) findViewById(R.id.citius_link);
		servando_link = (TextView) findViewById(R.id.servando_link);
		version = (TextView) findViewById(R.id.version);
		versionNotesButton = (Button) findViewById(R.id.version_notes_button);
		logo = (ImageView) findViewById(R.id.about_servando_logo);

		usc_link.setMovementMethod(LinkMovementMethod.getInstance());
		citius_link.setMovementMethod(LinkMovementMethod.getInstance());
		servando_link.setMovementMethod(LinkMovementMethod.getInstance());

		((TextView) findViewById(R.id.version_notes_button)).setText(getString(R.string.version_notes));

		version.setText("v-" + ServandoPlatformFacade.getInstance().version(getApplicationContext()));
		versionNotesButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// Intent i = new Intent(AboutActivity.this, VersionNotesActivity.class);
				// i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				// startActivity(i);
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View dialogView = inflater.inflate(R.layout.activity_version_notes, null);
				String title = "v-" + ServandoPlatformFacade.getInstance().version(getApplicationContext());
				String content = Html.fromHtml(ServandoPlatformFacade.getInstance().readVersionNotes()).toString();
				((TextView) dialogView.findViewById(R.id.version_notes_title)).setText(title);
				((TextView) dialogView.findViewById(R.id.version_notes_content)).setText(content);
				AlertDialog d = new AlertDialog.Builder(AboutActivity.this).setInverseBackgroundForced(true).setView(dialogView).create();
				d.show();
			}
		});
	}

	@Override
	protected int getViewId()
	{
		return R.layout.activity_about;
	}

	@Override
	protected String getActionBarTitle()
	{
		return "About";
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (R.id.check_for_updates == id)
		{
			ServandoBackgroundService.$.getInstance().checkForUpdates();
			return true;
		}
		return false;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.about_menu, menu);
		return true;
	}

}
