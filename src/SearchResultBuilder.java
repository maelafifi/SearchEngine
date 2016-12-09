import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * Builds  and stores a list search words, and their search results.
 * 
 */
public class SearchResultBuilder
{
	private final TreeMap <String, ArrayList<SearchResult>> search;
	private final InvertedIndex index;
	
	/**
	 * Creates a new and empty treemap of the search results
	 */
	public SearchResultBuilder(InvertedIndex index) 	{
		this.index = index;
		search = new TreeMap <String, ArrayList<SearchResult>>();
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
	public boolean parseSearchFile(Path pathName, boolean partial) throws IOException
	{
		try(BufferedReader reader = Files.newBufferedReader(pathName, Charset.forName("UTF-8"));)
		{
			String line;
			while((line = reader.readLine()) != null)
			{
				searchForMatches(line, partial);
			}
		}
		return false;
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
	public boolean searchForMatches(String line, boolean partial)
	{
		ArrayList<SearchResult> searchResults = new ArrayList<>();
		String cleanWord = line.replaceAll("\\p{Punct}+", "").toLowerCase().trim();
		String splitter[] = cleanWord.split("\\s+");
		Arrays.sort(splitter);
		cleanWord = String.join(" ", splitter);
		
		if(search.containsKey(cleanWord))
		{
			return true;
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
			search.put(cleanWord, searchResults);
		}
		
		return true;
	}
	
	/**
	 * @param output
	 * 					Output of search words linked to their search results in a clean 
	 * 					json format
	 * @throws IOException
	 */
	public void writeJSONSearch(Path output) throws IOException
	{
		InvertedIndexWriter.writeSearchWord(output, search);
	}
	
	/**
	 * Returns a string representation of the search results.
	 */
	public String toString()
	{
		return search.toString();
	}
}
