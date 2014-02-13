package es.usc.citius.servando.android.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

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
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.settings.ServandoStartConfig;

public class UpdateActivity extends Activity {

	private static final String TAG = UpdateActivity.class.getSimpleName();
	private TextView loadingMessage;
	private ProgressBar progressBar;
	private TextView info;
	Handler h = new Handler();

	private static ILog log = ServandoLoggerFactory.getLogger(AppManager.class);

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
				v.setVisibility(View.INVISIBLE);
				info.setVisibility(View.INVISIBLE);
				progressBar.setVisibility(View.VISIBLE);
				loadingMessage.setVisibility(View.VISIBLE);
				String text = getResources().getString(R.string.downloading_updates);
				loadingMessage.setText(text);

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

	private class ReinstallServandoTask extends AsyncTask<String, Integer, String> {

		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String downloadFilePath = externalStoragePath + "/ServandoPlatformApp.apk";

		@Override
		protected String doInBackground(String... sUrl)
		{
			log.debug("Downloading apk...");
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
				log.error("An error ocurred downloading apk", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			Log.d(TAG, "Setting timer...");
			progressBar.setVisibility(View.INVISIBLE);
			String text = getResources().getString(R.string.updating_application);
			loadingMessage.setText(text);

			log.debug("Opening apk file...");

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
			progressBar.setProgress(values[0]);
			String string = getResources().getString(R.string.downloading_data);
			String textProgress = String.format(string, values[0]);

			loadingMessage.setText(textProgress);
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

	}

}
