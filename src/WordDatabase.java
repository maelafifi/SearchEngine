import java.util.TreeMap;
import java.util.TreeSet;

public class WordDatabase
{
	private static TreeMap<String, TreeMap<String, TreeSet<Integer>>> database;

	public WordDatabase()
	{
		database = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
	}

	public void addToDatabase(String word, String stringPath, int position)
	{
		String lower = word.toLowerCase();
		if(!database.containsKey(lower))
		{
			TreeMap<String, TreeSet<Integer>> file = new TreeMap<String, TreeSet<Integer>>();
			TreeSet<Integer> index = new TreeSet<Integer>();
			index.add(position);
			file.put(stringPath, index);
			database.put(lower, file);
		}
		else
		{
			TreeMap<String, TreeSet<Integer>> file = database.get(lower);
			if(file.containsKey(stringPath))
			{
				TreeSet<Integer> index = file.get(stringPath);
				index.add(position);
				database.put(lower, file);
			}
			else
			{
				TreeSet<Integer> index = new TreeSet<Integer>();
				index.add(position);
				file.put(stringPath, index);
				database.put(lower, file);
			}
		}
	}

	public TreeMap<String, TreeMap<String, TreeSet<Integer>>> getDatabase()
	{
		return database;
	}
}
