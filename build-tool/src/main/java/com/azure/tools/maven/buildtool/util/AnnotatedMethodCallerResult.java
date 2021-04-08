package com.azure.tools.maven.buildtool.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Objects;

public class AnnotatedMethodCallerResult {
    private final Class<? extends Annotation> annotation;
    private final Method annotatedMethod;
    private final Member callingMember;

    public AnnotatedMethodCallerResult(final Class<? extends Annotation> annotation,
                                       final Method annotatedMethod,
                                       final Member callingMember) {
        this.annotation = annotation;
        this.annotatedMethod = annotatedMethod;
        this.callingMember = callingMember;
    }

    @Override
    public String toString() {
        return "AnnotatedMethodCallerResult{" +
                   "annotation=" + annotation +
                   ", annotatedMethod=" + annotatedMethod +
                   ", callingMember=" + callingMember +
                   '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AnnotatedMethodCallerResult that = (AnnotatedMethodCallerResult) o;
        return annotation.equals(that.annotation) && annotatedMethod.equals(that.annotatedMethod) &&
                   callingMember.equals(that.callingMember);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotation, annotatedMethod, callingMember);
    }
}
