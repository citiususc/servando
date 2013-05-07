package es.usc.citius.servando.android.app.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.app.R;
import es.usc.citius.servando.android.settings.ServandoStartConfig;

/**
 * This activity shows a splash screen and performs the necesary configurations (bluethooth enabling, logs, ...)
 * 
 * @author Ángel Piñeiro
 * 
 */
public class InstallActivity extends Activity {
	private TextView loadingMessage;
	private View loadingIndicator;
	private View installOptions;
	private Button fromFileButton;
	private Button fromUrlButton;

	TextToSpeech tts;
	private static final String DEBUG_TAG = InstallActivity.class.getSimpleName();
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
		setContentView(R.layout.activity_install);
		progressBar = (ProgressBar) findViewById(R.id.splash_progress);
		loadingMessage = (TextView) findViewById(R.id.loading_message);
		loadingIndicator = findViewById(R.id.loading);
		installOptions = findViewById(R.id.install_options);
		fromFileButton = (Button) findViewById(R.id.install_from_file_button);
		fromUrlButton = (Button) findViewById(R.id.install_from_url_button);

		fromFileButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				extractDefaultPatientData();
			}
		});

		fromUrlButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				downloadAndExtract();
			}
		});

	}

	protected void extractDefaultPatientData()
	{
		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();

		File sd = new File(externalStoragePath);
		String files[] = sd.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{
				return filename.startsWith("Servando") && filename.endsWith(".zip");
			}
		});

		Log.d("Splash", Arrays.toString(files));

		CharSequence[] choiceList;

		if (files != null && files.length > 0)
		{
			choiceList = new CharSequence[files.length];

			for (int i = 0; i < files.length; i++)
			{
				choiceList[i] = files[i];
			}

			showFileChooser(choiceList, files, sd.getAbsolutePath());
		}

	}

	private void showFileChooser(final CharSequence[] choiceList, final String files[], final String where)
	{

		final String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		final String unzipPath = externalStoragePath + ServandoStartConfig.getInstance().getPlatformInstallationPath() + "/";

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Setup from file:");
		builder.setCancelable(false);
		builder.setNegativeButton("Cancel", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				loadingIndicator.setVisibility(View.INVISIBLE);
			}
		});

		int selected = -1; // does not select anything
		builder.setSingleChoiceItems(choiceList, selected, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Toast.makeText(SplashActivity.this, "Select " + where + "/" + files[which],
				// Toast.LENGTH_SHORT).show();
				File zip = new File(where + "/" + files[which]);
				File where = new File(unzipPath);
				new DecompressTask(zip.getAbsolutePath(), where.getAbsolutePath() + "/").execute();
				dialog.cancel();
				Log.d(DEBUG_TAG, "Setting up app...");
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void downloadAndExtract()
	{

		Log.d(DEBUG_TAG, "Setting up app...");

		final EditText editText = new EditText(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setView(editText);
		builder.setTitle("Type url here:");
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				String url = editText.getText().toString();
				try
				{
					new URL(url);
					new DownloadAndInstallServandoSetupFile().execute(url);
				} catch (Exception e)
				{
					Toast.makeText(getApplication(), "Invalid URL format", Toast.LENGTH_SHORT).show();

				}

			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		});

		Dialog dialog = builder.create();
		dialog.show();

	}

	/**
	 * Launch the home activity
	 */
	private void startHomeActivity()
	{
		Class<?> homeActivity = ServandoPlatformFacade.getInstance().getSettings().isPatient() ? PatientHomeActivity.class : HomeActivity.class;
		startActivity(new Intent(InstallActivity.this, homeActivity));
	}

	private class DownloadAndInstallServandoSetupFile extends AsyncTask<String, Integer, String> {

		String externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String downloadFilePath = externalStoragePath + "/ServandoSetup.zip";
		String unzipPath = externalStoragePath + ServandoStartConfig.getInstance().getPlatformInstallationPath() + "/";

		boolean correctDownload = false;

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
				String urlStr = sUrl[0];
				url = new URL(urlStr);
				connection = url.openConnection();
				connection.setConnectTimeout(4000);
				connection.connect();

				int fileLength = connection.getContentLength();
				Log.d(DEBUG_TAG, "Downloading " + fileLength + "bytes of data from " + urlStr);

				if (fileLength > 0)
				{
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

					correctDownload = true;
				}

			} catch (Exception e)
			{
				Log.e("TAG", "Error", e);

				h.post(new Runnable()
				{
					@Override
					public void run()
					{
						findViewById(R.id.loading).setVisibility(View.INVISIBLE);
						loadingMessage.setText("Error downloading data!");
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

			if (correctDownload)
			{
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
						deleteFiles(ServandoStartConfig.getInstance().getPlatformInstallationPath());
					}
					new DecompressTask(zip.getAbsolutePath(), where.getAbsolutePath() + "/").execute();

				}
			}
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			showProgressBar();
			installOptions.setVisibility(View.INVISIBLE);
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
				loadingMessage.setText("Invalid file");
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
			loadingMessage.setText("Installation done!");

		}
	}

	void showProgressBar()
	{
		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);
		loadingMessage.setVisibility(View.VISIBLE);
		loadingIndicator.setVisibility(View.VISIBLE);

	}

	void hideProgressBar()
	{
		progressBar.setProgress(0);
		progressBar.setVisibility(View.INVISIBLE);
	}
}
