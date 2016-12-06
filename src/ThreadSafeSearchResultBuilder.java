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
	public void parseSearchFile(Path pathName, int searchType) throws IOException
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
	public boolean searchForMatches(String line, int searchType, InvertedIndex index, TreeMap <String, ArrayList<SearchResult>> map)
	{
		ArrayList<SearchResult> searchResults = new ArrayList<>();
		String cleanWord = line.replaceAll("\\p{Punct}+", "").toLowerCase().trim();
		String splitter[] = cleanWord.split("\\s+");
		Arrays.sort(splitter);
		cleanWord = Arrays.toString(splitter).replaceAll("\\p{Punct}+", "");
		if(map.containsKey(cleanWord))
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
			map.put(cleanWord, searchResults);
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
	public void writeJSONSearch(Path output, int counter) throws IOException
	{
		InvertedIndexWriter writer = new InvertedIndexWriter();
		writer.writeSearchWord(output, search, counter);
	}
	
	/**
	 * Returns a string representation of the search results.
	 */
	public String toString()
	{
		return search.toString();
	}
	
	public void addAll(TreeMap <String, ArrayList<SearchResult>> local)
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
		private int searchType;
		private InvertedIndex index;
		private TreeMap <String, ArrayList<SearchResult>> local;
		
		/**
		 * initializes line, the type of search, and the index. 
		 * @param line
		 * @param searchType
		 * @param index
		 */
		public LineMinion(String line, int searchType, InvertedIndex index)
		{
			this.line = line;
			this.searchType = searchType;
			this.index = index;
			local = new TreeMap <String, ArrayList<SearchResult>>();
		}

		/**
		 * Simply calls the searchForMatches method for each minion. 
		 */
		@Override
		public void run()
		{
			try
			{
				searchForMatches(line, searchType, index, local);
				addAll(local);
			}
			catch(Exception e)
			{
				e.printStackTrace();
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
