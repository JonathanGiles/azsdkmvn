package com.azure.tools.maven.buildtool.tools;

import java.util.ArrayList;
import java.util.List;

public class Tools {
    private static final List<Runnable> TOOLS = new ArrayList<>();
    static {
        TOOLS.add(new DependencyCheckerTool());
        TOOLS.add(new AnnotationProcessingTool());
        TOOLS.add(new ReportingTool());
    }

    public static List<Runnable> getTools() {
        return TOOLS;
    }
}
