package es.usc.citius.servando.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import es.usc.citius.servando.android.ServandoPlatformFacade;

public class VersionNotesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_version_notes);

		String title = "v-" + ServandoPlatformFacade.getInstance().version(getApplicationContext());
		Spanned content = Html.fromHtml(ServandoPlatformFacade.getInstance().readVersionNotes());

		((TextView) findViewById(R.id.version_notes_title)).setText(title);
		((TextView) findViewById(R.id.version_notes_content)).setText(content, BufferType.SPANNABLE);
	}


}
