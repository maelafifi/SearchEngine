import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;


// TODO Change to SearchResultBuilder.
public class IndexSearch
{
	private final TreeMap <String, ArrayList<SearchResult>> search;
	
	public IndexSearch()
	{
		search = new TreeMap <String, ArrayList<SearchResult>>();
	}

	// TODO Change String searchType into boolean or int.
	public boolean parseSearchFile(Path pathName, String searchType, InvertedIndex index) throws IOException
	{
		// TODO
		// search = new TreeMap <String, ArrayList<SearchResult>>();
		try(BufferedReader reader = Files.newBufferedReader(pathName, Charset.forName("UTF-8"));)
		{
			String line;
			while((line = reader.readLine()) != null)
			{
				searchForMatches(line, searchType, index);
			}
		}
		return false;
	}
	
	// TODO Change String searchType into boolean or int.
	public boolean searchForMatches(String line, String searchType, InvertedIndex index)
	{
		// TODO rename "searcher" into searchResults.
		ArrayList<SearchResult> searcher = new ArrayList<>();
		String cleanWord = line.replaceAll("\\p{Punct}+", "").toLowerCase().trim();
		String splitter[] = cleanWord.split("\\s+");
		Arrays.sort(splitter);
		cleanWord = Arrays.toString(splitter).replaceAll("\\p{Punct}+", "");
		if(search.containsKey(cleanWord))
		{
			return true;
		}
		if(searchType.equals("exact"))
		{
			searcher = index.exactSearch(splitter);
		}
		else
		{
			searcher = index.partialSearch(splitter);
		}
		if(searcher!=null)
		{
			Collections.sort(searcher);
			search.put(cleanWord, searcher);
		}
		return true;
	}
	
	public void writeJSONSearch(Path output) throws IOException
	{
		InvertedIndexWriter.writeSearchWord(output, search);
	}
	
	public String toString()
	{
		return search.toString();
	}
}
