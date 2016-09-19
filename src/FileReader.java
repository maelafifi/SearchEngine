import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReader
{
	private WordDatabase all = new WordDatabase();

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
								if(word2.equals(""))
								{

								}
								else
								{
									all.addToDatabase(word2, NRPath, count);
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
