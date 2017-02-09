/*
 * Copyright 2017 Peng Wan <phylame@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.commons.util;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import pw.phylame.commons.function.Prediction;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static pw.phylame.commons.util.StringUtils.capitalized;
import static pw.phylame.commons.util.StringUtils.isEmpty;

public final class Reflections {

    private Reflections() {
    }

    public static String normalized(String name) {
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

    public static Field getField(@NonNull Class<?> clazz, String name) {
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

    public static List<Field> getFields(@NonNull Class<?> clazz, Prediction<? super Field> prediction) {
        val fields = new ArrayList<Field>();
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            for (val field : clazz.getDeclaredFields()) {
                if (prediction == null || prediction.test(field)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    @SneakyThrows(IllegalAccessException.class)
    public static Object getFieldValue(@NonNull Class<?> clazz, String name) {
        val field = getField(clazz, name);
        if (field == null) {
            throw new RuntimeException("no such field: " + name);
        }
        makeAccessible(field);
        return field.get(null);
    }

    @SneakyThrows(IllegalAccessException.class)
    public static Object getFieldValue(@NonNull Object target, String name) {
        val field = getField(target.getClass(), name);
        if (field == null) {
            throw new RuntimeException("no such field: " + name);
        }
        makeAccessible(field);
        return field.get(target);
    }

    @SneakyThrows(IllegalAccessException.class)
    public static void setFieldValue(@NonNull Class<?> clazz, String name, Object value) {
        val field = getField(clazz, name);
        if (field == null) {
            throw new RuntimeException("no such field: " + name);
        }
        makeAccessible(field);
        field.set(null, value);
    }

    @SneakyThrows(IllegalAccessException.class)
    public static void setFieldValue(@NonNull Object target, String name, Object value) {
        val field = getField(target.getClass(), name);
        if (field == null) {
            throw new RuntimeException("no such field: " + name);
        }
        makeAccessible(field);
        field.set(target, value);
    }

    public static Method getMethod(@NonNull Class<?> clazz, String name, Class<?>... types) {
        if (isEmpty(name)) {
            return null;
        }
        Class<?> copy = clazz;
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredMethod(name, types);
            } catch (NoSuchMethodException | SecurityException ignored) {
            }
        }
        if (Versions.jvmVersion >= 8) { // for Java 8 default methods
            for (val iface : copy.getInterfaces()) {
                try {
                    return iface.getMethod(name, types);
                } catch (NoSuchMethodException | SecurityException ignored) {
                }
            }
        }
        return null;
    }

    public static List<Method> getMethods(@NonNull Class<?> clazz, Prediction<? super Method> prediction) {
        val methods = new ArrayList<Method>();
        Class<?> copy = clazz;
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            for (val method : clazz.getDeclaredMethods()) {
                if (prediction == null || prediction.test(method)) {
                    methods.add(method);
                }
            }
        }
        if (Versions.jvmVersion >= 8) { // for Java 8 default methods
            for (val iface : copy.getInterfaces()) {
                for (val method : iface.getMethods()) {
                    if (prediction == null || prediction.test(method)) {
                        methods.add(method);
                    }
                }
            }
        }
        return methods;
    }

    public static Method getGetter(@NonNull Class<?> clazz, String name) {
        if (isEmpty(name)) {
            return null;
        }
        name = normalized(name);
        val method = getMethod(clazz, "get" + name);
        if (method != null) {
            return method;
        } else {
            return getMethod(clazz, "is" + name);
        }
    }

    public static Method getGetter(@NonNull Class<?> clazz, String name, @NonNull Class<?> type) {
        return isEmpty(name)
                ? null
                : getMethod(clazz, (type == boolean.class ? "is" : "get") + normalized(name));
    }

    public static Method getSetter(@NonNull Class<?> clazz, String name) {
        if (isEmpty(name)) {
            return null;
        }
        name = "set" + normalized(name);
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            for (val method : clazz.getDeclaredMethods()) {
                if (name.equals(method.getName()) && method.getParameterTypes().length == 1) {
                    return method;
                }
            }
        }
        return null;
    }

    public static Method getSetter(@NonNull Class<?> clazz, String name, @NonNull Class<?> type) {
        return isEmpty(name)
                ? null
                : getMethod(clazz, "set" + normalized(name), type);
    }

    @SneakyThrows(IllegalAccessException.class)
    public static Object invokeMethod(@NonNull Method method, Object target, Object... args) {
        makeAccessible(method);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    public static Object getProperty(@NonNull Object target, String name) {
        val getter = getGetter(target.getClass(), name);
        if (getter == null) {
            throw new RuntimeException("no such getter for : " + name);
        }
        return invokeMethod(getter, target);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProperty(@NonNull Object target, String name, @NonNull Class<? extends T> type) {
        val getter = getGetter(target.getClass(), name, type);
        if (getter == null) {
            throw new RuntimeException("no such getter for : " + name);
        }
        return (T) invokeMethod(getter, target);
    }

    public static void setProperty(@NonNull Object target, String name, Object value) {
        val setter = getSetter(target.getClass(), name, value.getClass());
        if (setter == null) {
            throw new RuntimeException("no such setter for : " + name);
        }
        invokeMethod(setter, target, value);
    }

    public static <T> void setProperty(@NonNull Object target, String name, @NonNull Class<? super T> type, T value) {
        val setter = getSetter(target.getClass(), name, type);
        if (setter == null) {
            throw new RuntimeException("no such setter for : " + name);
        }
        invokeMethod(setter, target, value);
    }

    public static Object i(Object target, String name, Object... args) {
        Class<?>[] types = typesOf(args);
        val method = getMethod(target.getClass(), name, types);
        if (method == null) {
            throw new RuntimeException("no such method: " + name);
        }
        makeAccessible(method);
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object i(Class<?> target, String name, Object... args) {
        Class<?>[] types = typesOf(args);
        val method = getMethod(target, name, types);
        if (method == null) {
            throw new RuntimeException("no such method: " + name);
        }
        makeAccessible(method);
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object p(Object target, String name) {
        return getProperty(target, name);
    }

    public static void p(Object target, String name, Object value) {
        setProperty(target, name, value);
    }

    public static <T> void p(Object target, String name, Class<? super T> type, T value) {
        setProperty(target, name, type, value);
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
