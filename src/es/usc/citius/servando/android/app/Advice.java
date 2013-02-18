package es.usc.citius.servando.android.app;

public class Advice {

	private String content;
	private boolean viewed;

	public Advice()
	{
	}

	public Advice(String content, boolean viewed)
	{
		super();
		this.content = content;
		this.viewed = viewed;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public boolean isViewed()
	{
		return viewed;
	}

	public void setViewed(boolean viewed)
	{
		this.viewed = viewed;
	}

}
