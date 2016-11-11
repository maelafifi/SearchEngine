import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class WebIndexBuilder
{
	final static int MAX = 50;
	
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
