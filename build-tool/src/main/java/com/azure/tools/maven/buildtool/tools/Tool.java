package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;

public interface Tool {

    void run(AzureSdkMojo mojo);
}
