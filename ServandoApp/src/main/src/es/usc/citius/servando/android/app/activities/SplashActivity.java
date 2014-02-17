package es.usc.citius.servando.android.app.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.ServandoPlatformFacade.PlatformFacadeListener;
import es.usc.citius.servando.android.agenda.ServandoBackgroundService;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.models.protocol.MedicalAction;
import es.usc.citius.servando.android.models.protocol.MedicalActionMgr;
import es.usc.citius.servando.android.models.services.IPlatformService;
import es.usc.citius.servando.android.settings.ServandoStartConfig;
import es.usc.citius.servando.android.ui.NotificationMgr;
import es.usc.citius.servando.android.util.BluetoothUtils;

/**
 * This activity shows a splash screen and performs the necesary configurations (bluethooth enabling, logs, ...)
 * 
 * @author Ángel Piñeiro
 * 
 */
public class SplashActivity extends Activity implements OnInitListener, PlatformFacadeListener {

	private static final int SPLASH_DELAY_IN_SECONDS = 0;

	private static final int ENABLE_BLUETOOTH = 1;

	public static final String UNBIND_SERVANDO_SERVICE = "es.usc.citius.servando.android.UNBIND_SERVANDO_SERVICE";

	private boolean mIsBound;
	private TextView loadingMessage;
	View loadingIndicator;

	TextToSpeech tts;
	private static final String DEBUG_TAG = SplashActivity.class.getSimpleName();
	ProgressBar progressBar;

	Handler h = new Handler();

	String patientFolder;

	String externalStoragePath;
	String unzipPath;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		unzipPath = externalStoragePath + ServandoStartConfig.getInstance().getPlatformInstallationPath() + "/";
		printLocalIpAddress();

		Log.d(DEBUG_TAG, "Starting splash ...");

		if (ServandoPlatformFacade.isStarted())
		{
			Log.d(DEBUG_TAG, "Servando is already started.");
			startHomeActivity();
			finish();
		} else
		{
			Log.d(DEBUG_TAG, "Servando is not started");
			setContentView(R.layout.splash);
			progressBar = (ProgressBar) findViewById(R.id.splash_progress);
			loadingMessage = (TextView) findViewById(R.id.loading_message);

			loadingIndicator = findViewById(R.id.loading);

			h.postDelayed(new Runnable()
			{

				@Override
				public void run()
				{

                    if (ServandoStartConfig.getInstance().dynamicSetupEnabled())
                    {

                        copySettingsFromAssets();
                    }

					if (!ServandoStartConfig.getInstance().isPlatformSetupOnSDCard())
					{
							loadingMessage.setText("Servando no está correctamente configurado y no puede iniciarse. \n\nDisculpe las molestias");
							loadingIndicator.setVisibility(View.INVISIBLE);
					} else
					{
						startApplication();
					}
				}
			}, 100);

		}
	}

    private void copySettingsFromAssets() {

        Log.d(DEBUG_TAG,"Copying default files from assets to sdcard...");

        String platformDir =Environment.getExternalStorageDirectory().getPath() +"/"
                + ServandoStartConfig.getInstance().get(ServandoStartConfig.EXTERNAL_PATH) + "/"
                + ServandoStartConfig.getInstance().get(ServandoStartConfig.DIRECTORY);
        AssetManager am = getResources().getAssets();

        try {

            File f = new File(platformDir);

            if(!f.exists()){
                f.mkdirs();
            }

            InputStream settingsInputStream = am.open("settings.xml");
            InputStream patientInputStream = am.open("patient.xml");
            InputStream protocolInputStream = am.open("protocol.xml");


            OutputStream settingsOutput = new FileOutputStream(platformDir + "/settings.xml");
            OutputStream patientOutput = new FileOutputStream(platformDir+"/patient.xml");
            OutputStream protocolOutput = new FileOutputStream(platformDir+"/protocol.xml");

            copyFile(settingsInputStream,settingsOutput);
            copyFile(patientInputStream, patientOutput);
            copyFile(protocolInputStream,protocolOutput);

        } catch (IOException e) {
            Log.e(DEBUG_TAG,"Error copying default files to sdcard",e);
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
        out.flush();
        out.close();
        in.close();
    }


    private void startApplication()
	{

		Log.d(DEBUG_TAG, "Servando Service is not started.");

		// ServandoApplication.updateLocale(this);

		// initializeUiResources();
		if (BluetoothUtils.getInstance().getAdapter() == null)
		{
			BluetoothUtils.getInstance().setAdapter(BluetoothAdapter.getDefaultAdapter());
		}

		loadingMessage.setText("Starting... Please wait.");


		try
		{
			ServandoPlatformFacade.getInstance().addListener(this);

			// ServandoPlatformFacade.getInstance().start(getApplicationContext());
			startService(new Intent(getApplicationContext(), ServandoBackgroundService.class));

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// }
		// });

		// tts = new TextToSpeech(this.getApplicationContext(), this);
	}

	private void printLocalIpAddress()
	{
		Log.d(DEBUG_TAG, "Local ip address: " + getLocalIpAddress());
	}

	public String getLocalIpAddress()
	{
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress())
					{
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex)
		{
			Log.e(DEBUG_TAG, ex.toString());
		}
		return null;
	}

	@Override
	public void onInit(int status)
	{

		if (status == TextToSpeech.SUCCESS)
		{
			// Log.e("TTS", "Initilization Succeed!");
			NotificationMgr.getInstance().setTextToSpeech(tts);
		} else
		{
			Log.d("TTS", "Initilization Failed!");
		}
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{

		if (hasFocus)
		{
			final ImageView splash = (ImageView) findViewById(R.id.loading);
			((AnimationDrawable) splash.getDrawable()).start();
		}
		super.onWindowFocusChanged(hasFocus);
	}

	/**
	 * Launch the home activity
	 */
	private void startHomeActivity()
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		startActivity(new Intent(SplashActivity.this, homeActivity));
	}

	/**
	 *
	 */
	private void loadServices()
	{

		for (IPlatformService service : ServandoPlatformFacade.getInstance().getRegisteredServices().values())
		{
			for (MedicalAction a : service.getProvidedActions())
			{
				MedicalActionMgr.getInstance().addMedicalAction(a);
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		super.onActivityResult(requestCode, resultCode, data);

		// si é de bluetooth
		if (requestCode == ENABLE_BLUETOOTH)
		{
			if (resultCode == Activity.RESULT_OK)
			{

			} else if (resultCode == Activity.RESULT_CANCELED)
			{

			}
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public void onReady()
	{
		loadServices();
		// task to start the home activity after a few seconds
		final TimerTask initHomeTask = new TimerTask()
		{
			@Override
			public void run()
			{
				startHomeActivity();
				finish();
			}
		};
		h.post(new Runnable()
		{

			@Override
			public void run()
			{
				// timer to schedule the task
				Timer timer = new Timer();
				loadingMessage.setText("Starting ...");
				timer.schedule(initHomeTask, SPLASH_DELAY_IN_SECONDS * 1000);
			}
		});

	}


}
