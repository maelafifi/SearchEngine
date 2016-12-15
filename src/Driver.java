import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Driver class for project1 and all future projects Project 1: Checks for flags
 * "-dir" and "-index" as well as the parameter for those flags If the flags
 * exist, an invertedIndex is created through the InvertedIndexBuilder, and if
 * "-index" flag exists, a JSON file will be created, and output based on the
 * flags respective value (or default value) if there is no respective value.
 * Project 2: Checks for search flags. If the flags exist, the proper search is
 * performed and if there is a file for it to be output to, the search result is
 * output to the file Project 3: Checks for url flag. If the flag exists, the
 * url is sent to be parsed for links, a new index for the words in those links
 * is created, and all previous functionality remains usable. Project 4: Added
 * thread safe classes, methods, and helpers to increase efficiency and
 * usability. Checks for multi flag, and if it exists, web and directory
 * indexing, as well as search functionalities will now be utilized in a thread
 * safe manner.
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
		String search = null;
		String searchOutput = null;
		String urlSeed = null;
		int threads = 0;
		boolean partial = true;
		Path input = null;
		Path output = null;
		Path searchPath = null;
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
			}/* end thread flag */

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
			} /* end directory flag */

			if(parser.hasFlag("-index")) /* start index flag */
			{
				outputFile = parser.getValue("-index", "index.json");
				output = Paths.get(outputFile);
			} /* end index flag */

			if(parser.hasFlag("-exact")) /* start exact search flag */
			{
				if(parser.getValue("-exact") != null)
				{
					partial = false;
					search = parser.getValue("-exact");
					searchPath = Paths.get(search);
				}
				else
				{
					System.out.println("No directory listed");
				}
			} /* end exact search flag */

			if(parser.hasFlag("-url")) /* start url flag */
			{
				if(parser.getValue("-url") != null)
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
					partial = true;
					search = parser.getValue("-query");
					searchPath = Paths.get(search);
				}
			} /* end query flag */

			if(parser.hasFlag("-results")) /* start results flag */
			{
				searchOutput = parser.getValue("-results", "results.json");
				searchOutputPath = Paths.get(searchOutput);
			} /* end results flag */

			/**
			 * Start of multithreaded indexing and search if threads are
			 * provided
			 */
			if(threads != 0)
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

				if(parser.hasFlag("-port")) /* start search engine */
				{
					try
					{
						DatabaseConnector test = new DatabaseConnector("database.properties");
						System.out.println("Connecting to " + test.uri);

						if (test.testConnection())
						{
							System.out.println("Connection to database established.");
						}
						else
						{
							System.err.println("Unable to connect properly to database. Turn down server");
							System.exit(0);
						}
					}
					catch (Exception e)
					{
						System.err.println("Unable to connect properly to database.");
						System.err.println(e.getMessage());
					}
					
					Server server = new Server(parser.getValue("-port", 8080));
					
					ServletContextHandler handler1 = new ServletContextHandler(ServletContextHandler.SESSIONS);

					handler1.setContextPath("/");
					handler1.addServlet(LoginUserServlet.class, "/login");
					handler1.addServlet(new ServletHolder(new SearchServlet(tsIndex, threads)),"/");
					handler1.addServlet(LoginRegisterServlet.class, "/register");
					handler1.addServlet(HistoryServlet.class, "/history");
					handler1.addServlet(ChangePasswordServlet.class, "/change_password");
					handler1.addServlet(new ServletHolder(new NewCrawlServlet(tsIndex, threads)),"/new_crawl");

					server.setHandler(handler1);
					try
					{
						server.start();
						server.join();
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
					}
				} /* end search engine */

				if(output != null)
				{
					try
					{
						tsIndex.writeJSON(output);
					}
					catch(IOException e)
					{
						System.err.println("Error writing JSON to file: " + output.toString());
					}
				}

				if(searchPath != null)
				{
					try
					{
						tsSearcher.parseSearchFile(searchPath, partial);
						tsSearcher.shutdown();
					}
					catch(IOException e)
					{
						System.err.println("Error opening query file: " + searchPath.toString());
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
			}/**
				 * end of multithreaded indexing and search if threads are
				 * provided
				 */
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

				if(searchPath != null)
				{
					try
					{
						searcher.parseSearchFile(searchPath, partial);
					}
					catch(IOException e)
					{
						System.err.println("Error opening query file: " + searchPath.toString());
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
	 * public static void todo(String[] args) { ArgumentParser parser = new
	 * ArgumentParser(args);
	 * 
	 * InvertedIndex index = null; SearchResultBuilderInterface searcher = null;
	 * WebIndexBuilderInterface crawler = null;
	 * 
	 * WorkQueue queue = null;
	 * 
	 * if (-multi) { ThreadSafeInvertedIndex ts = new ThreadSafeInvertedIndex();
	 * index = ts;
	 * 
	 * searcher = new ThreadSafeSearchResultBuilder(ts, queue); etc. } else {
	 * index = new InvertedIndex(); }
	 * 
	 * 
	 * if (-exact) { searcher.parseQuery(path, true); }
	 * 
	 * if (queue != null) { queue.shutdown(); } }
	 */

}