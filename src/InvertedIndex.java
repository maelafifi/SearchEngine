import java.util.TreeMap;
import java.util.TreeSet;

// TODO Javadoc all classes and methods

public class InvertedIndex
{
	// TODO Great that this private, hate that it is static! Make it final!
	private static TreeMap<String, TreeMap<String, TreeSet<Integer>>> database;

	
	// Initializes the database
	public InvertedIndex()
	{
		database = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
	}

	/**
	 * Adding words to database;
	 * 
	 * If the word exists, checks to see if the path also exists; 
	 * 		if the path also exists, the position of the word is added to the database.
	 * 		if the path doesn't exist, path is added to database with position.
	 * 
	 * If the word doesn't exist, word is added to database with the path and it's position
	 * 
	 * 
	 * @param word
	 * 				the word to be added to the database
	 * @param stringPath
	 * 				The path of the word to be added
	 * @param position
	 * 				The position of the word in the file
	 * 
	 */
	public void addToDatabase(String word, String stringPath, int position)
	{
		// TODO The to lowercase should happen elsewhere
		String lower = word.toLowerCase();
		
		/* Checks to see if the word DOES NOT exist in the database
		 * 
		 * If it doesn't, the treemap for path and treeset for position of the word
		 * is initialized
		 */
		if(!database.containsKey(lower))
		{
			TreeMap<String, TreeSet<Integer>> file = new TreeMap<String, TreeSet<Integer>>();
			TreeSet<Integer> index = new TreeSet<Integer>();
			index.add(position);
			file.put(stringPath, index);
			database.put(lower, file);
		}
		else // The word DOES exist in the database
		{
			TreeMap<String, TreeSet<Integer>> file = database.get(lower); 
			
			/*
			 * Checks if there is a path related to the word
			 *  
			 * If it does, adds the position of the word to the database
			 */
			if(file.containsKey(stringPath))
			{
				TreeSet<Integer> index = file.get(stringPath);
				index.add(position);
				database.put(lower, file);
			}
			
			/*
			 * The path doesn't exist; 
			 * 
			 * Adds path and position to database
			 */
			else
			{
				TreeSet<Integer> index = new TreeSet<Integer>();
				index.add(position);
				file.put(stringPath, index);
				database.put(lower, file);
			}
		}
	}
	
	/* TODO Add writeJSON()
	public void writeJSON(Path output) {
		writeFile.writeToFile(output, database);
	}
	*/

	// TODO Remove this! Breaks encapsulation
	// standard getter to return the contents of the database
	public TreeMap<String, TreeMap<String, TreeSet<Integer>>> getDatabase()
	{
		return database;
	}
	
	// TODO Add size(), containsWord(), etc.
	
	
	public String toString() {
		return database.toString();
	}
	
}
