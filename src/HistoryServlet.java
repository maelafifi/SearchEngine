import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to get history for a specified user.
 * Allows users to view and delete search their own search history.
 * 
 * @author Mohamed Elafifi
 *
 */
@SuppressWarnings("serial")
public class HistoryServlet extends HttpServlet
{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		PrintWriter out = response.getWriter();
		out.printf("<html>%n");
		out.printf("<head><title>%s</title></head>%n", "INFINITUM");
		out.printf("<center>%n");
		out.printf("</body>%n");
		new LoginBaseServlet().prepareResponse("INFINITUM", response);

		String user = new LoginBaseServlet().getUsername(request);

		if(user!=null)
		{
			out.printf("<h1>Hello %s </h1>", user);
		}
		out.printf("<form method=\"get\" action=\"%s\">%n", request.getServletPath());
		out.printf("<p><input type=\"submit\" name=\"history\" value=\"Show history\">");
		out.printf("<input type=\"submit\" name=\"deleteHistory\" value=\"Delete History\">");
		out.printf("</form>\n%n");
		String button = request.getParameter("history");

		if(!(button == null))
		{
			showHistory(user, out);
		}
		button = request.getParameter("deleteHistory");
		
		if(!(button == null))
		{
			clearHistory(user, out);
		}

		out.printf("<h2 style=\"color:black;\"><a href='/search'>Back to Search</a><h2>");
	}

	/**
	 * Shows the search history for the specified user
	 * 
	 * @param user
	 * 			user whose history is to be shown
	 * @param out
	 */
	private void showHistory(String user, PrintWriter out)
	{
		if(user!=null)
		{
			out.printf("<p>Your Search History:<p>");
			LoginBaseServlet.dbhandler.getHistory(user, out);
		}
		else
		{
			out.printf("<p>No History To Show. Please login to see history.<p>");
		}
	}

	/**
	 * Deletes the search history for the specified user
	 * 
	 * @param user
	 * 			user whose history is to be deleted
	 * @param out
	 */
	private void clearHistory(String user, PrintWriter out)
	{
		if(user!=null)
		{
			out.printf("<p>Your Search History has been cleared<p>");
			LoginBaseServlet.dbhandler.clearHistory(user);
		}
		else
		{
			out.printf("<p>No History To Clear. Please login to clear history.<p>");
		}
	}

}