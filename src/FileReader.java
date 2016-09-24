import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReader
{
	private WordDatabase all = new WordDatabase();

	/**
	 * 
	 * Directory traversal; opens files that end with ".txt" and reads each line,
	 * then splits the lines by white space, then removes all non-alphanumeric characters, 
	 * and adds the cleaned word, normalized path of the file it was found in, and position
	 * in the file that it was found in to the database of words.
	 * 
	 * @param fileName
	 * 					file to open if .txt, traverse if directory, ignore if neither
	 * @return false
	 * 					no return value needed
	 */
	
	public boolean addWordsFromFile(Path fileName) throws IOException
	{
		try(DirectoryStream<Path> listing = Files.newDirectoryStream(fileName))
		{
			for(Path file : listing)
			{
				if(Files.isDirectory(file))
				{
					addWordsFromFile(file);
				}

				String NRPath = file.normalize().toString();
				String pathIgnoreCase = NRPath.toLowerCase();
				
				if(pathIgnoreCase.endsWith(".txt"))
				{
					try(BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));)
					{
						String line = null;
						int count = 1;
						
						while((line = reader.readLine()) != null)
						{
							String[] splitter = line.split("\\s+");
							for(String word : splitter)
							{
								String word2 = word.replaceAll("\\p{Punct}+", "");
								if(!word2.equals(""))
								{
									all.addToDatabase(word2, NRPath, count);
									count++;
								}
							}
						}
					}
				}
			}
			return true;
		}
	}
}
