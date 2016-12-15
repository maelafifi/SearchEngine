import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet
{
	private final ThreadSafeInvertedIndex index;
	private final int threads;
	ThreadSafeSearchResultBuilder search;

	/**
	 * Initializes the search servlet with an index and a number of threads for the search functionality
	 * 
	 * @param index
	 * @param threads
	 */
	public SearchServlet(ThreadSafeInvertedIndex index, int threads)
	{
		super();
		this.index = index;
		this.threads = threads;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		if(request.getRequestURI().endsWith("favicon.ico"))
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		out.printf("<html>%n");
		out.printf("<head><title>%s</title></head>%n", "INFINITUM");
		out.printf("<center>%n");
		out.printf("</body>%n");
		
		String user = new LoginBaseServlet().getUsername(request);

		if(user == null)
		{
			out.printf("<div align=left> <p><a href=/login>Welcome, Guest! Click to login!</a></p>");
		}
		else
		{
			out.printf("<div align=left> Welcome, " + user + "!</a></div>");
			out.printf("<div align=right>Not " + user + "? <a href='/login?logout'>Sign Out</a></div>%n");
		}
		out.printf("<body>%n");
		out.printf("<center>");
		out.printf("%n<h5>A googol is finite. This is</h5>%n");
		out.printf("<h1 style=\"color:lightblue;\">INFINITUM</h1>");

		printForm(request, response);

		out.printf("<p>");
		LoginBaseServlet.dbhandler.getLastLoginTime(user, out);
		out.printf("</p>%n");
		
		out.printf("<font size='3'><p>The current time is %s.</p></font>%n", getDate());
		out.printf("</center>%n");
		out.printf("</body>%n");
		out.printf("</center>");
		out.printf("</html>%n");

		response.setStatus(HttpServletResponse.SC_OK);
	}

	private static void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.printf("<center>");
		out.printf("<form method=\"post\" action=\"%s\">%n", "/search");
		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap></td>%n");
		out.printf("\t<td>%n");
		out.printf("<center>");
		out.printf("\t\t<input type=\"text\" name=\"search\" maxlength=\"50\" size=\"120\"><br>%n");
		out.printf("<input type=\"checkbox\" name=\"partialSearch\" value=\"partialSearch\">Partial Search<br>%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n");
		out.printf("</table>%n");
		out.printf("<p><input type=\"submit\" value=\"Search\">");
		out.printf("</form> \n%n");
		out.printf("<form method=\"get\" action=\"/search#q=%s\">%n", request.getParameter("Search"));
		out.printf("<button formaction=/history>History</button></p>");
		out.printf("<button formaction=/new_crawl>Want a bigger database? Click Here.</button></p>");
		out.printf("</form> \n%n");
		out.printf("</center>");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String query = request.getParameter("search");
		PrintWriter out = response.getWriter();

		if((query != null) && (!query.isEmpty()))
		{
			search(request, response, out);
		}
		else
		{
			response.sendRedirect("/");
		}
	}
	
	/**
	 * Performs a search of the query and outputs it to the search result page
	 * 
	 * @param request
	 * @param response
	 * @param out
	 * @throws IOException
	 */
	private void search(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException
	{
		out.printf("<body>%n");
		String query = request.getParameter("search");

		if(query == null)
		{
			query = "";
		}

		query = StringEscapeUtils.escapeHtml4(query);
		String partialSearch = request.getParameter("partialSearch");

		boolean isPartial;
		if(partialSearch == null)
		{
			isPartial = false;
		}
		else
		{
			isPartial = true;
		}

		HashMap<String, ArrayList<SearchResult>> map = new HashMap<String, ArrayList<SearchResult>>();
		search = new ThreadSafeSearchResultBuilder(threads, index);
		if((!query.equals(null)) && (!query.isEmpty()))
		{
			long startTime = System.nanoTime();
			search.searchForMatches(query, isPartial, map);
			search.finish();
			long endTime = System.nanoTime();

			out.printf("Search Result for %s</h1>%n", query);
			long duration = (endTime - startTime);
			double seconds = duration / 1000000000.0;
			
			out.printf("%n<style type='text/css'>");
			out.printf("d.pos_right{");
			out.printf("position:relative; left:70px }");
			out.printf("</style>%n%n");
			out.printf("<d class='pos_right'>");
			
			for(String b : map.keySet())
			{
				ArrayList<SearchResult> list = map.get(b);
				if(list.size() == 0)
				{
					out.printf("<p>No results found<p>%n");
				}
				else
				{
					out.printf("<p>%n<font size='3' color='darkgray'>%n%s Result(s) (%s seconds)%n</font><p>%n%n",
							list.size(), seconds);
				}

				for(SearchResult a : list)
				{
					out.printf("<p><a href=" + a.getPath() + ">" + a.getPath() + "</a>%n");
					out.printf("<br>");
				}
			}
		}
		out.printf("</d>%n");
		out.printf("<h2><a href='/search'>Back to Search</a><h2>");
		LoginBaseServlet.dbhandler.addSearchHistory(new LoginBaseServlet().getUsername(request), query);
	}

	/**
	 * Returns the date and time in a long format. For example:
	 * "12:00 am on Saturday, January 01 2000".
	 *
	 * @return current date and time
	 */
	public static String getDate()
	{
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
}
