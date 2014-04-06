package play.modules.playapps;

import java.io.File;
import play.Logger;
import play.Play;
import play.PlayPlugin;

public class Plugin extends PlayPlugin {

    @Override
    public void onLoad() {
        if(Play.id != null && Play.id.startsWith("playapps")) {
            Logger.info("Running on playapps.net");

            // Mode DEV forbidden
            if (Play.mode == Play.Mode.DEV) {
                Logger.error("You can't run an application in DEV mode on the playapps.net platform.\nAdd %playapps.application.mode=PROD to your application.conf file.");
                System.exit(-1);
            }

            // HTTP only on port 9000
            if (!Play.configuration.getProperty("http.port", "").equals("9000") && !Play.configuration.getProperty("http.port", "").equals("")) {
                Logger.error("HTTP server must listen on port 9000.\nAdd %playapps.http.port=9000 to your application.conf file.");
                System.exit(-1);
            }

            // Tmp
            Play.configuration.setProperty("play.tmp", "/slot/storage/tmp");
            Play.tmpDir = new File("/slot/storage/tmp");
        }
    }

    @Override
    public void onConfigurationRead() {
        if(Play.id != null && Play.id.startsWith("playapps")) {
            // Patch database
            if (Play.configuration.containsKey("db")) {
                Play.configuration.setProperty("db", "mysql:play:play@play");
            }

            // X-Forward
            Play.configuration.setProperty("XForwardedSupport", "127.0.0.1");

            // Attachments
            Play.configuration.setProperty("attachments.path", "data/attachments");
        }
    }
}
