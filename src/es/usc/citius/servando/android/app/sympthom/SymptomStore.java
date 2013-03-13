package es.usc.citius.servando.android.app.sympthom;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymptomStore {

	private static SymptomStore instance = new SymptomStore();

	/**
	 * Static method to obtain the unique instance
	 * 
	 * @return
	 */
	public static SymptomStore getInstance()
	{
		return instance;
	}

	private SymptomStore()
	{
		symptoms = new HashMap<String, Symptom>();
		// TODO: load from file
		loadSymptoms(null);
	}

	private Map<String, Symptom> symptoms;

	/**
	 * Symptoms map
	 */
	public void loadSymptoms(File f)
	{
		Symptom headache = new Symptom.Builder().setId("headache")
												.setName("Dor de cabeza")
												.setDescription("Dor de cabeza moderado")
												.create();

		Symptom pillows = new Symptom.Builder().setId("pillowNumber")
												.setName("Número de almofadas")
												.setDescription("Precísanse aumentar o número de almofadas pra durmir")
												.setViewMgr(new PillowSymptomMgr())
												.create();

		symptoms.put(headache.getId(), headache);
		symptoms.put(pillows.getId(), pillows);
	}

	/**
	 * Get all symptoms in the store
	 * 
	 * @return
	 */
	public List<Symptom> getAll()
	{
		return new ArrayList<Symptom>(symptoms.values());
	}

	/**
	 * Get a symptom by id
	 */
	public Symptom getById(String id)
	{
		return symptoms.get(id);
	}

}
