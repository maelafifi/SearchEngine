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
	private final static int MAX = 50;
	// TODO Did I tell you that those should be private?
	InvertedIndex index;
	Queue<String> queue;
	Set<String> linkSet;
	// TODO The constants should also be initialized in the constructor.
	// TODO And I think I tell you that the name of class members and variables should be private.
	private int URLCount = 0;
	// TODO Rename "i" to "crawledCount".s
	private int i = 0;
	
	public WebIndexBuilder(InvertedIndex index)
	{
		this.index = index;
		queue = new LinkedList<String>();
		linkSet = new HashSet<String>();
	}
	
	public void startCrawl(String seed) throws UnknownHostException, IOException 
	{
		URL seedURL = new URL(seed);
		String cleanURL = seedURL.getProtocol()+"://"+seedURL.getHost()+seedURL.getFile();
		linkSet.add(cleanURL);
		queue.add(cleanURL);
		URLCount++;
		while (i < linkSet.size())
		{
			crawl();
			queue.remove();
		}
	}
	
	
	public void crawl() throws UnknownHostException, IOException
	{
		if(i < MAX)
		{
			String html = HTTPFetcher.fetchHTML(queue.element());
			if(URLCount < MAX)
			{
				ArrayList<String> currentPageURLs = new ArrayList<>();
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
			}
			addWordsFromURL(queue.element(), html, index);
		}
		i++;
	}
	
	public boolean addWordsFromURL(String url, String html, InvertedIndex index)
	{
		int position = 1;
		// TODO Check your indentation.
			String words[];
			words = HTMLCleaner.fetchWords(html, 0);
			for(int i = 0; i < words.length; i++)
			{
				index.addToIndex(words[i], url, position);
				position++;
			}
		return true;
	}
}