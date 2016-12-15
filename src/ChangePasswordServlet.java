import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ChangePasswordServlet extends LoginBaseServlet
{
	protected static final LoginDatabaseHandler dbhandler = LoginDatabaseHandler.getInstance();

	@Override
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException
	{

		prepareResponse("Change Password", response);

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");

		if (error != null)
		{
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		printForm(out);

		finishResponse(response);
	}

	private void printForm(PrintWriter out)
	{
		out.println("<form  method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Username:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Old Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"oldpassword\" size=\"30\"></td>");
		out.println("\t<tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>New Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"newpassword\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Change Password\"></p>");
		out.println("</form>");

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String user = request.getParameter("user");
		String oldPassword = request.getParameter("oldpassword");
		String newPassword = request.getParameter("newpassword");
		
		Status status = dbhandler.authenticateUser(user, oldPassword);
		if(status == Status.OK)
		{
			status = dbhandler.changePassword(user, newPassword);
			if (status == Status.OK)
			{
				response.sendRedirect(response.encodeRedirectURL("/login?passwordchanged=true"));
			}
			else
			{
				String url = "/reset_password?error=" + status.name();
				url = response.encodeRedirectURL(url);
				response.sendRedirect(url);
			}
		}
		else
		{
			String url = "/reset_password?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);
		}	
	}
}