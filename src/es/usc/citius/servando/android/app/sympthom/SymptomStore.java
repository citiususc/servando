package es.usc.citius.servando.android.app.sympthom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import es.usc.citius.servando.android.app.R;

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

	}

	private Map<String, Symptom> symptoms;

	/**
	 * Symptoms map
	 */
	public void loadSymptoms(Context ctx)
	{
		if (symptoms.isEmpty())
		{
			Symptom headache = new Symptom.Builder().setId("headache")
													.setName(ctx.getString(R.string.symptom_headache))
													.setDescription("Dor de cabeza moderado")
													.setViewMgr(new HeadacheSymptomMgr())
													.create();

			Symptom pillows = new Symptom.Builder().setId("pillowNumber")
													.setName(ctx.getString(R.string.symptom_pillow))
													.setDescription("Precísanse aumentar o número de almofadas pra durmir")
													.setViewMgr(new PillowSymptomMgr())
													.create();

			Symptom dizziness = new Symptom.Builder().setId("dizziness")
														.setName(ctx.getString(R.string.symptom_dizziness))
														.setDescription("Síntomas de mareos ou náuseas")
														.setViewMgr(new DizzinessSymptomMgr())
														.create();

			Symptom edema = new Symptom.Builder().setId("edema")
													.setName(ctx.getString(R.string.symptom_edema))
													.setDescription("Síntomas de edema")
													.setViewMgr(new EdemaSymptomMgr())
													.create();

			symptoms.put(headache.getId(), headache);
			symptoms.put(pillows.getId(), pillows);
			symptoms.put(dizziness.getId(), dizziness);
			symptoms.put(edema.getId(), edema);
		}
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
