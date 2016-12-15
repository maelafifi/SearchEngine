
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Creates a new InvertedIndex
 * Words, the files  they were found in, and their positions within the respective file are added
 * and stored in an Inverted Index.
 * 
 * Also performs a search for exact words, or partial words, within the inverted index.
 */

public class ThreadSafeInvertedIndex extends InvertedIndex
{
	private final ReadWriteLock lock;
	
	/**
	 * Creates a new and empty inverted index.
	 */
	public ThreadSafeInvertedIndex()
	{
		super();
		lock = new ReadWriteLock();
	}
	
	@Override
	public void addToIndex(String word, String stringPath, int position)
	{
		lock.lockReadWrite();
		try
		{
			super.addToIndex(word, stringPath, position);
		}
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	@Override
	public boolean contains(String word)
	{
		lock.lockReadOnly();
		try
		{
			return super.contains(word);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public int indexSize()
	{
		lock.lockReadOnly();
		try
		{
			return super.indexSize();
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public int wordOccurence(String word)
	{
		lock.lockReadOnly();
		try
		{
			return super.wordOccurence(word);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public int firstOccurence(String word, String file)
	{
		lock.lockReadOnly();
		try
		{
			return super.firstOccurence(word, file);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public ArrayList<SearchResult> exactSearch(String searchWords[])
	{
		lock.lockReadOnly();
		try
		{
			return super.exactSearch(searchWords);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public ArrayList<SearchResult> partialSearch(String searchWords[])
	{
		lock.lockReadOnly();
		try
		{
			return super.partialSearch(searchWords);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public String toString()
	{
		lock.lockReadOnly();
		try
		{
			return super.toString();
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
	
	@Override
	public void addAll(InvertedIndex local)
	{
		lock.lockReadWrite();
		try
		{
			super.addAll(local);
		}
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	@Override
	public void writeJSON(Path output) throws IOException
	{
		lock.lockReadOnly();
		try
		{
			super.writeJSON(output);
		}
		finally
		{
			lock.unlockReadOnly();
		}
	}
}