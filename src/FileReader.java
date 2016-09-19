import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReader
{
	private static WordDatabase all = new WordDatabase();
	public boolean addWordsFromFile(Path fileName) throws IOException
	{
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(fileName)) 
		{
			for(Path file : listing)
			{
				if(Files.isDirectory(file))
				{
					addWordsFromFile(file);
				}
				else
				{
					if(file.endsWith(".txt"))
					{
						String NRPath = file.normalize().toString();
						try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));)
						{
							String line = null;
							while((line = reader.readLine()) != null)
							{
								int count = 1;
								String[] splitter = line.split(" ");
								for(String word : splitter)
								{
									word.replaceAll("[^\\p{Alnum}]","");
									all.addToDatabase(word, NRPath, count);
									count++;
								}
							}
							
						}
					}
				}
			}
		}
		return false;
	}

}
