import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class WebIndexBuilder
{
	final static int MAX = 50;
	InvertedIndex index;
	Queue<String> queue = new LinkedList<String>();
	Set<String> linkSet = new HashSet<String>();
	private int URLCount = 0;
	private int i = 0;
	
	public WebIndexBuilder(InvertedIndex index)
	{
		this.index = index;
	}
	
	
	public void startCrawl(String seed) throws UnknownHostException, IOException 
	{
		URL seedURL = new URL(seed);
		String cleanURL = seedURL.getProtocol()+"://"+seedURL.getHost()+seedURL.getFile();
		linkSet.add(cleanURL);
		queue.add(cleanURL);
		URLCount++;
		while (i < MAX && i < linkSet.size())
		{
			addWordsFromURLNoHTML(queue.element(), index);
			crawl();
			queue.remove();
		}
	}
	
	
	public void crawl() throws UnknownHostException, IOException
	{
		if(URLCount == MAX)
		{
			String html = HTTPFetcher.fetchHTML(queue.element());
			addWordsFromURL(queue.element(), html, index);
			i++;
		}
		else
		{
			ArrayList<String> currentPageURLs = new ArrayList<>();
			String html = HTTPFetcher.fetchHTML(queue.element());
			currentPageURLs = LinkParser.listLinks(html);
			URL base = new URL(queue.element());
			for(String url : currentPageURLs)
			{
				if(URLCount == MAX)
				{
					break;
				}
				URL absolute = new URL(base, url);
				String cleanURL = absolute.getProtocol() +"://"+ absolute.getHost() + absolute.getFile();
				
				if(!linkSet.contains(cleanURL))
				{
					linkSet.add(cleanURL);
					queue.add(cleanURL);
					URLCount++;
				}
			}
			addWordsFromURL(queue.element(), html, index);
			i++;
		}
	}
	
	public boolean addWordsFromURL(String url, String html, InvertedIndex index)
	{
		int position = 1;
			String words[];
			words = HTMLCleaner.fetchWords(html, 0);
			for(int i = 0; i < words.length; i++)
			{
				index.addToIndex(words[i], url, position);
				position++;
			}
		return true;
	}
	
	public boolean addWordsFromURLNoHTML(String url, InvertedIndex index)
	{
		int position = 1;
			String words[];
			words = HTMLCleaner.fetchWords(url, 1);
			for(int i = 0; i < words.length; i++)
			{
				index.addToIndex(words[i], url, position);
				position++;
			}
		return true;
	}
}