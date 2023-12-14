import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

// Declaring a WebServlet called SessionServlet, which maps to url "/session"
@WebServlet(name = "SessionServlet", urlPatterns = "/api/session")
public class SessionServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/json");
        JsonObject jsonObject = new JsonObject();
        PrintWriter out = response.getWriter();

        // Get a instance of current session on the request
        HttpSession session = request.getSession(true);
        System.out.println("session id: " + session.getId());
        System.out.println("customer id: " + session.getAttribute("customerId"));

        String heading;

        // Retrieve data named "accessCount" from session, which count how many times the user requested before
        Integer accessCount = (Integer) session.getAttribute("accessCount");

        if (accessCount == null) {
            // Which means the user is never seen before
            accessCount = 0;
            heading = "Welcome, New-Comer";
        } else {
            // Which means the user has requested before, thus user information can be found in the session
            heading = "Welcome Back";
            accessCount++;
        }

        // Update the new accessCount to session, replacing the old value if existed
        session.setAttribute("accessCount", accessCount);

        // The following two statements show how to retrieve parameters in the request. The URL format is something like:
        // http://localhost:8080/cs122b-fall21-project2-session-example/Session?myname=Chen%20Li
        Integer customerId = (Integer) session.getAttribute("customerId");
        if (customerId != null)
            jsonObject.addProperty("loggedIn", true);
        else
            jsonObject.addProperty("loggedIn", false);

        JsonObject movieListParams = (JsonObject) session.getAttribute("movieListParams");
        if (movieListParams == null) {
            JsonObject paramJson = new JsonObject();
            paramJson.addProperty("order1", "rating desc");
            paramJson.addProperty("order2", "title asc");
            session.setAttribute("movieListParams", paramJson);
        }
        jsonObject.add("movieListParams", (JsonObject) session.getAttribute("movieListParams"));
        jsonObject.addProperty("currentLink", (String) session.getAttribute("currentLink"));

        Integer currPagination = (Integer) session.getAttribute("currPagination");
        if (currPagination == null) {
            session.setAttribute("currPagination", 25);
            currPagination = 25;
        }
        jsonObject.addProperty("currPagination", currPagination);
        System.out.println(movieListParams);

        response.setStatus(200);
        out.write(jsonObject.toString());
        out.close();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/json");
        JsonObject jsonObject = new JsonObject();
        PrintWriter out = response.getWriter();

        String order1 = request.getParameter("order1");
        String order2 = request.getParameter("order2");
        String currentLink = request.getParameter("currentLink");
        String currentPagination = request.getParameter("currPagination");

        HttpSession session = request.getSession(true);
        if (order1 != null) {
            JsonObject orderJson = new JsonObject();
            orderJson.addProperty("order1", order1);
            orderJson.addProperty("order2", order2);
            session.setAttribute("movieListParams", orderJson);
            jsonObject.add("movieListParams", orderJson);
        }
        if (currentLink != null) {
            session.setAttribute("currentLink", currentLink);
        }

        if (currentPagination != null) {
            session.setAttribute("currPagination", Integer.parseInt(currentPagination));
        }


        out.write(jsonObject.toString());
        out.close();

        response.setStatus(200);
        System.out.println("sort by session data updated: " + (String) session.getAttribute("order1") + " " + (String) session.getAttribute("order2"));
        System.out.println("currentLink: "+ currentLink);
        System.out.println("currentPagination: " + session.getAttribute("currPagination"));
    }
}
