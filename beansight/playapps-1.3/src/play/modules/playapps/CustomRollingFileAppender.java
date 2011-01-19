package play.modules.playapps;

import org.apache.log4j.RollingFileAppender;

public class CustomRollingFileAppender extends RollingFileAppender {

    @Override
    public void activateOptions() {
        rollOver();
        super.activateOptions();
    }

}
