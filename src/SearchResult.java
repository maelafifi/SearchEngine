/**
 * Class  to store and sort search results.
 * Takes in frequency, firstOccurrence, and path.
 * 
 *
 */
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
	
	/**
	 * @return
	 * 				Frequency of a word or words
	 */
	public int getFrequency()
	{
		return frequency;
	}
	
	/**
	 * @return
	 * 				The initial position of the search word(s) in a particular path
	 */
	public int getFirstOccurrence()
	{
		return firstOccurrence;
	}
	
	/**
	 * @return
	 * 				The path where the search word(s) was found
	 */
	public String getPath()
	{
		return path;
	}
	
	/**
	 * @param frequency
	 * 					Updates the frequency of the word(s)
	 */
	public void updateFrequency(int frequency)
	{
		this.frequency += frequency;
	}
	
	/**
	 * @param firstOccurrence
	 * 					Updates the first occurrence of the word(s)
	 */
	public void updateFirstOccurrence(int firstOccurrence)
	{
		if(this.firstOccurrence > firstOccurrence)
		{
			this.firstOccurrence = firstOccurrence;
		}
	}
	
	/**
	 * Overrides the compareTo method to sort searchResults by frequency.
	 * If frequency's are equal, sorts by first occurrence.
	 * If frequency and first occurrence are both equal, sorts by path.
	 */
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
			return this.path.compareTo(search.path);
		}
	}

}
