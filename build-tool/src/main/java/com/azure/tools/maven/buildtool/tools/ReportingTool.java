package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;

public class ReportingTool implements Runnable {

    @Override
    public void run() {
        sendToMicrosoft();
    }

    private void sendToMicrosoft() {
        if (!AzureSdkMojo.MOJO.isSendToMicrosoft()) {
            return;
        }

        // Create a Report instance and send it to the appropriate endpoint
    }
}
