package es.usc.citius.servando.android.app.activities;

import java.io.File;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import es.usc.citius.servando.android.medim.MedimServiceHelper;
import es.usc.citius.servando.android.medim.Drivers.DeviceMgr;
import es.usc.citius.servando.android.medim.model.devices.DeviceInfo;
import es.usc.citius.servando.android.medim.model.devices.DeviceType;
import es.usc.citius.servando.android.medim.model.devices.Sensor;
import es.usc.citius.servando.android.models.MIT.MITSignalSpecification;
import es.usc.citius.servando.android.util.BluetoothUtils;

public class InitializeConfigActivity extends Activity {

	public static final int ENABLE_BLUETOOTH = 1;

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		configureBluetoothAdapter();
		saveTestDeviceInfo();
		finish();
	}

	private void saveTestDeviceInfo() {

		DeviceInfo ecg = new DeviceInfo();
		ecg.setDeviceType(DeviceType.ELECTROCARDIOGRAPH);
		ecg.setAcquisitionFrecuency(250);
		ecg.setDeviceId("BT3_6");
		ecg.setDeviceName("BT3/6");
		ecg.setModel("Corscience BT3/6");
		ecg.setServiceName("ECG service");
		ecg.addSignal(new MITSignalSpecification(16, (short) 250, 95.057f, "mV", 12, "I"));
		ecg.addSignal(new MITSignalSpecification(16, (short) 250, 95.057f, "mV", 12, "II"));
		ecg.addSignal(new MITSignalSpecification(16, (short) 250, 95.057f, "mV", 12, "III"));
		ecg.addSensor(new Sensor("R"));
		ecg.addSensor(new Sensor("L"));
		ecg.addSensor(new Sensor("F"));
		ecg.addSensor(new Sensor("N"));

		DeviceInfo sa = new DeviceInfo();
		sa.setDeviceType(DeviceType.ELECTROCARDIOGRAPH);
		sa.setAcquisitionFrecuency(250);
		sa.setDeviceId("Nonin_4100");
		sa.setDeviceName("Nonin_Medical_Inc._");
		sa.setModel("Nonin 4100");
		sa.setServiceName("Nonin POD");
		sa.addSignal(new MITSignalSpecification(16, (short) 1, 100f, "%", 7, "SaO2"));
		sa.addSignal(new MITSignalSpecification(16, (short) 75, 254f, "#", 7, "Pleth"));
		sa.addSensor(new Sensor("SaO2"));

		DeviceInfo mit = new DeviceInfo();
		mit.setDeviceType(DeviceType.ELECTROCARDIOGRAPH);
		mit.setAcquisitionFrecuency(250);
		mit.setDeviceId("Ecg_MITSim");
		mit.setDeviceName("MITSimulator");
		mit.setModel("ECG");
		mit.setServiceName("ECG Service");
		mit.addSignal(new MITSignalSpecification(16, (short) 250, 200f, "mV", 12, "I"));
		mit.addSignal(new MITSignalSpecification(16, (short) 250, 200f, "mV", 12, "II"));
		mit.addSensor(new Sensor("R"));
		mit.addSensor(new Sensor("L"));
		mit.addSensor(new Sensor("F"));
		mit.addSensor(new Sensor("N"));

		try {

			String path = MedimServiceHelper.getInstance().getStorageHelper().createWorkingDirectory("devices");
			DeviceMgr.storeDeviceInfo(ecg, path);
			DeviceMgr.storeDeviceInfo(sa, path);
			DeviceMgr.storeDeviceInfo(mit, path);

			ecg = DeviceMgr.loadDeviceInfo(new File(path + "/" + ecg.getDeviceId() + ".deviceinfo"));
			sa = DeviceMgr.loadDeviceInfo(new File(path + "/" + sa.getDeviceId() + ".deviceinfo"));
			mit = DeviceMgr.loadDeviceInfo(new File(path + "/" + mit.getDeviceId() + ".deviceinfo"));

			DeviceMgr.storeDeviceInfo(ecg, path);
			DeviceMgr.storeDeviceInfo(sa, path);
			DeviceMgr.storeDeviceInfo(mit, path);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		// si é de bluetooth
		if (requestCode == ENABLE_BLUETOOTH) {
			if (resultCode == Activity.RESULT_OK) {

			} else if (resultCode == Activity.RESULT_CANCELED) {

			}
		}

	}

	private void configureBluetoothAdapter() {
		// Get the adapter
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

		if (btAdapter == null) {
			throw new NullPointerException("Device not supports bluetooth");
		}

		// gardamos o adaptador para futuros usos
		BluetoothUtils.getInstance().setAdapter(btAdapter);

		// If Bluetooth is not yet enabled, enable it
		if (!btAdapter.isEnabled()) {
			Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH);
		}
	}

}
