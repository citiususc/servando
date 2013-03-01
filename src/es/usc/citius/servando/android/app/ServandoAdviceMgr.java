package es.usc.citius.servando.android.app;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.android.advices.Advice;
import es.usc.citius.servando.android.advices.storage.SQLiteAdviceDAO;

public class ServandoAdviceMgr {

	public interface HomeAdviceListener {
		public void onHomeAdvice(Advice advice);
	}

	private static final ServandoAdviceMgr instance = new ServandoAdviceMgr();

	private List<HomeAdviceListener> listeners;

	private ServandoAdviceMgr()
	{

		listeners = new ArrayList<ServandoAdviceMgr.HomeAdviceListener>();
		SQLiteAdviceDAO dao = SQLiteAdviceDAO.getInstance();
		dao.restartDataBase();

		// Date d1 = new Date();
		// d1.setDate(d1.getDate() - 1);
		//
		// Date d3 = new Date();
		// d3.setDate(d3.getDate() - 2);
		//
		// Date d7 = new Date();
		// d7.setDate(d7.getDate() - 4);
		//
		// Advice a1 = new Advice(Advice.SERVANDO_SENDER_NAME, "Consumo excesivo de sal.", d1);
		// Advice a2 = new Advice(Advice.SERVANDO_SENDER_NAME, "Consumo excesivo de alcohol.", d1);
		// Advice a3 = new Advice(Advice.SERVANDO_SENDER_NAME, "Deberías facer máis deporte.", d3);
		// Advice a4 = new Advice(Advice.SERVANDO_SENDER_NAME, "Deberías controlar a tensión.", d3);
		// Advice a5 = new Advice(Advice.SERVANDO_SENDER_NAME, "Deberías controlar o peso.", d3);
		// Advice a6 = new Advice("medico", "A túa cita do mércores queda confirmada para as 10:15h. <br/> Saúdos :)",
		// d3);
		// Advice a7 = new Advice(Advice.SERVANDO_SENDER_NAME, "Deberías controlar a saturación.", d7);
		//
		// dao.add(a1);
		// dao.add(a2);
		// dao.add(a3);
		// dao.add(a4);
		// dao.add(a5);
		// dao.add(a6);
		// dao.add(a7);
	}

	public static ServandoAdviceMgr getInstance()
	{
		return instance;
	}

	private Advice homeAdvice = null;

	public Advice getHomeAdvice()
	{
		return homeAdvice;
	}

	public void setHomeAdvice(Advice homeAdvice)
	{
		this.homeAdvice = homeAdvice;
		for (HomeAdviceListener l : listeners)
		{
			l.onHomeAdvice(homeAdvice);
		}
	}

	public void addAdviceListener(HomeAdviceListener l)
	{
		if (!listeners.contains(l))
			listeners.add(l);
	}

	public void removeAdviceListener(HomeAdviceListener l)
	{
		if (listeners.contains(l))
			listeners.remove(l);
	}

}
