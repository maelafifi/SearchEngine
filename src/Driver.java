import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
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
	private static final int exact = 0;
	private static final int partial = 1;
	
	public static void main(String[] args)
	{
		InvertedIndex index = new InvertedIndex();
		ThreadSafeInvertedIndex tsIndex = new ThreadSafeInvertedIndex();
		ArgumentParser parser = new ArgumentParser();
		SearchResultBuilder searcher = new SearchResultBuilder();
		String inputFile = null;
		String outputFile = null;
		String exactSearch = null;
		String partialSearch = null;
		String searchOutput = null;
		String urlSeed = null;
		int threads = 0;
		int x = 0;
		Path input = null;
		Path output = null;
		Path exactSearchQueryPath = null;
		Path partialSearchQueryPath = null;
		Path searchOutputPath = null;
		
		if(args != null)
		{
			parser.parseArguments(args);
			if(parser.hasFlag("-multi"))
			{
				String thread = parser.getValue("-multi", "5");
				try
				{
					threads = Integer.parseInt(thread);
				}
				catch(NumberFormatException e)
				{
					System.err.println("invalid input");
				}
				if(threads <= 0)
				{
					threads = 5;
				}
			}
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
					exactSearchQueryPath = Paths.get(exactSearch);		
				}
				else
				{
					System.out.println("No directory listed");
				}
			}
			if(parser.hasFlag("-url"))
			{
				if(parser.getValue("-url")!=null)
				{
					urlSeed = parser.getValue("-url");
				}
				else
				{
					System.out.println("No seed URL given.");
				}
			}
			
			if(parser.hasFlag("-query"))
			{
				if(parser.getValue("-query") != null)
				{
					partialSearch = parser.getValue("-query");
					partialSearchQueryPath = Paths.get(partialSearch);
				}
			}
			
			if(parser.hasFlag("-results"))
			{
				searchOutput = parser.getValue("-results", "results.json");
				searchOutputPath = Paths.get(searchOutput);
			}
			
			
			if(threads != 0)
			{
				ThreadSafeSearchResultBuilder tsSearcher = new ThreadSafeSearchResultBuilder(threads, tsIndex);
				if(input != null)
				{
					try
					{
						ThreadSafeInvertedIndexBuilder tsIndexBuilder = new ThreadSafeInvertedIndexBuilder(threads, tsIndex);
						tsIndexBuilder.directoryTraversal(input);
						tsIndexBuilder.shutdown();
					}
					catch(IOException e)
					{
						System.out.println("Error reading file: " + input);
					}
				}
				
				if(urlSeed != null)
				{
					try
					{
						ThreadSafeWebIndexBuilder webBuilder = new ThreadSafeWebIndexBuilder(tsIndex, threads);
						webBuilder.startCrawl(urlSeed);
					}
					catch(MalformedURLException e)
					{
						e.printStackTrace();
					}
				}
				
				if(output != null)
				{
					try
					{
						index = tsIndex;
						index.writeJSON(output);
					}
					catch(IOException e)
					{
						System.out.println("Error writing JSON to file: " + output);
					}
				}
				
				if(exactSearchQueryPath != null)
				{
					try
					{
						tsSearcher.parseSearchFile(exactSearchQueryPath, exact);
						tsSearcher.shutdown();
					}
					catch(IOException e)
					{
						System.out.println("Error opening query file" + exactSearchQueryPath);
					}
				}
				
				if(partialSearchQueryPath != null)
				{
					try
					{
						tsSearcher.parseSearchFile(partialSearchQueryPath, partial);
						tsSearcher.shutdown();
					}
					catch(IOException e)
					{
						System.out.println("Error opening query file" + partialSearchQueryPath);
					}
				}
				if(searchOutputPath != null)
				{
					try
					{
						tsSearcher.writeJSONSearch(searchOutputPath, x);
						x++;
					}
					catch(IOException e)
					{
						System.out.println("Error writing JSON to file: " + searchOutputPath);
					}
				}
				
				
			}
			else
			{
				if(input != null)
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
				
				if(urlSeed != null)
				{
					try
					{
						WebIndexBuilder webBuilder = new WebIndexBuilder(index);
						webBuilder.startCrawl(urlSeed);
					}
					catch(UnknownHostException e)
					{
						e.printStackTrace();
					}
					catch(MalformedURLException e)
					{
						e.printStackTrace();
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
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
			
			if(exactSearchQueryPath != null)
			{
				try
				{
					searcher.parseSearchFile(exactSearchQueryPath, exact, index);
				}
				catch(IOException e)
				{
					System.out.println("Error opening query file" + exactSearchQueryPath);
				}
			}
			
			if(partialSearchQueryPath != null)
			{
				try
				{
					searcher.parseSearchFile(partialSearchQueryPath, partial, index);
				}
				catch(IOException e)
				{
					System.out.println("Error opening query file" + partialSearchQueryPath);
				}
			}
			
			if(searchOutputPath != null)
			{
				try
				{
					searcher.writeJSONSearch(searchOutputPath, x);
				}
				catch(IOException e)
				{
					System.out.println("Error writing JSON to file: " + searchOutputPath);
				}
			}
		}
		else
		{
			System.out.println("No, or not enough, arguments provided");
		}
	}

}