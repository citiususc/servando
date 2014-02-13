package es.usc.citius.servando.android.app.fragments;

import java.util.HashMap;

import es.usc.citius.servando.android.ui.ServiceFragmentView;

/**
 * 
 * @author Ángel Piñeiro Clase encargada de almacenar os fragments (vistas) de cada unha das actuacións médicas que a
 *         proporcionen, para evitar instancialos máis dunha vez
 */
public class MedicalActionFragmentMgr {

	private HashMap<String, ServiceFragmentView> fragments;
	/**
	 * Singleton unique instance
	 */
	private static final MedicalActionFragmentMgr instance = new MedicalActionFragmentMgr();

	/**
	 * Private constructor to avoid multiple instances
	 */
	private MedicalActionFragmentMgr() {
		fragments = new HashMap<String, ServiceFragmentView>();
	}

	/**
	 * Static member to obtain the unique instance
	 */
	public static MedicalActionFragmentMgr getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param actionId
	 * @param f
	 */
	public void addFragment(String actionId, ServiceFragmentView f) {
		fragments.put(actionId, f);
	}

	/**
	 * 
	 * @param actionId
	 * @param f
	 */
	public ServiceFragmentView getFragment(String actionId) {
		return fragments.get(actionId);
	}

	/**
	 * 
	 * @param actionId
	 */
	public void removeFragment(String actionId) {
		fragments.remove(actionId);
	}

	/**
	 * 
	 * @param actionId
	 * @return
	 */
	public boolean contains(String actionId) {
		return fragments.containsKey(actionId);
	}
}
