import play.*;
import play.jobs.*;
import play.test.*;
 
import models.*;
 
@OnApplicationStart
public class Bootstrap extends Job {
 
    public void doJob() {
        // Check if the database is empty
        if(User.count() == 0) {
            // Initial data of the plateform
            Fixtures.load("initial-data.yml");
        	
        	// Load debug data only if run in DEV mode
            if(play.Play.mode == play.Play.mode.DEV) {
            	Fixtures.load("debug-data.yml");
            }
        }
    }
}