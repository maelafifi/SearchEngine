import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ThreadSafeWebIndexBuilder
{
	private final static int MAX = 50;
	private ThreadSafeInvertedIndex index;
	private Set<String> linkSet;
	private ReadWriteLock lock;
	private final WorkQueue minions;
	
	public ThreadSafeWebIndexBuilder(ThreadSafeInvertedIndex index, int numThreads)
	{
		this.index = index;
		linkSet = new HashSet<String>();
		lock = new ReadWriteLock();
		minions = new WorkQueue(numThreads);
	}
	
	public void startCrawl(String seed) throws MalformedURLException
	{
		lock.unlockReadWrite();
		URL seedURL = new URL(seed);
		String cleanURL = seedURL.getProtocol()+"://"+seedURL.getHost()+seedURL.getFile();
		urlAdder(cleanURL);
		lock.unlockReadWrite();
		
		finish();
	}
	
	public void urlAdder(String seed) throws MalformedURLException
	{
		if(!linkSet.contains(seed))
		{
			
			linkSet.add(seed);
			minions.execute(new CrawlMinion(seed));
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
	
	private class CrawlMinion implements Runnable
	{
		private String url;
		public CrawlMinion(String url)
		{
			this.url = url;
		}
		
		@Override
		public void run()
		{
			try
			{
				String html = HTTPFetcher.fetchHTML(url);
				ArrayList<String> links = LinkParser.listLinks(html);
				lock.lockReadWrite();
				URL base = new URL(url);
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
				lock.unlockReadWrite();
				addWordsFromURL(url, html, index);
			}
			catch(IOException e)
			{
				System.err.println("invalid link");
			}
		}
	}
	
	public void finish()
	{
		minions.finish();
	}

	public void shutdown()
	{
		finish();
		minions.shutdown();
	}
	
}
