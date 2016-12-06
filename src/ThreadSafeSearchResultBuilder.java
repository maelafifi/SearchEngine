import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Builds and stores a list search words, and their search results.
 * 
 */
public class ThreadSafeSearchResultBuilder
{
	private final TreeMap <String, ArrayList<SearchResult>> search;
	
	private final WorkQueue minions;
	
	ThreadSafeInvertedIndex index;
	
	private ReadWriteLock lock = new ReadWriteLock();
	
	/**
	 * Creates a new and empty treemap of the search results
	 */
	public ThreadSafeSearchResultBuilder(int numThreads, ThreadSafeInvertedIndex index)
	{
		search = new TreeMap <String, ArrayList<SearchResult>>();
		minions  = new WorkQueue(numThreads);
		this.index = index;
	}

	/**
	 * 
	 * @param pathName
	 * 					File containing the search word(s) to be search for
	 * @param searchType
	 * 					Type of search to be performed(0 for exact, 1 for partial)
	 * @param index
	 * 					The index of all words that the searchWords will search for words in
	 * @return
	 * @throws IOException
	 */
	public synchronized void parseSearchFile(Path pathName, int searchType) throws IOException
	{
		try(BufferedReader reader = Files.newBufferedReader(pathName, Charset.forName("UTF-8"));)
		{
			String line;
			while((line = reader.readLine()) != null)
			{
					minions.execute(new LineMinion(line, searchType, index));
			}
		}
	}
	
	/**
	 * Takes a line from the parseSearchFile, cleans the line, splits the words in the line,
	 * puts it into an array, and sends the array of words to be searched for with the exactSearch 
	 * or partialSearch methods
	 * @param line
	 * 					Search line from the opened file of search words
	 * @param searchType
	 * 					Type of search to be performed(0 for exact, 1 for partial)
	 * @param index
	 * 					The index of all words that the searchWords will search for words in
	 * @return
	 */
	public boolean searchForMatches(String line, int searchType, InvertedIndex index)
	{
		ArrayList<SearchResult> searchResults = new ArrayList<>();
		String cleanWord = line.replaceAll("\\p{Punct}+", "").toLowerCase().trim();
		String splitter[] = cleanWord.split("\\s+");
		Arrays.sort(splitter);
		cleanWord = Arrays.toString(splitter).replaceAll("\\p{Punct}+", "");
		if(search.containsKey(cleanWord))
		{
			return true;
		}
		if(searchType == 0)
		{
			searchResults = index.exactSearch(splitter);
		}
		else
		{
			searchResults = index.partialSearch(splitter);
		}
		if(searchResults!=null)
		{
			Collections.sort(searchResults);
			search.put(cleanWord, searchResults);
		}
		return true;
	}
	
	/**
	 * @param output
	 * 					Output of search words linked to their search results in a clean 
	 * 					json format
	 * @return 
	 * @throws IOException
	 */
	public synchronized void writeJSONSearch(Path output, int x) throws IOException
	{
		InvertedIndexWriter writer = new InvertedIndexWriter();
		writer.writeSearchWord(output, search, x);
	}
	
	/**
	 * Returns a string representation of the search results.
	 */
	public String toString()
	{
		return search.toString();
	}
	
	private class LineMinion implements Runnable {

		private String line;
		private int searchType;
		private InvertedIndex index;

		public LineMinion(String line, int searchType, InvertedIndex index)
		{
			this.line = line;
			this.searchType = searchType;
			this.index = index;
		}

		@Override
		public void run()
		{
			try
			{
				lock.lockReadWrite();
				searchForMatches(line, searchType, index);
				lock.unlockReadWrite();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	
	public synchronized void finish()
	{
		minions.finish();
	}
	
	public synchronized void shutdown()
	{
		finish();
		minions.shutdown();
	}
}
