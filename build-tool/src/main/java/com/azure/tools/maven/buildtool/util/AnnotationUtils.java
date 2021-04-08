package com.azure.tools.maven.buildtool.util;

import org.reflections8.Reflections;
import org.reflections8.ReflectionsException;
import org.reflections8.scanners.MemberUsageScanner;
import org.reflections8.scanners.MethodAnnotationsScanner;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotationUtils {

    private AnnotationUtils() {
        // no-op
    }

    public static Set<AnnotatedMethodCallerResult> findCallsToAnnotatedMethod(final Class<? extends Annotation> annotation,
                                                                              final Stream<Path> paths) {
        final ConfigurationBuilder config = new ConfigurationBuilder()
              .setScanners(new MethodAnnotationsScanner(), new MemberUsageScanner());

        final List<URL> urls = paths.map(AnnotationUtils::pathToUrl).collect(Collectors.toList());
        config.addUrls(urls);
        config.addClassLoader(URLClassLoader.newInstance(urls.toArray(new URL[0])));

        // This is extremely ugly code, but it is necessary as the reflections library throws away the classloader
        // I have built above, and so when it goes looking for classes it cannot always find them. What I am doing here
        // is augmenting the actual context class loader with the additional urls, so that when the reflections library
        // falls back to using the context class loader (which it does by default, because it throws away the proper
        // class loader I built above), it can still find the classes I want it to find.
        final URLClassLoader contextClassLoader = (URLClassLoader) ClasspathHelper.contextClassLoader();
        try {
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            for (final URL url : urls) {
                method.invoke(contextClassLoader, url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Reflections reflections = new Reflections(config);
        final Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(annotation);
        final Set<AnnotatedMethodCallerResult> results = new HashSet<>();

        annotatedMethods.stream().forEach(method -> {
            try {
                final Set<Member> callingMethods = reflections.getMethodUsage(method);
                if (!callingMethods.isEmpty()) {
                    System.out.println("For method " + method + ", the following methods call it:");
                    callingMethods.forEach(member -> {
                        System.out.println("  " + member);
                        results.add(new AnnotatedMethodCallerResult(annotation, method, member));
                    });
                } else {
                    System.out.println("Couldn't find method usage of method " + method);
                }
            } catch (ReflectionsException e) {
                System.err.println("Error trying to find usage of method " + method);
//                e.printStackTrace();
            }
        });

        return results;
    }

    private static URL pathToUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
