package es.usc.citius.servando.android.app.sympthom;


public class Symptom {

	private String id;
	private String name;
	private String description;
	private String patientComment;

	private SymptomViewMgr viewMgr;

	private Symptom()
	{

	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public SymptomViewMgr getViewMgr()
	{
		if (viewMgr == null)
		{
			viewMgr = DefaultSymptomMgr.getInstance();
		}
		return viewMgr;
	}

	public void setViewMgr(SymptomViewMgr viewMgr)
	{
		this.viewMgr = viewMgr;
	}

	public String getPatientComment()
	{
		return patientComment;
	}

	public void setPatientComment(String patientComment)
	{
		this.patientComment = patientComment;
	}

	public static class Builder {

		private Symptom s;

		public Builder()
		{
			s = new Symptom();
		}

		public Symptom create()
		{
			return s;
		}

		public Builder setId(String id)
		{
			s.id = id;
			return this;
		}

		public Builder setName(String name)
		{
			s.name = name;
			return this;
		}

		public Builder setViewMgr(SymptomViewMgr mgr)
		{
			s.viewMgr = mgr;
			return this;
		}

		public Builder setDescription(String description)
		{
			s.description = description;
			return this;
		}

		public Builder setpatientComment(String comment)
		{
			s.patientComment = comment;
			return this;
		}

	}

}
