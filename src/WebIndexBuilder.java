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
	private InvertedIndex index;
	private Queue<String> queue;
	private Set<String> linkSet;
	private int urlCount;
	private int crawlCount;
	
	public WebIndexBuilder(InvertedIndex index)
	{
		this.index = index;
		queue = new LinkedList<String>();
		linkSet = new HashSet<String>();
		urlCount = 0;
		crawlCount = 0;
	}
	
	public void startCrawl(String seed) throws UnknownHostException, IOException 
	{
		URL seedURL = new URL(seed);
		String cleanURL = seedURL.getProtocol()+"://"+seedURL.getHost()+seedURL.getFile();
		linkSet.add(cleanURL);
		queue.add(cleanURL);
		urlCount++;
		while (crawlCount < linkSet.size())
		{
			crawl();
			queue.remove();
		}
	}
	
	
	public void crawl() throws UnknownHostException, IOException
	{
		if(crawlCount < MAX)
		{
			String html = HTTPFetcher.fetchHTML(queue.element());
			if(urlCount < MAX)
			{
				ArrayList<String> currentPageURLs = new ArrayList<>();
				currentPageURLs = LinkParser.listLinks(html);
				URL base = new URL(queue.element());
				for(String url : currentPageURLs)
				{
					if(urlCount == MAX)
					{
						break;
					}
					URL absolute = new URL(base, url);
					String cleanURL = absolute.getProtocol() +"://"+ absolute.getHost() + absolute.getFile();
					if(!linkSet.contains(cleanURL))
					{
						linkSet.add(cleanURL);
						queue.add(cleanURL);
						urlCount++;
					}
				}
			}
			addWordsFromURL(queue.element(), html, index);
		}
		crawlCount++;
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
}