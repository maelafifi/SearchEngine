import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class NewCrawlServlet extends HttpServlet
{
	private ThreadSafeInvertedIndex index;
	int threads;

	public NewCrawlServlet(ThreadSafeInvertedIndex index, int threads)
	{
		super();
		this.index = index;
		this.threads = threads;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();
		out.printf("<html>%n");
		out.printf("<head><title>%s</title></head>%n", "A STEP CLOSER TO INFINITUM...");
		
		String status = request.getParameter("ok");
		if(status != null)
		{
			out.printf("<p>You URL has been added and crawled!</p>");
		}

		out.printf("<p>Enter a URL to crawl<p>");
		printForm(request, response);
		String newCrawl = request.getParameter("newcrawl");

		String userChoice = request.getParameter("addcrawl");
		if(userChoice != null)
		{
			try
			{
				new URL(newCrawl);
			}
			catch(MalformedURLException ex)
			{
				response.sendRedirect("/new_crawl?error=invalid_link");
			}
			
			URL url = new URL(newCrawl);
			if((url.getProtocol() == null) || (url.getHost() == null) || (url.getPath() == null))
			{
				response.sendRedirect("/new_crawl?error=invalid_link");
			}
			if((newCrawl == null) || newCrawl.trim().isEmpty())
			{
				response.sendRedirect("/new_crawl?error=empty_link");
			}
			else
			{
				addToDatabase(newCrawl, request, response);
				response.sendRedirect("/new_crawl?ok");
			}
		}

		out.printf("</body>%n");
		out.printf("</html>%n");

	}

	private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();

		out.printf("<form method=\"get\" action=\"%s\">%n", request.getServletPath());
		out.printf("<input type=\"text\" name=\"newcrawl\" maxlength=\"200\" size=\"120\">");
		out.printf("<p><input type=\"submit\" name=\"addcrawl\" value=\"ADD\">");

		out.printf("<h2><a href='/'>Back to Search</a><h2>");
	}

	/**
	 * Takes in a seed from the user and crawls the web and creates a new index. The index
	 * is then added to the original index 
	 * 
	 * @param seed
	 * 			User input seed to be crawled
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void addToDatabase(String seed, HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		ThreadSafeInvertedIndex newIndex = new ThreadSafeInvertedIndex();
		ThreadSafeWebIndexBuilder webcrawler = new ThreadSafeWebIndexBuilder(newIndex, threads);
		webcrawler.startCrawl(seed);

		index.addAll(newIndex);
	}

}