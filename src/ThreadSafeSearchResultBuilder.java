import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Builds and stores a list search words, and their search results.
 * 
 */
public class ThreadSafeSearchResultBuilder
{
	private final TreeMap <String, ArrayList<SearchResult>> search;
	private final WorkQueue minions;
	private final ThreadSafeInvertedIndex index;
	
	/**
	 * Creates a new and empty treemap of the search results, initializes minions
	 * and the index, as well as the lock
	 */
	public ThreadSafeSearchResultBuilder(int numThreads, ThreadSafeInvertedIndex index)
	{
		search = new TreeMap <String, ArrayList<SearchResult>>();
		minions  = new WorkQueue(numThreads);
		this.index = index;
	}

	/**
	 * Takes in a file path, opens it and creates a new minion for each line in the file
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
	public void parseSearchFile(Path pathName, boolean partial) throws IOException
	{
		try(BufferedReader reader = Files.newBufferedReader(pathName, Charset.forName("UTF-8"));)
		{
			String line;
			while((line = reader.readLine()) != null)
			{
				minions.execute(new LineMinion(line, partial));
			}
			minions.finish();
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
	public void searchForMatches(String line, boolean partial, HashMap <String, ArrayList<SearchResult>> map)
	{
		ArrayList<SearchResult> searchResults = new ArrayList<>();
		String cleanWord = line.replaceAll("\\p{Punct}+", "").toLowerCase().trim();
		String splitter[] = cleanWord.split("\\s+");
		Arrays.sort(splitter);
		cleanWord = Arrays.toString(splitter).replaceAll("\\p{Punct}+", "");
		if(map.containsKey(cleanWord))
		{
			return;
		}
		if(!partial)
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
			map.put(cleanWord, searchResults);
		}
	}
	
	/**
	 * @param output
	 * 					Output of search words linked to their search results in a clean 
	 * 					json format
	 * @return 
	 * @throws IOException
	 */
	public synchronized void writeJSONSearch(Path output) throws IOException
	{
		InvertedIndexWriter.writeSearchWord(output, search);
	}
	
	/**
	 * Returns a string representation of the search results.
	 */
	public synchronized String toString()
	{
		return search.toString();
	}
	
	private void addAll(HashMap <String, ArrayList<SearchResult>> local) // TODO Can integrate into your run()
	{
		for(String word : local.keySet())
		{
			search.put(word, local.get(word)); 
		}
	}
	
	/**
	 * Class that implements the Runnable interface; allows for the defined number of minions to
	 * help build an index of the words to be search for.
	 * @author macbookpro
	 *
	 */
	private class LineMinion implements Runnable {

		private String line;
		private boolean partial;
		private HashMap <String, ArrayList<SearchResult>> local; // TODO Just an ArrayList
		
		/**
		 * initializes line, the type of search, and the index. 
		 * @param line
		 * @param searchType
		 * @param index
		 */
		public LineMinion(String line, boolean partial)
		{
			this.line = line;
			this.partial = partial;
			local = new HashMap <String, ArrayList<SearchResult>>();
		}

		/**
		 * Simply calls the searchForMatches method for each minion. 
		 */
		@Override
		public void run()
		{
			try
			{
				searchForMatches(line, partial, local);
				synchronized(search)
				{
					addAll(local);
				}
			}
			catch(Exception e)
			{
				System.err.println("Error running minion for: " +  Thread.currentThread());
			}
		}

	}
	
	/**
	 * Helper method, that helps a thread wait until all of the current
	 * work is done. This is useful for resetting the counters or shutting
	 * down the work queue.
	 */
	public void finish()
	{
		minions.finish();
	}
	
	/**
	 * Will shutdown the work queue after all the current pending work is
	 * finished. Necessary to prevent our code from running forever in the
	 * background.
	 */
	public void shutdown()
	{
		finish();
		minions.shutdown();
	}
}
