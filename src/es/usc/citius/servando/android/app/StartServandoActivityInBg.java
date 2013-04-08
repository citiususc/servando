package es.usc.citius.servando.android.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import es.usc.citius.servando.android.ServandoPlatformFacade;
import es.usc.citius.servando.android.ServandoPlatformFacade.PlatformFacadeListener;
import es.usc.citius.servando.android.agenda.ServandoBackgroundService;
import es.usc.citius.servando.android.models.protocol.MedicalAction;
import es.usc.citius.servando.android.models.protocol.MedicalActionMgr;
import es.usc.citius.servando.android.models.services.IPlatformService;

public class StartServandoActivityInBg extends Activity implements PlatformFacadeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_servando_activity_in_bg);
		if (ServandoPlatformFacade.isStarted())
		{
			startApplication();
		}
	}

	private void returnResultAndFinish()
	{
		Intent data = new Intent();

		if (getParent() == null)
		{
			setResult(Activity.RESULT_OK, data);
		} else
		{
			getParent().setResult(Activity.RESULT_OK, data);
		}
		finish();
	}

	private void startApplication()
	{
		// ServandoApplication.updateLocale(this);
		try
		{
			ServandoPlatformFacade.getInstance().addListener(this);
			startService(new Intent(getApplicationContext(), ServandoBackgroundService.class));

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}



	@Override
	public void onReady()
	{
		loadServices();
		returnResultAndFinish();
	}

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

}
