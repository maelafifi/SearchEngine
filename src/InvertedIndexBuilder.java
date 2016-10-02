/**
 * Builds the information for the Inverted Index. 
 * Takes in a path as a parameter and traverses (or opens) the path, looking for all ".txt"
 * files. The .txt files are then opened and read line by line, and the parameters for the
 * InvertedIndex are called to be added to the InvertedIndex.
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvertedIndexBuilder
{
	private  final InvertedIndex all;
	
	/**
	 * Initializes the inverted index.
	 */
	public InvertedIndexBuilder()
	{
		all = new InvertedIndex();
	}
	
	/**
	 * Method to traverse a directory or open a file ending in ".txt"
	 * If the file name is a directory, it is recursively called to traverse the directory.
	 * For all ".txt" files within the directory traversal, addWordsToFile() method is called.
	 * 
	 * @param fileName
	 * 						Name of file to be opened or added
	 * @return
	 * @throws IOException
	 */
	public boolean directoryTraversal(Path fileName) throws IOException 
	{
		if(!Files.isDirectory(fileName))
		{
			String NRPath = fileName.normalize().toString();
			String pathIgnoreCase = NRPath.toLowerCase();
			if(pathIgnoreCase.endsWith(".txt"))
			{
				addWordsToIndex(fileName, NRPath);
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
					addWordsToIndex(file, NRPath);
				}
			}
		}
		return true;
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
	public boolean addWordsToIndex(Path pathName, String pathToString) throws IOException
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
	
	/**
	 * Method to write a JSON file for the inverted index.
	 * 
	 * @param output
	 * 						Name of the output JSON file
	 * @return
	 * @throws IOException
	 */
	public boolean jsonwriter(Path output) throws IOException
	{
		all.writeJSON(output);
		return true;
	}
}
