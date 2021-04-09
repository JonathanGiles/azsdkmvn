package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;

public class ReportingTool implements Tool {

    @Override
    public void run(AzureSdkMojo mojo) {

        sendToMicrosoft(mojo);
    }

    private void sendToMicrosoft(AzureSdkMojo mojo) {
        if (!mojo.isSendToMicrosoft()) {
            return;
        }

        // Create a Report instance and send it to the appropriate endpoint
    }
}
