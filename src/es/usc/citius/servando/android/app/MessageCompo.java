package es.usc.citius.servando.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MessageCompo extends Activity {

	private String mAction;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// Initialization skipped

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent intent = getIntent();
		mAction = intent.getAction();

		// Handle the various intents that launch the message composer
		if (Intent.ACTION_VIEW.equals(mAction) || Intent.ACTION_SENDTO.equals(mAction) || Intent.ACTION_SEND.equals(mAction)
				|| Intent.ACTION_SEND_MULTIPLE.equals(mAction))
		{
			String text = intent.getStringExtra(Intent.EXTRA_TEXT);

			String[] elems = text.replace("\n", "###").split("###");

			String activity = "", time = "", fecha = "";

			for (String s : elems)
			{
				System.out.println("Linea [" + s + "]");
				if (s.contains("Tipo de actividad"))
				{
					activity = s.split("actividad:")[1];

				} else if (s.contains("Tiempo total"))
				{
					time = s.split("total:")[1];
				} else if (s.contains("Registro"))
				{
					fecha = s.split("Registro:")[1];
				}
			}

			((TextView) findViewById(R.id.text_view)).setText("O paciente foi " + activity + " durante " + time);

			// System.out.println((text != null) ? text : "null");
		} else
		{
			// Otherwise, handle the internal cases (Message Composer invoked from within app)
		}
	}
}