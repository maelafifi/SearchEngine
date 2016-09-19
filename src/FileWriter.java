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

public class FileWriter
{
	public static final char TAB = '\t';
	public static final char END = '\n';
	
	public static String tab(int n)
	{
		char[] tabs = new char[n];
		Arrays.fill(tabs, TAB);
		return String.valueOf(tabs);
	}
	
	public static String quote(String text)
	{
		return String.format("\"%s\"", text);
	}
	
	public boolean writeToFile(Path path, TreeMap<String, TreeMap<String, TreeSet<Integer>>> write)
	{
		try(BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"));)
		{
			writer.write("{");
				int fullMapSize = write.size();
				int j = 1;
				
				for(Entry<String, TreeMap<String, TreeSet<Integer>>> entry : write.entrySet())
				{
					writer.newLine();
					writer.write(tab(1));
					writer.write(quote(entry.getKey()));
					writer.write(": {");
					TreeMap<String, TreeSet<Integer>> file = entry.getValue();
					int secondMapSize = file.size();
					int i = 1;
					
					for(Entry<String, TreeSet<Integer>> paths : file.entrySet())
					{
						writer.newLine();
						writer.write(tab(2));
						writer.write(quote(paths.getKey()));
						writer.write(": [");
						TreeSet<Integer> tree = paths.getValue();
						Iterator<Integer> iter = tree.iterator();
						
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
