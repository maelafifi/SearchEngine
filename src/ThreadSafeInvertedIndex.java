
import java.util.ArrayList;

/**
 * Creates a new InvertedIndex
 * Words, the files they were found in, and their positions within the respective file are added
 * and stored in an Inverted Index.
 * 
 * Also performs a search for exact words, or partial words, within the inverted index.
 */

public class ThreadSafeInvertedIndex extends InvertedIndex
{
	private ReadWriteLock lock;
	/**
	 * Creates a new and empty inverted index.
	 */
	public ThreadSafeInvertedIndex()
	{
		super();
		lock = new ReadWriteLock();
	}

	/**
	 * Adds a given word to the inverted index; first checks if the word exists in the index already;
	 * if not, it is added along with the path of the file that the word was found in, and the position 
	 * of the word in that file. If it exists, it then checks to see if the word was previously found in 
	 * the same file, and if it does, the position is added to the TreeSet of positions. If it does not
	 * exist in any of the previous files, the path of the file and the position is stored. 
	 * 
	 * @param word
	 * 						The word to be added to the index
	 * @param stringPath
	 * 						The path (converted to string) of the file that "word" is found
	 * @param position
	 * 						The position that the word was found within the particular file
	 */
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
	
	/**
	 * Tests whether the index contains the specified word.
	 * 
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	
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
	
	/**
	 * Returns the number of words stored in the index.
	 * 
	 * @return number of words
	 */
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
	
	/**
	 * Returns the number of times a word was found (i.e. the number of
	 * positions associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	public int wordOccurence(String word)
	{
		lock.lockReadWrite();
		try
		{
			return super.wordOccurence(word);
		}
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	public int firstOccurence(String word, String file)
	{
		lock.lockReadWrite();
		try
		{
			return super.firstOccurence(word, file);
		}
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	/**
	 * Takes in a search word, or words, and searches the index for 
	 * exact matches. If the word(s) exist in a particular file,
	 * the number of occurrences, and it's initial position for the
	 * search is updated. 
	 * 
	 * @param searchWords
	 * 				A word or array of words to be searched for
	 * @return
	 * 				An arraylist of all files linked to the number of
	 * 				occurrences and initial position of the search word
	 */
	public ArrayList<SearchResult> exactSearch(String searchWords[])
	{
		lock.lockReadWrite();
		try
		{
			return super.exactSearch(searchWords);
		}
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	/**
	 * Takes in a search word, or words, and searches the index for 
	 * partial matches. If the word(s) in the index start with the 
	 * search word the number of occurrences, and it's initial position 
	 * for the search is updated. 
	 * 
	 * @param searchWords
	 * 				A word or array of words to be searched for
	 * @return
	 * 				An arraylist of all files linked to the number of
	 * 				occurrences and initial position of the search word(s)
	 */
	public ArrayList<SearchResult> partialSearch(String searchWords[])
	{
		lock.lockReadWrite();
		try
		{
			return super.partialSearch(searchWords);
		}
		finally
		{
			lock.unlockReadWrite();
		}
	}
	
	/**
	 * Returns a string representation of this index.
	 */
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
}