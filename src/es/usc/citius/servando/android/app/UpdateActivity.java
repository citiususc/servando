package es.usc.citius.servando.android.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import es.usc.citius.servando.android.app.uiHelper.AppManager;
import es.usc.citius.servando.android.settings.ServandoStartConfig;

public class UpdateActivity extends Activity {

	private static final String TAG = UpdateActivity.class.getSimpleName();
	private TextView loadingMessage;
	private ProgressBar progressBar;
	private TextView info;
	Handler h = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);

		progressBar = (ProgressBar) findViewById(R.id.splash_progress);
		loadingMessage = (TextView) findViewById(R.id.loading_message);
		info = (TextView) findViewById(R.id.info);

		findViewById(R.id.updateButton).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				reinstallFromApk(getApplicationContext());

				// new CheckForUpdates().execute(VERSION_URL);
				v.setVisibility(View.INVISIBLE);
				info.setVisibility(View.INVISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				loadingMessage.setVisibility(View.VISIBLE);
				loadingMessage.setText("Descargando actualizacións...");

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update, menu);
		return true;
	}

	private void reinstallFromApk(Context ctx)
	{
		new ReinstallServandoTask().execute();
	}

	class ReinstallServandoTask extends AsyncTask<String, Integer, String> {

		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String downloadFilePath = externalStoragePath + "/ServandoPlatformApp.apk";

		@Override
		protected String doInBackground(String... sUrl)
		{
			Log.d(TAG, "Downloading apk...");
			try
			{
				URL url = new URL(ServandoStartConfig.getInstance().get(ServandoStartConfig.APK_URL));
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(4000);
				connection.connect();

				int fileLength = connection.getContentLength();

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
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			Log.d(TAG, "Setting timer...");
			progressBar.setVisibility(View.INVISIBLE);
			loadingMessage.setText("Actualizando aplicación...");

			h.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					loadApk(downloadFilePath);
				}
			}, 1000);
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			super.onProgressUpdate(values);
			Log.d(TAG, "Descargando actualizacións " + values[0] + "%");
			progressBar.setProgress(values[0]);
			loadingMessage.setText("Downloading data... (" + values[0] + "%)");
		}

	}

	private void loadApk(String path)
	{
		File apk = new File(path);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
		getApplicationContext().startActivity(intent);
		finish();
		AppManager.closeApplication(getApplicationContext());

	}

	class CheckForUpdates extends AsyncTask<String, Integer, String> {

		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String version = "";

		@Override
		protected String doInBackground(String... sUrl)
		{
			Log.d(TAG, "Checking for updates...");
			try
			{
				URL url = new URL(sUrl[0]);
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(4000);
				connection.connect();
				// download the file
				InputStream input = new BufferedInputStream(url.openStream());
				ByteArrayBuffer buff = new ByteArrayBuffer(1024);

				byte data[] = new byte[1024];
				int count;

				while ((count = input.read(data)) != -1)
				{
					buff.append((byte) count);
				}
				input.close();

				version = new String(buff.toByteArray());

			} catch (Exception e)
			{
				Log.e("TAG", "Error", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			Log.d(TAG, "Version: " + version);
		}

	}


}
