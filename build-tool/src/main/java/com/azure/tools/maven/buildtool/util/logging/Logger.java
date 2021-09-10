package com.azure.tools.maven.buildtool.util.logging;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;

public interface Logger {

    static Logger getInstance() {
        if (AzureSdkMojo.MOJO == null) {
            return ConsoleLogger.getInstance();
        } else {
            return MojoLogger.getInstance();
        }
    }

    void info(String msg);

    boolean isWarnEnabled();

    void warn(String msg);

    boolean isErrorEnabled();

    void error(String msg);
}
