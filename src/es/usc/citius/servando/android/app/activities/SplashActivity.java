package es.usc.citius.servando.android.app.activities;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.ServandoPlatformFacade.PlatformFacadeListener;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.medim.util.BluetoothUtils;
import es.usc.citius.servando.android.models.protocol.MedicalAction;
import es.usc.citius.servando.android.models.protocol.MedicalActionMgr;
import es.usc.citius.servando.android.models.services.IPlatformService;
import es.usc.citius.servando.android.ui.NotificationMgr;
import es.usc.citius.servando.android.ui.ServandoService;
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
	private ServandoService mBoundService;
	TextToSpeech tts;
	private static final String DEBUG_TAG = SplashActivity.class.getSimpleName();
	Handler h = new Handler();

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		printLocalIpAddress();

		Log.d(DEBUG_TAG, "Starting splash ...");

		if (ServandoService.isRunning())
		{
			Log.d(DEBUG_TAG, "Servando Service is already started.");
			startHomeActivity();
			finish();
		} else
		{
			setContentView(R.layout.splash);
			Log.d(DEBUG_TAG, "Servando Service is not started.");
			initializeUiResources();

			loadingMessage = (TextView) findViewById(R.id.loading_message);
			loadingMessage.setText("Configuring logs...");
			configureLogs();

			loadingMessage.setText("Configuring bluetooth...");
			configureBluetooth();

			loadingMessage.setText("Starting. Please wait...");

			h.post(new Runnable()
			{
				@Override
				public void run()
				{
					startServandoService();
				}
			});

			tts = new TextToSpeech(this.getApplicationContext(), this);

			// onReady();

		}
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
	protected void onNewIntent(Intent intent)
	{
		// UiUtils.showToast("onNewIntent", this);
		if (intent.getAction().equals(UNBIND_SERVANDO_SERVICE))
		{
			doUnbindService();
		}
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
	private void startServandoService()
	{
		ServandoPlatformFacade.getInstance().addListener(this);

		if (!ServandoService.isRunning())
		{
			Log.d(DEBUG_TAG, "Invoking servando service...");
			Intent intent = new Intent(SplashActivity.this, ServandoService.class);
			startService(intent);
			doBindService();
		}
	}

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
	private void configureBluetooth()
	{
		// Get the adapter
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null)
		{
			throw new NullPointerException("Device not supports bluetooth");
		}
		// gardamos o adaptador para futuros usos
		BluetoothUtils.getInstance().setAdapter(btAdapter);
		// If Bluetooth is not yet enabled, enable it
		if (!btAdapter.isEnabled())
		{
			Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH);
		}
	}

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

	/**
	 *
	 */
	private final ServiceConnection mConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mBoundService = ((ServandoService.ServandoBinder) service).getService();
			NotificationMgr.getInstance().setServandoService(mBoundService);
			loadServices();
			doUnbindService();
		}

		@Override
		public void onServiceDisconnected(ComponentName className)
		{
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
		}
	};

	private void doBindService()
	{
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(SplashActivity.this, ServandoService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	/**
	 *
	 */
	public void doUnbindService()
	{
		if (mIsBound)
		{
			Log.d(DEBUG_TAG, "UnbindService");
			// UiUtils.showToast("UnbindService", this);
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
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
	protected void onDestroy()
	{
		// UiUtils.showToast("unbind service", this);
		doUnbindService();
		super.onDestroy();

	}

	@Override
	public void onReady()
	{
		// task to start the home activity after a few seconds
		TimerTask initHomeTask = new TimerTask()
		{
			@Override
			public void run()
			{
				startHomeActivity();
				finish();
			}
		};
		// timer to schedule the task
		Timer timer = new Timer();
		loadingMessage.setText("Starting ...");
		timer.schedule(initHomeTask, SPLASH_DELAY_IN_SECONDS * 1000);

	}

	// private void saveTestDeviceInfo()
	// {
	//
	// DeviceInfo ecg = new DeviceInfo();
	// ecg.setDeviceType(DeviceType.ELECTROCARDIOGRAPH);
	// ecg.setAcquisitionFrecuency(250);
	// ecg.setDeviceId("BT3_6");
	// ecg.setDeviceName("BT3/6");
	// ecg.setModel("Corscience BT3/6");
	// ecg.setServiceName("ECG service");
	// ecg.addSignal(new MITSignalSpecification(16, (short) 250, 95.057f, "mV", 12, "I"));
	// ecg.addSignal(new MITSignalSpecification(16, (short) 250, 95.057f, "mV", 12, "II"));
	// ecg.addSignal(new MITSignalSpecification(16, (short) 250, 95.057f, "mV", 12, "III"));
	// ecg.addSensor(new Sensor("R"));
	// ecg.addSensor(new Sensor("L"));
	// ecg.addSensor(new Sensor("F"));
	// ecg.addSensor(new Sensor("N"));
	//
	// DeviceInfo sa = new DeviceInfo();
	// sa.setDeviceType(DeviceType.ELECTROCARDIOGRAPH);
	// sa.setAcquisitionFrecuency(250);
	// sa.setDeviceId("Nonin_4100");
	// sa.setDeviceName("Nonin_Medical_Inc._");
	// sa.setModel("Nonin 4100");
	// sa.setServiceName("Nonin POD");
	// sa.addSignal(new MITSignalSpecification(16, (short) 1, 100f, "%", 7, "SaO2"));
	// sa.addSignal(new MITSignalSpecification(16, (short) 75, 254f, "#", 7, "Pleth"));
	// sa.addSensor(new Sensor("SaO2"));
	//
	// DeviceInfo mit = new DeviceInfo();
	// mit.setDeviceType(DeviceType.ELECTROCARDIOGRAPH);
	// mit.setAcquisitionFrecuency(250);
	// mit.setDeviceId("Ecg_MITSim");
	// mit.setDeviceName("MITSimulator");
	// mit.setModel("ECG");
	// mit.setServiceName("ECG Service");
	// mit.addSignal(new MITSignalSpecification(16, (short) 250, 200f, "mV", 12, "I"));
	// mit.addSignal(new MITSignalSpecification(16, (short) 250, 200f, "mV", 12, "II"));
	// mit.addSensor(new Sensor("R"));
	// mit.addSensor(new Sensor("L"));
	// mit.addSensor(new Sensor("F"));
	// mit.addSensor(new Sensor("N"));
	//
	// try
	// {
	// String path = MedimServiceHelper.getInstance().getStorageHelper().createWorkingDirectory("devices");
	// DeviceMgr.storeDeviceInfo(ecg, path);
	// DeviceMgr.storeDeviceInfo(sa, path);
	// DeviceMgr.storeDeviceInfo(mit, path);
	//
	// ecg = DeviceMgr.loadDeviceInfo(new File(path + "/" + ecg.getDeviceId() + ".deviceinfo"));
	// sa = DeviceMgr.loadDeviceInfo(new File(path + "/" + sa.getDeviceId() + ".deviceinfo"));
	// mit = DeviceMgr.loadDeviceInfo(new File(path + "/" + mit.getDeviceId() + ".deviceinfo"));
	//
	// DeviceMgr.storeDeviceInfo(ecg, path);
	// DeviceMgr.storeDeviceInfo(sa, path);
	// DeviceMgr.storeDeviceInfo(mit, path);
	//
	// } catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
}
