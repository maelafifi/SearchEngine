import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Threadsafe Index builder that allows a user traverse a directory with multiple threads.
 * Each thread will handle it's own file, and send the words in the file to be parsed, 
 * cleaned, and organized, added to a local inverted index and from there, added to the 
 * overall inverted index which is surrounded by a readWriteLock.
 * @author macbookpro
 *
 */

public class ThreadSafeInvertedIndexBuilder
{
	
	private final WorkQueue minions;
	private final ThreadSafeInvertedIndex index;
	/**
	 * Initializes ThreadSafe InvertedIndex and the number of threads for the workQueue to use
	 * @param numThreads
	 * 				Number of threads
	 * @param index
	 * 				InvertedIndex to store words to their respective paths and positions
	 */
	public ThreadSafeInvertedIndexBuilder(int numThreads, ThreadSafeInvertedIndex index)
	{
		minions = new WorkQueue(numThreads);
		this.index = index;
	}

	/**
	 * Method to traverse a directory or open a file ending in ".txt"
	 * If the file name is a directory, it is recursively called to traverse the directory.
	 * For all ".txt" files within the directory traversal, a new FileMinion is created.
	 * 
	 * @param fileName
	 * 						Name of file to be opened or added
	 * @throws IOException
	 */
	public void directoryTraversal(Path fileName) throws IOException
	{
		if(!Files.isDirectory(fileName))
		{
			String NRPath = fileName.normalize().toString();
			String pathIgnoreCase = NRPath.toLowerCase();
			if(pathIgnoreCase.endsWith(".txt"))
			{
				minions.execute(new FileMinion(fileName, NRPath));
			}
			
		}
		
		try(DirectoryStream<Path> listing = Files.newDirectoryStream(fileName))
		{
			for(Path file : listing)
			{
				if(Files.isDirectory(file))
				{
					directoryTraversal(file);
				}

				String NRPath = file.normalize().toString();
				String pathIgnoreCase = NRPath.toLowerCase();
				
				if(pathIgnoreCase.endsWith(".txt"))
				{
					minions.execute(new FileMinion(file, NRPath));
				}
			}
		}
	}
	
	/**
	 * Class that implements the Runnable interface; allows for the defined number of minions to
	 * help build the inverted index.
	 * @author macbookpro
	 *
	 */
	private class FileMinion implements Runnable
	{

		private Path fileName;
		private String NRPath;
		
		/**
		 * Initializes the path and the normalized relative path.
		 * @param fileName
		 * @param NRPath
		 */
		public FileMinion(Path fileName, String NRPath)
		{
			this.fileName = fileName;
			this.NRPath = NRPath;
		}
		
		/**
		 * Method accessible to the defined number of threads which will have a local index, and
		 * upon completion, will lock the overall index and add all of the words from the local
		 * copy
		 */
		@Override
		public void run()
		{
			try
			{
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.addWordsToIndex(fileName, NRPath, local);
				index.addAll(local);
			}
			catch (Exception e)
			{
				System.err.println("Problem adding words to index.");
			}
		}
	}
	
	/**
	 * Helper method,  that helps a thread wait until all of the current
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
