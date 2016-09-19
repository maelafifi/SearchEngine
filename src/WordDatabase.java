import java.util.TreeMap;
import java.util.TreeSet;

public class WordDatabase
{
	private static TreeMap<String, TreeMap<String, TreeSet<Integer>>> database;
	public void addToDatabase(String word, String stringPath, int position)
	{
		if(!database.containsKey(word))
		{
			TreeMap<String, TreeSet<Integer>> file = new TreeMap<String, TreeSet<Integer>>();
			TreeSet<Integer> index = new TreeSet<Integer>();
			index.add(position);
			file.put(stringPath, index);
			database.put(word, file);
		}
		else
		{
			TreeMap<String, TreeSet<Integer>> file = database.get(word);
			if(file.containsKey(stringPath))
			{
				TreeSet<Integer> index = file.get(stringPath);
				index.add(position);
				database.put(word, file);
			}
			else
			{
				TreeSet<Integer> index = new TreeSet<Integer>();
				index.add(position);
				file.put(stringPath, index);
				database.put(word, file);
			}
		}
	}
	
	public TreeMap<String, TreeMap<String, TreeSet<Integer>>> copyForFileWriter()
	{
		TreeMap<String, TreeMap<String, TreeSet<Integer>>> copy = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		copy = database;
		return copy;
	}
}














