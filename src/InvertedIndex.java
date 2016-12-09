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
	 * Adds  a given word to the inverted index; first checks if the word exists in the index already;
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
	
	/**
	 * Takes in a word and the file to grab the first position from
	 * @param word
	 * 			the word to search for in the specified file
	 * @param file
	 * 			the file to search for the word
	 * @return
	 * 			the first position of a word in a particular file
	 */
	public int firstOccurence(String word, String file)
	{
		if(index.containsKey(word))
		{
			if(index.get(word).containsKey(file))
			{
				return index.get(word).get(file).first();
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
		
		TreeMap<String, SearchResult> result = new TreeMap<>();
		
		for(String searchWord : searchWords)
		{
			if(index.containsKey(searchWord))
			{
				searchHelper(searchWord, result);
			}
		}
		
		ArrayList<SearchResult> results = new ArrayList<>(result.values());
		
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
		
		TreeMap<String, SearchResult> result = new TreeMap<>();
		for(String searchWord : searchWords)
		{
			for(String words : index.tailMap(searchWord, true).keySet())
			{
				if(words.startsWith(searchWord))
				{
					searchHelper(words, result);
				}
				else
				{
					break;
				}
			}
		}
		
		ArrayList<SearchResult> results = new ArrayList<>(result.values());
		
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
	
	public void searchHelper(String searchWord, TreeMap<String, SearchResult> result)
	{
		for(String path : index.get(searchWord).keySet())
		{
			String location = path;
			int frequency = index.get(searchWord).get(path).size();
			int firstOccurrence = index.get(searchWord).get(location).first();
			if(result.containsKey(location))
			{
				result.get(location).updateFirstOccurrence(firstOccurrence);
				result.get(location).updateFrequency(frequency);
			}
			else
			{
				 SearchResult r = new SearchResult(frequency, firstOccurrence, location);
				 result.put(location, r);
			}
		}
	}
	/**
	 * Method to add everything from one inverted index into another
	 * @param local
	 * 				the index to be added
	 */
	public void addAll(InvertedIndex local)
	{
		for(String word : local.index.keySet())
		{
			if(!this.index.containsKey(word))
			{
				this.index.put(word, local.index.get(word));
			}
			else
			{
				for (String path : local.index.get(word).keySet())
				{
					if(index.get(word).containsKey(path)==false)
					{
						this.index.get(word).put(path, local.index.get(word).get(path));
					}
					else
					{
						this.index.get(word).get(path).addAll(local.index.get(word).get(path));
					}
				}
			}
		}
	}
}