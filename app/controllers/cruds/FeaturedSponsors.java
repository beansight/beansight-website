package controllers.cruds;
 
import controllers.CRUD;
import controllers.Check;
import controllers.Secure;
import play.*;
import play.mvc.*;

@Check("admin")
@With(Secure.class)
public class FeaturedSponsors extends CRUD {
}
