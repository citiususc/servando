package es.usc.citius.servando.android.app;

import java.io.IOException;
import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import es.usc.citius.servando.android.app.exception.AppExceptionHandler;
import es.usc.citius.servando.android.settings.ServandoStartConfig;
import es.usc.citius.servando.android.settings.StorageModule;
import es.usc.citius.servando.android.util.ServandoLocaleUtils;

public class ServandoApplication extends Application {

	public static int version = 1;

	private static final String TAG = ServandoApplication.class.getSimpleName();

	public static Locale locale = null;

	private static Handler h = new Handler();

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String lang = null;
		try
		{
			lang = settings.getString("lang", null);
			if (lang != null)
			{
				locale = new Locale(ServandoLocaleUtils.getValidLocale(lang));
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			lang = null;
		}

		if (locale != null)
		{
			newConfig.locale = locale;
			Locale.setDefault(locale);
			getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
			Log.d(TAG, "Locale: " + locale.toString());
		}

		newConfig.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate()
	{
		Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler(this));
        ServandoStartConfig.getInstance().load(this);
		updateLocale(this);
	}

	public static void updateLocale(Context ctx)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);

		Configuration config = ctx.getApplicationContext().getResources().getConfiguration();

		String lang;
		try
		{
			lang = StorageModule.getInstance().getSettings().getLanguage();
		} catch (IOException e)
		{
			lang = "es";
		}

		if (!"".equals(lang) && !config.locale.getLanguage().equals(lang))
		{
			locale = new Locale(ServandoLocaleUtils.getValidLocale(lang));
			Locale.setDefault(locale);
			config.locale = locale;
			ctx.getApplicationContext().getResources().updateConfiguration(config, ctx.getApplicationContext().getResources().getDisplayMetrics());
		}

		settings.edit().putString("lang", lang).commit();
	}

}
