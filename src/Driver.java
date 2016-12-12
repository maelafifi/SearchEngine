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
 * Project 3:
 * 				Checks for url flag. If the flag exists, the url is sent to be parsed for links,
 * 				a new index for the words in those links is created, and all previous functionality
 * 				remains usable.
 * Project 4:
 * 				Added thread safe classes, methods, and helpers to increase efficiency and usability.
 * 				Checks for multi flag, and if it exists, web and directory indexing, as well as search
 * 				functionalities will now be utilized in a thread safe manner.
 */

public class Driver
{	
	public static void main(String[] args)
	{		
		InvertedIndex index = new InvertedIndex();
		SearchResultBuilder searcher = new SearchResultBuilder(index);
		ArgumentParser parser = new ArgumentParser();
		String inputFile = null;
		String outputFile = null;
		String exactSearch = null;
		String partialSearch = null;
		String searchOutput = null;
		String urlSeed = null;
		int threads = 0;
		boolean partial;
		Path input = null;
		Path output = null;
		Path exactSearchQueryPath = null;
		Path partialSearchQueryPath = null;
		Path searchOutputPath = null;
		
		if(args != null)
		{
			parser.parseArguments(args);
			
			if(parser.hasFlag("-multi"))/* start thread flag */
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
			}/*end thread flag*/
			
			if(parser.hasFlag("-dir"))/* start directory flag */
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
			} /* end directory flag*/
			
			if(parser.hasFlag("-index")) /* start index flag */
			{
				outputFile = parser.getValue("-index", "index.json");
				output = Paths.get(outputFile);
			} /* end index flag*/
			
			if(parser.hasFlag("-exact")) /* start exact search flag */
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
			} /* end exact search flag*/
			
			if(parser.hasFlag("-url")) /* start url flag */
			{
				if(parser.getValue("-url")!=null)
				{
					urlSeed = parser.getValue("-url");
				}
				else
				{
					System.out.println("No seed URL given.");
				}
			} /* end url flag */
			
			if(parser.hasFlag("-query")) /* start query flag */
			{
				if(parser.getValue("-query") != null)
				{
					partialSearch = parser.getValue("-query");
					partialSearchQueryPath = Paths.get(partialSearch);
				}
			} /* end query flag */
			
			if(parser.hasFlag("-results")) /* start results flag */
			{
				searchOutput = parser.getValue("-results", "results.json");
				searchOutputPath = Paths.get(searchOutput);
			} /* end results flag */
			
			if(threads != 0) /** Start of multithreaded indexing and search if threads are provided */
			{
				ThreadSafeInvertedIndex tsIndex = new ThreadSafeInvertedIndex();
				ThreadSafeSearchResultBuilder tsSearcher = new ThreadSafeSearchResultBuilder(threads, tsIndex);
				ThreadSafeInvertedIndexBuilder tsIndexBuilder = new ThreadSafeInvertedIndexBuilder(threads, tsIndex);
				if(input != null)
				{
					try
					{
						tsIndexBuilder.directoryTraversal(input);
						tsIndexBuilder.shutdown();
					}
					catch(IOException e)
					{
						System.err.println("Error reading file: " + input.toString());
					}
				}
				
				if(urlSeed != null)
				{
					try
					{
						ThreadSafeWebIndexBuilder webBuilder = new ThreadSafeWebIndexBuilder(tsIndex, threads);
						webBuilder.startCrawl(urlSeed);
						webBuilder.shutdown();
					}
					catch(MalformedURLException e)
					{
						System.err.println("Incorrect URL Format.");
					}
				}
				
				if(output != null)
				{
					try
					{
						tsIndex.writeJSON(output);
					}
					catch(IOException e)
					{
						System.out.println("Error writing JSON to file: " + output.toString());
					}
				}
				
				if(exactSearchQueryPath != null)
				{
					try
					{
						
						partial = false;
						tsSearcher.parseSearchFile(exactSearchQueryPath, partial);
						tsSearcher.shutdown();
					}
					catch(IOException e)
					{
						System.err.println("Error opening query file: " + exactSearchQueryPath.toString());
					}
				}
				
				if(partialSearchQueryPath != null)
				{
					try
					{
						partial = true;
						tsSearcher.parseSearchFile(partialSearchQueryPath, partial);
						tsSearcher.shutdown();
					}
					catch(IOException e)
					{
						System.out.println("Error opening query file: " + partialSearchQueryPath.toString());
					}
				}
				if(searchOutputPath != null)
				{
					try
					{
						tsSearcher.writeJSONSearch(searchOutputPath);
					}
					catch(IOException e)
					{
						System.err.println("Error writing JSON to file: " + searchOutputPath.toString());
					}
				}
			}/** end of multithreaded indexing and search if threads are provided */
			else /** start of non-multihreaded functionality */
			{
				if(input != null)
				{
					try
					{
						InvertedIndexBuilder.directoryTraversal(input, index);
					}
					catch(IOException e)
					{
						System.err.println("Error reading file: " + input);
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
						System.err.println("Error with URL; No viable host found");
					}
					catch(MalformedURLException e)
					{
						System.err.println("Incorrect URL Format.");
					}
					catch(IOException e)
					{
						System.err.println("Error parsing URL: " + urlSeed);
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
						System.err.println("Error writing JSON to file: " + output.toString());
					}
				}
				
				if(exactSearchQueryPath != null)
				{
					try
					{
						partial = false;
						searcher.parseSearchFile(exactSearchQueryPath, partial);
					}
					catch(IOException e)
					{
						System.err.println("Error opening query file: " + exactSearchQueryPath.toString());
					}
				}
				
				if(partialSearchQueryPath != null)
				{
					try
					{
						partial = true;
						searcher.parseSearchFile(partialSearchQueryPath, partial);
					}
					catch(IOException e)
					{
						System.err.println("Error opening query file: " + partialSearchQueryPath.toString());
					}
				}
				
				if(searchOutputPath != null)
				{
					try
					{
						searcher.writeJSONSearch(searchOutputPath);
					}
					catch(IOException e)
					{
						System.err.println("Error writing JSON to file: " + searchOutputPath.toString());
					}
				}
			}
		}
			/** start output functionality for both, threaded and non-threaded */
			
		else
		{
			System.err.println("No, or not enough, arguments provided");
		}
	}

	/*
	public static void todo(String[] args) {
		ArgumentParser parser = new ArgumentParser(args);
		
		InvertedIndex index = null;
		SearchResultBuilderInterface searcher = null;
		WebIndexBuilderInterface crawler = null;
		
		WorkQueue queue = null;
		
		if (-multi) {
			ThreadSafeInvertedIndex ts = new ThreadSafeInvertedIndex();
			index = ts;
			
			searcher = new ThreadSafeSearchResultBuilder(ts, queue);
			etc.
		}
		else {
			index = new InvertedIndex();
		}
		
		
		if (-exact) {
			searcher.parseQuery(path, true);
		}
		
		if (queue != null) {
			queue.shutdown();
		}
	}
	*/
	
}