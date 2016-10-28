import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;


public class SearchResultBuilder
{
	private final TreeMap <String, ArrayList<SearchResult>> search;
	
	public SearchResultBuilder()
	{
		search = new TreeMap <String, ArrayList<SearchResult>>();
	}

	public boolean parseSearchFile(Path pathName, int searchType, InvertedIndex index) throws IOException
	{
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
	
	public boolean searchForMatches(String line, int searchType, InvertedIndex index)
	{
		ArrayList<SearchResult> searcgResults = new ArrayList<>();
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
			searcgResults = index.exactSearch(splitter);
		}
		else
		{
			searcgResults = index.partialSearch(splitter);
		}
		if(searcgResults!=null)
		{
			Collections.sort(searcgResults);
			search.put(cleanWord, searcgResults);
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
