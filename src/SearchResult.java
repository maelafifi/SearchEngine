
public class SearchResult implements Comparable<SearchResult>
{
	private int frequency;
	private int firstOccurrence;
	private final String path;
	
	public SearchResult(int frequency, int firstOccurrence, String path)
	{
		super();
		this.frequency = frequency;
		this.firstOccurrence = firstOccurrence;
		this.path = path;
		
	}
	
	public int getFrequency()
	{
		return frequency;
	}
	
	public int getFirstOccurrence()
	{
		return firstOccurrence;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public void updateFrequency(int frequency)
	{
		this.frequency += frequency;
	}
	
	public void updateFirstOccurrence(int firstOccurrence)
	{
		if(this.firstOccurrence > firstOccurrence)
		{
			this.firstOccurrence = firstOccurrence;
		}
	}
	@Override
	public int compareTo(SearchResult search)
	{
		if(this.frequency != search.frequency)
		{
			return Integer.compare(search.frequency, this.frequency);
		}
		else if(this.firstOccurrence != search.firstOccurrence)
		{
			return Integer.compare(this.firstOccurrence, search.firstOccurrence);
		}
		else
		{
			return this.path.compareToIgnoreCase(search.path);
		}
	}

}
