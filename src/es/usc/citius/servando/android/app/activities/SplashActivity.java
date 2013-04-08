package es.usc.citius.servando.android.app.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.ServandoPlatformFacade.PlatformFacadeListener;
import es.usc.citius.servando.android.agenda.ServandoBackgroundService;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.models.protocol.MedicalAction;
import es.usc.citius.servando.android.models.protocol.MedicalActionMgr;
import es.usc.citius.servando.android.models.services.IPlatformService;
import es.usc.citius.servando.android.settings.ServandoStartConfig;
import es.usc.citius.servando.android.sound.SoundHelper;
import es.usc.citius.servando.android.ui.NotificationMgr;
import es.usc.citius.servando.android.ui.animation.AnimationStore;

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

	TextToSpeech tts;
	private static final String DEBUG_TAG = SplashActivity.class.getSimpleName();
	ProgressBar progressBar;

	Handler h = new Handler();

	String patientFolder;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

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

			h.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					if (!ServandoStartConfig.getInstance().isPlatformSetupOnSDCard())
					{
						setupAppDir();
					} else
					{
						startApplication();
					}
				}
			}, 100);

		}
	}

	private void setupAppDir()
	{
		Log.d(DEBUG_TAG, "Setting up app...");

		final EditText editText = new EditText(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setView(editText);
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				patientFolder = editText.getText().toString();
				new DownloadAndInstallServandoSetupFile().execute();

			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
				finish();
			}
		});

		Dialog dialog = builder.create();
		dialog.show();

	}

	private void startApplication()
	{

		Log.d(DEBUG_TAG, "Servando Service is not started.");

		// ServandoApplication.updateLocale(this);

		// initializeUiResources();

		loadingMessage.setText("Configuring logs...");
		configureLogs();

		// loadingMessage.setText("Configuring bluetooth...");
		// configureBluetooth();

		loadingMessage.setText("Starting... Please wait.");

		// h.post(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// startServandoService();

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

		tts = new TextToSpeech(this.getApplicationContext(), this);
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

	/**
	 *
	 */
	private void initializeUiResources()
	{
		AnimationStore.getInstance().initialize(this);
		SoundHelper.initSounds(this);

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
	 * Starts Servando Service
	 */
	// private void startServandoService()
	// {
	// ServandoPlatformFacade.getInstance().addListener(this);
	//
	// if (!ServandoService.isRunning())
	// {
	// Log.d(DEBUG_TAG, "Invoking servando service...");
	// Intent intent = new Intent(SplashActivity.this, ServandoService.class);
	// startService(intent);
	// doBindService();
	// }
	// }

	/**
	 *
	 */
	private void configureLogs()
	{
		// logs initialization
		// String logsFilename = DataSource.getInstance().getBasePath() + "/" + "medimLog4j.log";
		// Log4JConfig.initialize(logsFilename, Level.ALL, true);
	}

	/**
	 *
	 */
	// private void configureBluetooth()
	// {
	// // Get the adapter
	// BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	// if (btAdapter == null)
	// {
	// throw new NullPointerException("Device not supports bluetooth");
	// }
	// // gardamos o adaptador para futuros usos
	// BluetoothUtils.getInstance().setAdapter(btAdapter);
	// // If Bluetooth is not yet enabled, enable it
	// if (!btAdapter.isEnabled())
	// {
	// Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	// startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH);
	// }
	// }

	/**
	 *
	 */
	private void loadServices()
	{
		// HashMap<String, IPlatformService> services = new HashMap<String, IPlatformService>();
		// IPlatformService service = new MedimService();
		// services.put(service.getId(), service);
		// ServiceManager.getInstance().setRegisteredServices(services);
		//
		// for (MedicalAction a : service.getProvidedActions()) {
		// MedicalActionMgr.getInstance().addMedicalAction(a);
		// }

		for (IPlatformService service : ServandoPlatformFacade.getInstance().getRegisteredServices().values())
		{
			for (MedicalAction a : service.getProvidedActions())
			{
				MedicalActionMgr.getInstance().addMedicalAction(a);
			}
		}

	}

	// /**
	// *
	// */
	// private final ServiceConnection mConnection = new ServiceConnection()
	// {
	// @Override
	// public void onServiceConnected(ComponentName className, IBinder service)
	// {
	// // This is called when the connection with the service has been
	// // established, giving us the service object we can use to
	// // interact with the service. Because we have bound to a explicit
	// // service that we know is running in our own process, we can
	// // cast its IBinder to a concrete class and directly access it.
	// mBoundService = ((ServandoService.ServandoBinder) service).getService();
	// NotificationMgr.getInstance().setServandoService(mBoundService);
	// loadServices();
	// doUnbindService();
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName className)
	// {
	// // This is called when the connection with the service has been
	// // unexpectedly disconnected -- that is, its process crashed.
	// // Because it is running in our same process, we should never
	// // see this happen.
	// mBoundService = null;
	// }
	// };

	// private void doBindService()
	// {
	// // Establish a connection with the service. We use an explicit
	// // class name because we want a specific service implementation that
	// // we know will be running in our own process (and thus won't be
	// // supporting component replacement by other applications).
	// bindService(new Intent(SplashActivity.this, ServandoService.class), mConnection, Context.BIND_AUTO_CREATE);
	// mIsBound = true;
	// }

	/**
	 *
	 */
	// public void doUnbindService()
	// {
	// if (mIsBound)
	// {
	// Log.d(DEBUG_TAG, "UnbindService");
	// // UiUtils.showToast("UnbindService", this);
	// // Detach our existing connection.
	// unbindService(mConnection);
	// mIsBound = false;
	// }
	// }

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

	// @Override
	// protected void onDestroy()
	// {
	// // UiUtils.showToast("unbind service", this);
	// doUnbindService();
	// super.onDestroy();
	//
	// }

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

	private class DownloadAndInstallServandoSetupFile extends AsyncTask<String, Integer, String> {

		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String downloadFilePath = externalStoragePath + "/ServandoSetup.zip";
		String unzipPath = externalStoragePath + ServandoStartConfig.getInstance().getPlatformInstallationPath() + "/";

		@Override
		protected String doInBackground(String... sUrl)
		{

			try
			{
				Log.d("Splash", "Starting download...");
				Log.d("Splash", "EEP: " + externalStoragePath);
				Log.d("Splash", "DFP: " + downloadFilePath);
				Log.d("Splash", "UZP: " + unzipPath);

				h.post(new Runnable()
				{
					@Override
					public void run()
					{
						loadingMessage.setText("Downloading data...");
					}
				});

				URL url = null;
				URLConnection connection = null;
				String urlStr = "";
				try
				{
					SharedPreferences prefs = getSharedPreferences("servando", Context.MODE_PRIVATE);
					String patientId = prefs.getString("patient_id", null);
					Log.d(DEBUG_TAG, "Patient id: " + patientId);

					if (patientFolder != null)
					{
						urlStr = ServandoStartConfig.getInstance()
													.get(ServandoStartConfig.SDCARD_PATIENT_DATA_URL)
													.replaceAll("%PATIENT_ID%", patientFolder);
					} else if (patientId != null)
					{
						urlStr = ServandoStartConfig.getInstance()
													.get(ServandoStartConfig.SDCARD_PATIENT_DATA_URL)
													.replaceAll("%PATIENT_ID%", patientId);

					} else
					{
						urlStr = ServandoStartConfig.getInstance().get(ServandoStartConfig.SDCARD_DATA_URL);
					}

					url = new URL(urlStr);
					connection = url.openConnection();
					connection.setConnectTimeout(4000);
					connection.connect();
				} catch (Exception e)
				{
					Log.d(DEBUG_TAG, "Cannot download data for patient ");
					// try with generic data
					url = new URL(ServandoStartConfig.getInstance().get(ServandoStartConfig.SDCARD_DATA_URL));
					connection = url.openConnection();
					connection.setConnectTimeout(4000);
					connection.connect();

				}

				int fileLength = connection.getContentLength();

				Log.d(DEBUG_TAG, "Downloading " + fileLength + "bytes of data from " + urlStr);
				// download the file
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(downloadFilePath);

				byte data[] = new byte[1024];
				long total = 0;
				int count;

				while ((count = input.read(data)) != -1)
				{
					total += count;
					// publishing the progress....
					publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();

			} catch (Exception e)
			{
				Log.e("TAG", "Error", e);

				h.post(new Runnable()
				{
					@Override
					public void run()
					{
						findViewById(R.id.loading).setVisibility(View.INVISIBLE);
						loadingMessage.setText("Update site not avaliable. Servando can not be started.");
					}
				});

			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			super.onProgressUpdate(values);
			progressBar.setProgress(values[0]);
			loadingMessage.setText("Downloading data... (" + values[0] + "%)");
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			hideProgressBar();

			Log.d("TAG", "finish download");

			File zip = new File(downloadFilePath);

			Log.d("Splash", "Exist: " + zip.exists());

			Log.d("Splash", "Unzipping to: " + unzipPath);

			if (zip.exists())
			{
				File where = new File(unzipPath);

				if (where.exists())
				{
					Log.d("Splash", "Deleting platform path...");
					// deleteFiles(ServandoStartConfig.getInstance().getPlatformInstallationPath());
				}

				new DecompressTask(zip.getAbsolutePath(), where.getAbsolutePath() + "/").execute();

				// zip.delete();
			}

			// startApplication();
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			showProgressBar();
			loadingMessage.setText("...");
		}
	}

	public static void deleteFiles(String path)
	{

		Log.d("Splash", "Deleting platform path");

		File file = new File(path);

		if (file.exists())
		{
			String deleteCmd = "rm -r " + path;
			Runtime runtime = Runtime.getRuntime();
			try
			{
				runtime.exec(deleteCmd);
			} catch (IOException e)
			{
			}
		}
	}

	public class DecompressTask extends AsyncTask<String, Integer, String> {

		private String _zipFile;
		private String _location;
		private int total;
		private int max;

		public DecompressTask(String zipFile, String location)
		{
			_zipFile = zipFile;
			_location = location;

			_dirChecker("");
		}

		private void _dirChecker(String dir)
		{
			File f = new File(_location + dir);

			if (!f.isDirectory())
			{
				f.mkdirs();
			}
		}

		@Override
		protected String doInBackground(String... params)
		{

			try
			{
				FileInputStream fin = new FileInputStream(_zipFile);
				ZipInputStream zin = new ZipInputStream(fin);
				ZipEntry ze = null;

				ZipFile zipFile = new ZipFile(_zipFile);
				max = zipFile.size();
				progressBar.setMax(max);

				while ((ze = zin.getNextEntry()) != null)
				{
					Log.v("Decompress", "Unzipping " + ze.getName());

					if (ze.isDirectory())
					{
						_dirChecker(ze.getName());
					} else
					{
						FileOutputStream fout = new FileOutputStream(_location + ze.getName());
						for (int c = zin.read(); c != -1; c = zin.read())
						{
							fout.write(c);
						}

						zin.closeEntry();
						fout.close();
					}

					total += 1;
					// publishing the progress....
					publishProgress((int) (total));

				}
				zin.close();
			} catch (Exception e)
			{
				Log.e("Decompress", "unzip", e);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			super.onProgressUpdate(values);
			int progress = values[0];
			progressBar.setProgress(progress);
			loadingMessage.setText("Setting up Servando... (" + (int) (((float) progress / max) * 100) + "%)");
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			showProgressBar();
			loadingMessage.setText("Setting up Servando...");
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			hideProgressBar();
			Log.d("TAG", "finish unzip");
			startApplication();
		}
	}

	void showProgressBar()
	{
		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);

	}

	void hideProgressBar()
	{
		progressBar.setProgress(0);
		progressBar.setVisibility(View.INVISIBLE);
	}
}
