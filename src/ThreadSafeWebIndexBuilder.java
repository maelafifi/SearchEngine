import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
 * TODO Create a common interface
 */

/**
 * A threadable version of the WebIndexBuilder class;
 * Class that will take in a seed URL, and from that seed URL, build a custom inverted
 * index by parsing for extra links to be added to the index. The max number of URL's
 * is 50. Those fifty, or less, links will be parsed and all the words on each of those
 * pages will be indexed.
 * @author macbookpro
 *
 */

public class ThreadSafeWebIndexBuilder
{
	private final static int MAX = 50;
	private final ThreadSafeInvertedIndex index;
	private final Set<String> linkSet;
	private final WorkQueue minions;
	
	/**
	 * Initializes index, linkSet, lock, and workQueue
	 * @param index
	 * 				index passed in from driver
	 * @param numThreads
	 * 				number of threads as determined by driver
	 */
	public ThreadSafeWebIndexBuilder(ThreadSafeInvertedIndex index, int numThreads)
	{
		this.index = index;
		linkSet = new HashSet<String>();
		minions = new WorkQueue(numThreads);
	}
	
	/**
	 * Takes in a seed URL from the driver, cleans it, and then calls the urlAdder.
	 * This method only occurs once, when it is called in the driver, and finishes
	 * once all of the urls are parsed.
	 * @param seed
	 * @throws MalformedURLException
	 */
	public void startCrawl(String seed) throws MalformedURLException
	{
		URL seedURL = new URL(seed);
		String cleanURL = seedURL.getProtocol()+"://"+seedURL.getHost()+seedURL.getFile();
		urlAdder(cleanURL);
		finish();
	}
	
	/**
	 * Method to simply add a clean url to the linkSet, and creates a new minion to execute
	 * work.
	 * @param url
	 * 			Cleaned url from method that calls this.
	 * @throws MalformedURLException
	 */
	public void urlAdder(String url) throws MalformedURLException
	{
		synchronized (linkSet)
		{
			if(!linkSet.contains(url))
			{
				linkSet.add(url);
				minions.execute(new CrawlMinion(url));
			}
		}
	}
	
	// TODO Remove, call static version in interface
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
	
	/**
	 * Class that implements the Runnable interface; allows for the defined number of minions to
	 * help build the inverted index from a URL.
	 * @author macbookpro
	 *
	 */
	private class CrawlMinion implements Runnable
	{
		private String url;
		
		/**
		 * initializes the url
		 * @param url
		 */
		public CrawlMinion(String url)
		{
			this.url = url;
		}
		
		/**
		 * Uses the url  to fetch the html, parse for new links, and then continues to fetch the 
		 * words from the orginal url and adds them to the index. 
		 */
		@Override
		public void run()
		{
			try
			{
				InvertedIndex local = new InvertedIndex();
				String html = HTTPFetcher.fetchHTML(url);
				ArrayList<String> links = LinkParser.listLinks(html);
				URL base = new URL(url);
				// TODO synchronized (linkSet) {
				for(String link : links)
				{
					if(linkSet.size() < MAX)
					{
						URL absolute = new URL(base, link);
						String cleanURL = absolute.getProtocol() +"://"+ absolute.getHost() + absolute.getFile();
						urlAdder(cleanURL);
					}
					else
					{
						break;
					}
				}
				addWordsFromURL(url, html, local);
				index.addAll(local);
			}
			catch(IOException e)
			{
				System.err.println("invalid link");
			}
		}
	}
	
	/**
	 * Helper  method, that helps a thread wait until all of the current
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
