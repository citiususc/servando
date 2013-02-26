package es.usc.citius.servando.android.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import es.usc.citius.servando.android.advices.Advice;
import es.usc.citius.servando.android.advices.DailyReport;
import es.usc.citius.servando.android.agenda.PlatformResources;
import es.usc.citius.servando.android.agenda.PlatformResources.Available;
import es.usc.citius.servando.android.logging.ILog;
import es.usc.citius.servando.android.logging.ServandoLoggerFactory;
import es.usc.citius.servando.android.models.protocol.MedicalAction;
import es.usc.citius.servando.android.models.protocol.MedicalActionExecution;
import es.usc.citius.servando.android.models.protocol.MedicalActionPriority;
import es.usc.citius.servando.android.models.protocol.MedicalActionState;
import es.usc.citius.servando.android.models.services.IPlatformService;
import es.usc.citius.servando.android.models.util.ParameterList;

public class PlatformService implements IPlatformService {


	private static final String id = "SERVANDO";

	private static final ILog log = ServandoLoggerFactory.getLogger(PlatformService.class);

	public static final String DAILY_REPORT = "dailyreport";

	private Map<String, MedicalAction> providedActions;
	private Map<String, PlatformResources> resources;

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public void onPlatformStarted()
	{
	}

	@Override
	public void onPlatformStopping()
	{

	}

	@Override
	public List<MedicalAction> getProvidedActions()
	{
		if (providedActions == null)
		{
			providedActions = new HashMap<String, MedicalAction>();
			resources = new HashMap<String, PlatformResources>();

			MedicalAction dr = new MedicalAction(DAILY_REPORT);
			dr.setDisplayName("DR");
			dr.setProvider(this);
			dr.setDescription("DR");

			providedActions.put(dr.getId(), dr);
			resources.put(dr.getId(), PlatformResources.with(Available.NONE));
		}

		return new ArrayList<MedicalAction>(providedActions.values());
	}

	@Override
	public MedicalActionExecution newExecution(MedicalAction action, PlatformResources neededResources, ParameterList parameters,
			MedicalActionPriority priority, Calendar startDate, long timeWindow)
	{
		if (!providedActions.containsKey(action.getId()))
		{
			throw new IllegalArgumentException("Action " + action.getId() + " is not provided by the service " + this.getId());
		}

		MedicalActionExecution execution = new DailyReportMedicalActionExection(providedActions.get(action.getId()), parameters, priority,
				(GregorianCalendar) startDate, timeWindow);

		// Set execution needed resources
		execution.setResources(resources.get(action.getId()));

		log.debug("Creating new execution for action " + action.getId() + ", provider: "
				+ (execution.getAction().getProvider() != null ? execution.getAction().getProvider().getId() : "null"));

		return execution;
	}

	@Override
	public MedicalActionExecution restoreExecution(MedicalAction action, PlatformResources neededResources, ParameterList parameters,
			MedicalActionPriority priority, Calendar startDate, long timeWindow)
	{
		return newExecution(action, neededResources, parameters, priority, startDate, timeWindow);
	}

	private class DailyReportMedicalActionExection extends MedicalActionExecution {

		public DailyReportMedicalActionExection(MedicalAction medicalAction, ParameterList parameters, MedicalActionPriority priority, GregorianCalendar startDate, long timeWindow)
		{
			super(medicalAction, parameters, priority, startDate, timeWindow);
		}

		@Override
		public void start(Context ctx)
		{
			log.debug("Starting  " + getAction().getId() + ", state: " + state);

			if (state == MedicalActionState.NotStarted)
			{
				state = MedicalActionState.Uncompleted;
				
				for(Advice a : DailyReport.getInstance().getNotSeen()){
					if(Advice.SERVANDO_SENDER_NAME.equals(a.getSender())){
						ServandoAdviceMgr.getInstance().setHomeAdvice(a);
						break;
					}
				}
			}

			if (listener != null)
			{
				listener.onStart(this);
			}

			this.finish(ctx);
		}
	}

}
