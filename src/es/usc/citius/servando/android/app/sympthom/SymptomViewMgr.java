package es.usc.citius.servando.android.app.sympthom;

import android.view.View;

/**
 * 
 * @author joseangel.pineiro
 */
public interface SymptomViewMgr {

	public int getView();

	public void onViewCreated(View v);

	public void completeFromView(View v, Symptom symptom);

}
