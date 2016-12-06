import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ThreadSafeInvertedIndexBuilder
{
	
	private final WorkQueue minions;
	ThreadSafeInvertedIndex index;
	private ReadWriteLock lock = new ReadWriteLock();
	
	public ThreadSafeInvertedIndexBuilder(int numThreads, ThreadSafeInvertedIndex index)
	{
		minions = new WorkQueue(numThreads);
		this.index = index;
	}

	
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
	 * Method to add words to the inverted index; For each ".txt" file in the directoryTraversal,
	 * this method will open the file, read the file line by line, split each word in the line, replace
	 * all unnecessary punctuation, and make the word all lower case. The word is then added to the 
	 * inverted index with the addToIndex() method
	 * 
	 * @param pathName
	 * 						Path of the file to be opened and read
	 * @param pathToString
	 * 						Path(converted to string) of the file to be opened and read for the purpose
	 * 						of adding the string to the inverted index 
	 * @return
	 * @throws IOException
	 */
	public static boolean addWordsToIndex(Path pathName, String pathToString, ThreadSafeInvertedIndex all) throws IOException
	{
		try(BufferedReader reader = Files.newBufferedReader(pathName, Charset.forName("UTF-8"));)
		{
			String line = null;
			int count = 1;
			
			while((line = reader.readLine()) != null)
			{
				String[] splitter = line.split("\\s+");
				for(String word : splitter)
				{
					String word2 = word.replaceAll("\\p{Punct}+", "").toLowerCase();
					if(!word2.equals(""))
					{
						all.addToIndex(word2, pathToString, count);
						count++;
					}
				}
			}
		}
		return true;
	}
	
	
	
	
	
	private class FileMinion implements Runnable
	{

		private Path fileName;
		private String NRPath;

		public FileMinion(Path fileName, String NRPath)
		{
			this.fileName = fileName;
			this.NRPath = NRPath;
		}

		@Override
		public void run()
		{
			try
			{
				ThreadSafeInvertedIndex local = new ThreadSafeInvertedIndex();
				addWordsToIndex(fileName, NRPath, local);
				lock.lockReadWrite();
				index.addAll(local);
				lock.unlockReadWrite();
			}
			catch (Exception e)
			{
				System.err.println("there's an issue");
				e.printStackTrace();
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
