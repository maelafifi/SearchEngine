import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Creates a new InvertedIndex
 * Words, the files they were found in, and their positions within the respective file are added
 * and stored in an Inverted Index.
 * 
 * Also performs a search for exact words, or partial words, within the inverted index.
 */

public class InvertedIndex
{
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	
	/**
	 * Creates a new and empty inverted index.
	 */
	public InvertedIndex()
	{
		index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
	}

	/**
	 * Adds a given word to the inverted index; first checks if the word exists in the index already;
	 * if not, it is added along with the path of the file that the word was found in, and the position 
	 * of the word in that file. If it exists, it then checks to see if the word was previously found in 
	 * the same file, and if it does, the position is added to the TreeSet of positions. If it does not
	 * exist in any of the previous files, the path of the file and the position is stored. 
	 * 
	 * @param word
	 * 						The word to be added to the index
	 * @param stringPath
	 * 						The path (converted to string) of the file that "word" is found
	 * @param position
	 * 						The position that the word was found within the particular file
	 */
	public void addToIndex(String word, String stringPath, int position)
	{
		if(!index.containsKey(word))
		{
			TreeMap<String, TreeSet<Integer>> file = new TreeMap<String, TreeSet<Integer>>();
			TreeSet<Integer> positions = new TreeSet<Integer>();
			positions.add(position);
			file.put(stringPath, positions);
			index.put(word, file);
		}
		else
		{
			TreeMap<String, TreeSet<Integer>> file = index.get(word);
			
			if(file.containsKey(stringPath))
			{
				TreeSet<Integer> positions = file.get(stringPath);
				positions.add(position);
				index.put(word, file);
			}
			else
			{
				TreeSet<Integer> positions = new TreeSet<Integer>();
				positions.add(position);
				file.put(stringPath, positions);
				index.put(word, file);
			}
		}
	}
	
	/**
	 * Method call to JSONWriter to write inverted index to a file.
	 * 
	 * @param output
	 * 						The path for the JSON file to be written to.
	 * @throws IOException
	 */
	public void writeJSON(Path output) throws IOException
	{
		InvertedIndexWriter.writeWords(output, index);
	}
	
	/**
	 * Tests whether the index contains the specified word.
	 * 
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	
	public boolean contains(String word)
	{
		return index.containsKey(word);
	}
	
	/**
	 * Returns the number of words stored in the index.
	 * 
	 * @return number of words
	 */
	public int indexSize()
	{
		return index.size();
	}
	
	/**
	 * Returns the number of times a word was found (i.e. the number of
	 * positions associated with a word in the index).
	 *
	 * @param word
	 *            word to look for
	 * @return number of times the word was found
	 */
	public int wordOccurence(String word)
	{
		int occurence = 0;
		if(index.containsKey(word))
		{
			occurence = index.get(word).size();
		}
		return occurence;
	}
	
	public int firstOccurence(String word, String file)
	{
		int firstOccurrence = 0;
		TreeMap<String, TreeSet<Integer>> paths = new TreeMap<String, TreeSet<Integer>>();
		TreeSet<Integer> positions = new TreeSet<Integer>();
		if(index.containsKey(word))
		{
			paths = index.get(word);
			if(paths.containsKey(file))
			{
				positions = paths.get(file);
				firstOccurrence = positions.first();
				return firstOccurrence;
			}
		}
		return 0;
	}
	
	/**
	 * Takes in a search word, or words, and searches the index for 
	 * exact matches. If the word(s) exist in a particular file,
	 * the number of occurrences, and it's initial position for the
	 * search is updated. 
	 * 
	 * @param searchWords
	 * 				A word or array of words to be searched for
	 * @return
	 * 				An arraylist of all files linked to the number of
	 * 				occurrences and initial position of the search word
	 */
	public ArrayList<SearchResult> exactSearch(String searchWords[])
	{
		if(index.isEmpty())
		{
			return null;
		}
		ArrayList<SearchResult> results = new ArrayList<>();
		TreeMap<String, SearchResult> result = new TreeMap<>();
		String location;
		int frequency;
		int firstOccurrence;
		for(String searchWord : searchWords)
		{
			frequency = 0;
			if(index.containsKey(searchWord))
			{
				TreeMap<String, TreeSet<Integer>> paths = index.get(searchWord);
				for(String path : paths.keySet())
				{
					location = path;
					frequency = paths.get(location).size();
					firstOccurrence = index.get(searchWord).get(location).first();
					if(result.containsKey(location))
					{
						result.get(location).updateFirstOccurrence(firstOccurrence);
						result.get(location).updateFrequency(frequency);
					}
					else
					{
						result.put(location, new SearchResult(frequency, firstOccurrence, location));
					}
				}
			}
		}
		for(String path : result.keySet())
		{
			results.add(result.get(path));
		}
		Collections.sort(results);
		return results;
	}
	
	/**
	 * Takes in a search word, or words, and searches the index for 
	 * partial matches. If the word(s) in the index start with the 
	 * search word the number of occurrences, and it's initial position 
	 * for the search is updated. 
	 * 
	 * @param searchWords
	 * 				A word or array of words to be searched for
	 * @return
	 * 				An arraylist of all files linked to the number of
	 * 				occurrences and initial position of the search word(s)
	 */
	public ArrayList<SearchResult> partialSearch(String searchWords[])
	{
	if(index.isEmpty())
	{
		return null;
	}
	ArrayList<SearchResult> results = new ArrayList<>();
	TreeMap<String, SearchResult> result = new TreeMap<>();
	String location;
	int frequency;
	int firstOccurrence;
	for(String searchWord : searchWords)
	{
		frequency = 0;
		for(String words : index.tailMap(searchWord, true).keySet())
		{
			if(words.startsWith(searchWord))
			{
				TreeMap<String, TreeSet<Integer>> paths = index.get(words);
				for(String path : paths.keySet())
				{
					location = path;
					frequency = paths.get(location).size();
					firstOccurrence = index.get(words).get(location).first();
					if(!result.containsKey(location))
					{
						result.put(location, new SearchResult(frequency, firstOccurrence, location));
					}
					else
					{
						result.get(location).updateFirstOccurrence(firstOccurrence);
						result.get(location).updateFrequency(frequency);
					}
				}
			}
			else
			{
				break;
			}
		}
	}
	for(String path : result.keySet())
	{
		results.add(result.get(path));
	}
	Collections.sort(results);
	return results;
}
	/**
	 * Returns a string representation of this index.
	 */
	public String toString()
	{
		return index.toString();
	}
	
}