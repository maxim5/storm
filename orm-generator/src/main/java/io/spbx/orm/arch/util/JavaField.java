package io.spbx.orm.arch.util;

import io.spbx.util.reflect.BasicGenerics;
import io.spbx.util.reflect.BasicMembers;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

public record JavaField(@NotNull Field rawField, @NotNull Class<?> ownerClass) implements Member, AnnotatedElement {
    public @NotNull String getName() {
        return rawField.getName();
    }

    public @NotNull Class<?> getType() {
        return rawField.getType();
    }

    public @NotNull Type getGenericType() {
        return rawField.getGenericType();
    }

    public @NotNull Type[] getGenericTypeArguments() {
        return BasicGenerics.getGenericTypeArgumentsOfField(rawField);
    }

    public boolean isOwn() {
        return ownerClass == getDeclaringClass();
    }

    public boolean isNonStatic() {
        return !BasicMembers.isStatic(rawField);
    }

    public boolean isPackageVisible() {
        return BasicMembers.isVisibleInPackage(rawField, ownerClass.getPackageName());
    }

    @Override public Class<?> getDeclaringClass() {
        return rawField.getDeclaringClass();
    }

    @Override public int getModifiers() {
        return rawField.getModifiers();
    }

    @Override public boolean isSynthetic() {
        return rawField.isSynthetic();
    }

    @Override public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
        return rawField.getAnnotation(annotationClass);
    }

    @Override public Annotation[] getAnnotations() {
        return rawField.getAnnotations();
    }

    @Override public Annotation[] getDeclaredAnnotations() {
        return rawField.getDeclaredAnnotations();
    }
}
