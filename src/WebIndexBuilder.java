import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class WebIndexBuilder
{
	final static int MAX = 50;
	
	/**
	 * Takes in a seed URL and builds a list of URL's by fetching the HTML of the seed and parsing 
	 * all the links, then repeating the process with the second URL added to the list, and repeats
	 * until the number of URL's equals the max or until there are no more links to parse
	 * 
	 * @param seed
	 * 				Starting URL to build the list of URLs for the index
	 * @param index
	 * 				Passed in index from driver to store the words, it's positions, and it's URL
	 * @return
	 * 				No return value necessary
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static boolean URLListBuilder(String seed, InvertedIndex index) throws UnknownHostException, MalformedURLException, IOException
	{
		ArrayList<String> URLList = new ArrayList<String>();
		int URLCount = 0;
		int i = 0;
		if(seed.contains("#"))
		{
			seed = seed.substring(0, seed.indexOf("#"));
		}
		URLList.add(seed);
		URLCount++;
		while(URLCount < MAX && i < URLList.size())
		{
			ArrayList<String> currentPageURLs = new ArrayList<>();
			currentPageURLs = LinkParser.listLinks(HTTPFetcher.fetchHTML(URLList.get(i)));
			URL base = new URL(URLList.get(i));
			for(String url : currentPageURLs)
			{
				String SBase;
				if(!url.contains("http"))
				{
					URL absolute = new URL(base, url);
					SBase = absolute.toString();
				}
				else
				{
					SBase = url;
				}
				if(SBase.contains("#"))
				{
					SBase = SBase.substring(0, SBase.indexOf("#"));
				}
				if(!URLList.contains(SBase))
				{
					URLList.add(SBase);
					URLCount++;
					if(URLCount == MAX)
					{
						break;
					}
				}
			}
			i++;
		}
		addWordsFromURL(URLList, index);
		return true;
	}
	
	/**
	 * Takes each URL from the list, fetches the words from the URL, and adds each word,
	 * position, and the url it was found in to the index
	 * 
	 * @param URLs
	 * 				List of all URLs to be searched for words
	 * @param index
	 * 				Index to store all the words in each URL
	 * @return
	 * 				No return value necessary
	 */
	public static boolean addWordsFromURL(ArrayList<String> URLs, InvertedIndex index)
	{
		for(String url : URLs)
		{
			int position = 1;
			String words[];
			words = HTMLCleaner.fetchWords(url);
			for(int i = 0; i < words.length; i++)
			{
				index.addToIndex(words[i], url, position);
				position++;
			}
		}
		return true;
	}
}
