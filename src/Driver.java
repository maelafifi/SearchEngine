import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Driver class for project1 and all future projects
 * Project 1:
 * 				Checks for flags "-dir" and "-index" as well as the parameter for those flags
 * 				If the flags exist, an invertedIndex is created through the InvertedIndexBuilder,
 * 				and if "-index" flag exists, a JSON file will be created, and output based on the 
 * 				flags respective value (or default value) if there is no respective value.
 * Project 2:
 * 				Checks for search flags. If the  flags exist, the proper search is performed and 
 * 				if there is a file for it to be output to, the search result is output to the file
 */

public class Driver
{
	public static void main(String[] args)
	{
		InvertedIndex index = new InvertedIndex();
		ArgumentParser parser = new ArgumentParser();
		SearchResultBuilder searcher = new SearchResultBuilder();
		String inputFile = null;
		String outputFile = null;
		String exactSearch = null;
		String partialSearch = null;
		String searchOutput = null;
		Path input = null;
		Path output = null;
		Path exactSearcher = null;
		Path partialSearcher = null;
		Path searchOutputter = null;
		
		if(args != null)
		{
			parser.parseArguments(args);
			if(parser.hasFlag("-dir"))
			{
				if(parser.getValue("-dir") != null)
				{
					inputFile = parser.getValue("-dir");
					input = Paths.get(inputFile);
				}
				else
				{
					System.out.println("No directory listed.");
				}
			}
			
			if(parser.hasFlag("-index"))
			{
				outputFile = parser.getValue("-index", "index.json");
				output = Paths.get(outputFile);
			}
			
			if(parser.hasFlag("-exact"))
			{
				if(parser.getValue("-exact")!=null)
				{
					exactSearch = parser.getValue("-exact");
					exactSearcher = Paths.get(exactSearch);		
				}
				else
				{
					System.out.println("No directory listed");
				}
			}
			
			if(parser.hasFlag("-query"))
			{
				if(parser.getValue("-query") != null)
				{
					partialSearch = parser.getValue("-query");
					partialSearcher = Paths.get(partialSearch);
				}
			}
			
			if(parser.hasFlag("-results"))
			{
				searchOutput = parser.getValue("-results", "results.json");
				searchOutputter = Paths.get(searchOutput);
			}
			
			if(input!=null)
			{
				try
				{
					InvertedIndexBuilder.directoryTraversal(input, index);
				}
				catch(IOException e)
				{
					System.out.println("Error reading file: " + input);
				}
			}
			if(output != null)
			{
				try
				{
					index.writeJSON(output);
				}
				catch(IOException e)
				{
					System.out.println("Error writing JSON to file: " + output);
				}
			}
			
			if(exactSearcher != null)
			{
				try
				{
					int exact = 0;
					searcher.parseSearchFile(exactSearcher, exact, index);
				}
				catch(IOException e)
				{
					System.out.println("Error opening query file" + exactSearcher);
				}
			}
			
			if(partialSearcher != null)
			{
				try
				{
					int partial = 1;
					searcher.parseSearchFile(partialSearcher, partial, index);
				}
				catch(IOException e)
				{
					System.out.println("Error opening query file" + partialSearcher);
				}
			}
			
			if(searchOutputter != null)
			{
				try
				{
					searcher.writeJSONSearch(searchOutputter);
				}
				catch(IOException e)
				{
					System.out.println("Error writing JSON to file: " + searchOutputter);
				}
			}
		}
		else
		{
			System.out.println("No, or not enough, arguments provided");
			return;
		}
	}

}