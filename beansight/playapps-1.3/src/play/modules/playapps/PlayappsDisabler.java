package play.modules.playapps;

import play.Play;
import play.PlayPlugin;

public class PlayappsDisabler extends PlayPlugin {

    @Override
    public void onConfigurationRead() {
        Play.configuration.remove("module.playapps");
    }

}
