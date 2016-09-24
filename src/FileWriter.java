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

public class FileWriter // TODO More specific class name
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
	 * Takes text and returns the text surrounded by quotes
	 * 
	 * @param text
	 * @return text in quotes
	 */
	public static String quote(String text)
	{
		return String.format("\"%s\"", text);
	}

	// TODO Consider adding back in the ability to write other JSON objects
	
	/**
	 * Takes a database of words (write) and a filename (path) and writes all the words to the 
	 * output filename
	 * 
	 * @param path
	 * 				path of file to write the database to
	 * @param write
	 * 				database of words and their location to write to file
	 * @return 
	 */
	// TODO throw exception
	public boolean writeToFile(Path path, TreeMap<String, TreeMap<String, TreeSet<Integer>>> write) // TODO Rename "write" to something else "map"
	{
		try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"));)
		{
			writer.write("{");
			int fullMapSize = write.size();
			int j = 1;
			
			// for every word in the database
			for(Entry<String, TreeMap<String, TreeSet<Integer>>> entry : write.entrySet())
			{
				writer.newLine();
				writer.write(tab(1));
				writer.write(quote(entry.getKey()));
				writer.write(": {");
				TreeMap<String, TreeSet<Integer>> file = entry.getValue();
				int secondMapSize = file.size();
				int i = 1;

				// for every path in the database
				for(Entry<String, TreeSet<Integer>> paths : file.entrySet())
				{
					writer.newLine();
					writer.write(tab(2));
					writer.write(quote(paths.getKey()));
					writer.write(": [");
					TreeSet<Integer> tree = paths.getValue();
					Iterator<Integer> iter = tree.iterator();
					
					// for every position in the database
					while(iter.hasNext())
					{
						writer.newLine();
						writer.write(tab(3));
						writer.write(iter.next().toString());
						if(iter.hasNext())
						{
							writer.write(",");
						}
					}
					writer.newLine();
					writer.write(tab(2));
					writer.write("]");

					if(i < secondMapSize)
					{
						writer.write(",");
						i++;
					}
				}

				writer.newLine();
				writer.write(tab(1));
				writer.write("}");

				if(j < fullMapSize)
				{
					writer.write(",");
					j++;
				}
			}
			writer.newLine();
			writer.write("}");
		}

		catch(IOException e)
		{
			// TODO "Problem writing JSON to file" + path + "."
			System.out.println("Problem reading or writing file");
			return false;
		}
		return false;
	}
}
