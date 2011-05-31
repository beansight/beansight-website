package controllers.cruds;
 
import play.mvc.With;
import controllers.CRUD;
import controllers.Check;
import controllers.Secure;

@Check("admin")
@With(Secure.class)
public class Insights extends CRUD {
}
