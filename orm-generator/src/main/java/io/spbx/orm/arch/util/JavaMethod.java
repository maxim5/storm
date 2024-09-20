package io.spbx.orm.arch.util;

import io.spbx.util.reflect.BasicMembers;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public record JavaMethod(@NotNull Method rawMethod, @NotNull Class<?> ownerClass) implements Member, AnnotatedElement {
    public @NotNull String getName() {
        return rawMethod.getName();
    }

    public @NotNull Class<?> getReturnType() {
        return rawMethod.getReturnType();
    }

    public int getParameterCount() {
        return rawMethod.getParameterCount();
    }

    public @NotNull Class<?>[] getExceptionTypes() {
        return rawMethod.getExceptionTypes();
    }

    public boolean isOwn() {
        return ownerClass == getDeclaringClass();
    }

    public boolean isNonStatic() {
        return !BasicMembers.isStatic(rawMethod);
    }

    public boolean isPackageVisible() {
        return BasicMembers.isVisibleInPackage(rawMethod, ownerClass.getPackageName());
    }

    @Override public Class<?> getDeclaringClass() {
        return rawMethod.getDeclaringClass();
    }

    @Override public int getModifiers() {
        return rawMethod.getModifiers();
    }

    @Override public boolean isSynthetic() {
        return rawMethod.isSynthetic();
    }

    @Override public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
        return rawMethod.getAnnotation(annotationClass);
    }

    @Override public Annotation[] getAnnotations() {
        return rawMethod.getAnnotations();
    }

    @Override public Annotation[] getDeclaredAnnotations() {
        return rawMethod.getDeclaredAnnotations();
    }
}
