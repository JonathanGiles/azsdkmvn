package com.azure.tools.maven.buildtool.util.logging;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import org.apache.maven.plugin.logging.Log;

public class MojoLogger implements Logger {
    private static MojoLogger INSTANCE;
    private Log mojoLog;

    public static Logger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MojoLogger(AzureSdkMojo.MOJO.getLog());
        }
        return INSTANCE;
    }

    private MojoLogger(Log mojoLog) {
        this.mojoLog = mojoLog;
    }

    @Override
    public void info(String msg) {
        mojoLog.info(msg);
    }

    @Override
    public boolean isWarnEnabled() {
        return mojoLog.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        mojoLog.warn(msg);
    }

    @Override
    public boolean isErrorEnabled() {
        return mojoLog.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        mojoLog.error(msg);
    }
}
