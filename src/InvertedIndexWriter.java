/**
 * Write the InvertedIndex to a .json file. Includes a method to write words, a method to write
 * paths associated with a word, and a method to write the position of a word, associated to the
 * path of that word. 
 * 
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class InvertedIndexWriter
{
	public static final char TAB = '\t';
	public static final char END = '\n';

	/**
	 * Simple method for returning tabs 
	 * 
	 * @param n
	 * 			number of tabs to out put
	 * @return number of tabs 
	 */
	public static String tab(int n)
	{
		char[] tabs = new char[n];
		Arrays.fill(tabs, TAB);
		return String.valueOf(tabs);
	}
	
	/**
	 * Simple method for returning a string surrounded by quotes
	 * 
	 * @param text
	 * @return text surrounded by quotation marks
	 */
	public static String quote(String text)
	{
		return String.format("\"%s\"", text);
	}
	
	/**
	 * Method to start writing the JSON file; this method will write the opening and closing brace
	 * and also write each word in the map. It will, for each word, call the writePaths() method.
	 * 
	 * @param path
	 * 				path to write file to
	 * @param map
	 * 				map containing all the data to be written to JSON file
	 * @return
	 * @throws IOException
	 */
	public static boolean createFile(Path path, TreeMap<String, TreeMap<String, TreeSet<Integer>>> map) throws IOException
	{
		try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"));)
		{
			writer.write("{");
			writeWords(writer, map);
			writer.newLine();
			writer.write("}");
		}

		catch(IOException e)
		{
			System.out.println("Problem writing JSON to file: " + path);
			return false;
		}
		return false;
	}
	
	public static boolean writeWords(BufferedWriter writer, TreeMap<String, TreeMap<String, TreeSet<Integer>>> map) throws IOException
	{
		int fullMapSize = map.size();
		int j = 1;
		for(Entry<String, TreeMap<String, TreeSet<Integer>>> entry : map.entrySet())
		{
			writer.newLine();
			writer.write(tab(1));
			writer.write(quote(entry.getKey()));
			writer.write(": {");
			TreeMap<String, TreeSet<Integer>> file = entry.getValue();
			writePaths(writer, file);
			writer.newLine();
			writer.write(tab(1));
			writer.write("}");
			if(j < fullMapSize)
			{
				writer.write(",");
				j++;
			}
		}
		return false;
	}
	
	/**
	 * This method will write each path that the associated word is found within; it will also call, for each word-path
	 * pairing, the writePositions() method
	 * 
	 * @param writer
	 * 					Already opened BufferedWriter from the writeWords() method
	 * @param map
	 * 					Data structure containing all of the path-to-position information
	 * @return
	 * @throws IOException
	 */
	public static boolean writePaths(BufferedWriter writer, TreeMap<String, TreeSet<Integer>> map) throws IOException
	{
		int secondMapSize = map.size();
		int i = 1;
		for(Entry<String, TreeSet<Integer>> paths : map.entrySet())
		{
			writer.newLine();
			writer.write(tab(2));
			writer.write(quote(paths.getKey()));
			writer.write(": [");
			TreeSet<Integer> tree = paths.getValue();
			writePositions(writer, tree);
			writer.newLine();
			writer.write(tab(2));
			writer.write("]");

			if(i < secondMapSize)
			{
				writer.write(",");
				i++;
			}
		}
		return true;
	}
	
	/**
	 * This method will write each position that a word is found in a particular path to the JSON file.
	 * 
	 * @param writer
	 * 					Already opened BufferedWriter from the writeWords() method
	 * @param set
	 * 					Data structure containing all the positions a particular word was found within a particular file
	 * @return
	 * @throws IOException
	 */
	public static boolean writePositions(BufferedWriter writer, TreeSet<Integer> set) throws IOException
	{
		writer.newLine();
		Iterator<Integer> tree = set.iterator();
		if(!set.isEmpty())
		{
			while(tree.hasNext())
			{
				writer.write(tab(3));
				String key = tree.next().toString();
				writer.write(key);
				if(tree.hasNext())
				{
					writer.write(",");
					writer.newLine();
				}
			}
		}
		return true;
	}
}
