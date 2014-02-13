package es.usc.citius.servando.android.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.app.uiHelper.AppManager;
import es.usc.citius.servando.android.settings.ServandoSettings;

/**
 * ServandoPlatform about activity
 * 
 * @author Ángel Piñeiro
 * 
 */
public class SettingsActivity extends ServandoActivity {

	private static final String DEBUG_TAG = SettingsActivity.class.getSimpleName();

	private ServandoSettings settings;

	private EditText serverUrl;
	private Button saveButton;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		settings = ServandoPlatformFacade.getInstance().getSettings();
		initComponents();
		setupListeners();
	}

	private void setupListeners()
	{
		saveButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String settingsUrlText = serverUrl.getText().toString().trim();

				if (!settings.getServerUrl().equals(settingsUrlText))
				{
					settings.setServerUrl(settingsUrlText);
				}

				try
				{
					ServandoPlatformFacade.getInstance().saveSettings();

					AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
					builder.setTitle("Settings saved!")
							.setMessage("Changes will not take effect until you restart the application. Do you want to restart it now?")
							.setCancelable(false)
							.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int id)
								{
									AppManager.restartApplication(SettingsActivity.this);
									finish();
								}
							})
							.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int id)
								{
									SettingsActivity.this.finish();
								}
							});
					AlertDialog dialog = builder.create();
					dialog.show();

				} catch (Exception e)
				{
					Log.e(DEBUG_TAG, "Error saving settings", e);
				}
			}
		});

	}

	private void initComponents()
	{
		saveButton = (Button) findViewById(R.id.save_button);
		serverUrl = (EditText) findViewById(R.id.server_url);
		serverUrl.setText(settings.getServerUrl());
	}

	@Override
	protected int getViewId()
	{
		return R.layout.activity_settings;
	}

	@Override
	protected String getActionBarTitle()
	{
		return "Settings";
	}

	private void updateSettings()
	{
		ServandoPlatformFacade.getInstance().getSettings().setServerUrl("");

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

}
