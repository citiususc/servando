package es.usc.citius.servando.android.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import es.usc.citius.servando.android.util.BluetoothUtils;

public class EnableBluetoothActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		enableBluetoothIfDisabled();
		finish();
	}

	private void enableBluetoothIfDisabled()
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
			enableBluetooth.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivityForResult(enableBluetooth, 1);
		}
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent data)
	// {
	//
	// super.onActivityResult(requestCode, resultCode, data);
	//
	// // si é de bluetooth
	// if (requestCode == 1)
	// {
	// if (resultCode == Activity.RESULT_OK)
	// {
	// BluetoothUtils.getInstance().setAdapter()
	// } else if (resultCode == Activity.RESULT_CANCELED)
	// {
	// Toast.makeText(this, "Cannot enable bluetooth", Toast.LENGTH_SHORT).show();
	// }
	// }
	//
	// }

}
