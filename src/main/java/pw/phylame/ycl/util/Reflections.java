/*
 * Copyright 2016 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package pw.phylame.ycl.util;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static pw.phylame.ycl.util.StringUtils.capitalized;
import static pw.phylame.ycl.util.StringUtils.isEmpty;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public final class Reflections {

    private Reflections() {
    }

    public static String normalizedName(String name) {
        if (isEmpty(name)) {
            return name;
        }
        val length = name.length();
        char ch = name.charAt(0);
        if (isUpperCase(ch)) {
            if (length > 2) {
                char next = name.charAt(1);
                if (isLowerCase(next)) {
                    throw new IllegalArgumentException("invalid name: " + name);
                } else {
                    return name;
                }
            } else {
                return name;
            }
        } else if (isLowerCase(ch)) {
            if (length > 2) {
                char next = name.charAt(1);
                if (isUpperCase(next)) {
                    return name;
                } else {
                    return capitalized(name);
                }
            } else {
                return name.toUpperCase();
            }
        }
        return name;
    }

    public static void makeAccessible(@NonNull AccessibleObject object) {
        if (!object.isAccessible()) {
            object.setAccessible(true);
        }
    }

    public static Class<?> getGenericType(@NonNull Class<?> clazz) {
        return getGenericType(clazz, 0);
    }

    public static Class<?> getGenericType(@NonNull Class<?> clazz, int index) {
        val type = clazz.getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            return Object.class;
        }
        val types = ((ParameterizedType) type).getActualTypeArguments();
        if ((index >= types.length) || (index < 0)) {
            return Object.class;
        }
        if (!(types[index] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>) types[index];
    }

    public static Field findField(@NonNull Class<?> clazz, String name) {
        if (isEmpty(name)) {
            return null;
        }
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException | SecurityException ignored) {
            }
        }
        return null;
    }

    public static List<Field> getFields(@NonNull Class<?> clazz, Function<Field, Boolean> prediction) {
        val fields = new ArrayList<Field>();
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            for (val field : clazz.getDeclaredFields()) {
                if (prediction == null || prediction.apply(field)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    @SneakyThrows(IllegalAccessException.class)
    public static Object getFieldValue(@NonNull Object target, String name) {
        val field = findField(target.getClass(), name);
        if (field == null) {
            throw new IllegalStateException("no such field: " + name);
        }
        makeAccessible(field);
        return field.get(target);
    }

    @SneakyThrows(IllegalAccessException.class)
    public static void setFieldValue(@NonNull Object target, String name, Object value) {
        val field = findField(target.getClass(), name);
        if (field == null) {
            throw new IllegalStateException("no such field: " + name);
        }
        makeAccessible(field);
        field.set(target, value);
    }

    public static Method findMethod(@NonNull Class<?> clazz, String name, Class<?>... types) {
        if (isEmpty(name)) {
            return null;
        }
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredMethod(name, types);
            } catch (NoSuchMethodException | SecurityException ignored) {
            }
        }
        return null;
    }

    public static List<Method> getMethods(@NonNull Class<?> clazz, Function<Method, Boolean> prediction) {
        val methods = new ArrayList<Method>();
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            for (val method : clazz.getDeclaredMethods()) {
                if (prediction == null || prediction.apply(method)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    public static Method findGetter(@NonNull Class<?> clazz, String name) {
        if (isEmpty(name)) {
            return null;
        }
        name = normalizedName(name);
        val method = findMethod(clazz, "get" + name);
        if (method != null) {
            return method;
        } else {
            return findMethod(clazz, "is" + name);
        }
    }

    public static Method findGetter(@NonNull Class<?> clazz, @NonNull Class<?> type, String name) {
        return isEmpty(name)
                ? null
                : findMethod(clazz, (type == boolean.class ? "is" : "get") + normalizedName(name));
    }

    public static Method findSetter(@NonNull Class<?> clazz, String name) {
        if (isEmpty(name)) {
            return null;
        }
        name = "set" + normalizedName(name);
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            for (val method : clazz.getDeclaredMethods()) {
                if (name.equals(method.getName()) && method.getParameterTypes().length == 1) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Method findSetter(@NonNull Class<?> clazz, @NonNull Class<?> type, String name) {
        return isEmpty(name)
                ? null
                : findMethod(clazz, "set" + normalizedName(name), type);
    }

    @SneakyThrows(IllegalAccessException.class)
    public static Object invokeMethod(@NonNull Method method, Object target, Object... args) {
        makeAccessible(method);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        }
    }

    public static Object getProperty(@NonNull Object target, String name) {
        val getter = findGetter(target.getClass(), name);
        if (getter == null) {
            throw new IllegalStateException("no such getter for : " + name);
        }
        return invokeMethod(getter, target);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProperty(@NonNull Object target, String name, @NonNull Class<? extends T> type) {
        val getter = findGetter(target.getClass(), type, name);
        if (getter == null) {
            throw new IllegalStateException("no such getter for : " + name);
        }
        return (T) invokeMethod(getter, target);
    }

    public static void setProperty(@NonNull Object target, String name, Object value) {
        val setter = findSetter(target.getClass(), value.getClass(), name);
        if (setter == null) {
            throw new IllegalStateException("no such setter for : " + name);
        }
        invokeMethod(setter, target, value);
    }

    public static <T> void setProperty(@NonNull Object target, String name, @NonNull Class<? super T> type, T value) {
        val setter = findSetter(target.getClass(), type, name);
        if (setter == null) {
            throw new IllegalStateException("no such setter for : " + name);
        }
        invokeMethod(setter, target, value);
    }

    public static Object i(Object target, String name, Object... args) {
        Class<?>[] types = typesOf(args);
        val method = findMethod(target.getClass(), name, types);
        if (method == null) {
            throw new IllegalStateException("no such method: " + name);
        }
        makeAccessible(method);
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object i(Class<?> target, String name, Object... args) {
        Class<?>[] types = typesOf(args);
        val method = findMethod(target, name, types);
        if (method == null) {
            throw new IllegalStateException("no such method: " + name);
        }
        makeAccessible(method);
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Object p(Object target, String name) {
        return getProperty(target, name);
    }

    public static void p(Object target, String name, Object value) {
        setProperty(target, name, value);
    }

    private static Class<?>[] typesOf(Object[] args) {
        Class<?>[] types = null;
        if (args.length > 0) {
            types = new Class[args.length];
            for (int i = 0; i < types.length; ++i) {
                types[i] = args[i].getClass();
            }
        }
        return types;
    }
}
