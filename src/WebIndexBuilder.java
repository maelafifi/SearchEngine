import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Class  that will take in a seed URL, and from that seed URL, build a custom inverted
 * index by parsing for extra links to be added to the index. The max number of URL's
 * is 50. Those fifty, or less, links will be parsed and all the words on each of those
 * pages will be indexed.
 * @author macbookpro
 *
 */

public class WebIndexBuilder
{
	private final static int MAX = 50;
	private final InvertedIndex index;
	private final Queue<String> queue;
	private final Set<String> linkSet;
	private int urlCount;
	private int crawlCount;
	
	/**
	 * Initalizes Index, queue, linkSet, urlCount and crawlCount
	 * @param index
	 * 				Index passed in from the driver. 
	 */
	public WebIndexBuilder(InvertedIndex index)
	{
		this.index = index;
		queue = new LinkedList<String>();
		linkSet = new HashSet<String>();
		urlCount = 0;
		crawlCount = 0;
	}
	
	/**
	 * Takes in a seed URL, adds the seed to the queue and linkSet, then calls on the
	 * crawl() method to continue work on the web page. Checks to make sure that the
	 * crawl count is always smaller than the linkSet size, meaning there is more work
	 * to be done.
	 * 
	 * @param seed
	 * 				seed URL to initiate webCrawl as provided by main
	 * @throws UnknownHostException
	 * @throws IOException
	 */
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
	
	/**
	 * Method to open open a connection to the web, grabs all of the html from the particular
	 * web page(first element in the queue), and parses for links. Once this is complete, the
	 * method then calls the addWordsFromUrl. Increments crawlCount variable to make sure that
	 * we never go over the max number of urls
	 * @throws UnknownHostException
	 * @throws IOException
	 */
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
	
	// TODO public static and move into an interface
	/**
	 * Method to add words to the index. Takes in the URL and the HTML that was already cleaned 
	 * and fetched, respectively, and fetches all of the words and adds them to the index.
	 * @param url
	 * 			cleaned url to be added to the index on a per word basis
	 * @param html
	 * 			already fetched html from the method that calls this function
	 * @param index
	 * 			index for words to be added to
	 * @return
	 */
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
	
	// TODO Create a toAbsolute static helper method (put in interfaces)
	/*
	public static URL toAbsolute(URL base, String current)
	public static URL cleanURL(URL url) {
		return new URL(url.getProtocol(), url.getHost(), url.getFile(), null);
	}
	*/
}