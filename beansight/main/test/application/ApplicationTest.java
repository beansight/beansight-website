package application;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testThatWelcomePageWorks() {
        Response response = GET("/welcome");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset("utf-8", response);
    }
    
    @Test
    public void testAdminSecurity() {
        Response response = GET("/admin");
        assertStatus(302, response);
        assertHeaderEquals("Location", "http://localhost/login", response);
    }
    
}