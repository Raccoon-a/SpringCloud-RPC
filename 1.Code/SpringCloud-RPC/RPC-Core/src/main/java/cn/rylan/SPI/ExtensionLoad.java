/*
 * Copyright (c) 2023 Rylan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 */
package cn.rylan.SPI;

import lombok.extern.slf4j.Slf4j;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtensionLoad<T> {
    private static final String PREFIX = "META-INF/SPI/";

    private static final Map<String,Object> INSTANCE_MAP = new ConcurrentHashMap<>();

    private static final Map<String,ExtensionLoad<?>> EXTENSION_LOAD_MAP = new ConcurrentHashMap<>();

    private final Map<String, Class<?>> cacheClass = new ConcurrentHashMap<>();
    private Class<?> clazz;

    private ExtensionLoad(Class<?> clazz){
        this.clazz = clazz;
    }

    public static <T> ExtensionLoad<T> getExtensionLoader(Class<T> clazz){
        if (clazz == null){
            throw new IllegalArgumentException("clazz must not null");
        }
        ExtensionLoad<T> extensionLoad = (ExtensionLoad<T>) EXTENSION_LOAD_MAP.get(clazz.getName());
        if (extensionLoad == null){
            EXTENSION_LOAD_MAP.putIfAbsent(clazz.getName(), new ExtensionLoad<>(clazz));
            extensionLoad = (ExtensionLoad<T>) EXTENSION_LOAD_MAP.get(clazz.getName());
        }
        return extensionLoad;
    }

    public T getExtension(String name){
        T instance = null;
        try {
            Class<?> _clazz = getClasses().get(name);
            instance = (T) INSTANCE_MAP.get(_clazz.getName());
            if (instance == null){
                INSTANCE_MAP.putIfAbsent(_clazz.getName(), _clazz.newInstance());
                instance = (T) INSTANCE_MAP.get(_clazz.getName());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("getExtension error: {}", e.getMessage());
        }
        return instance;
    }

    public T getExtension(String name, Object[] paramValues, Class<?>... paramTypes){
        T instance = null;
        try {
            Class<?> _clazz = getClasses().get(name);
            Constructor<?> constructor = _clazz.getDeclaredConstructor(paramTypes);
            instance = (T) constructor.newInstance(paramValues);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("getExtension error: {}", e.getMessage());
        }
        return instance;
    }

    private Map<String, Class<?>> getClasses() {
        if (cacheClass.size() == 0){
            loadClasses();
        }
        return cacheClass;
    }

    private void loadClasses() {
        try {
            String path = PREFIX + clazz.getName();
            Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources(path);
            while (urlEnumeration.hasMoreElements()){
                URL url = urlEnumeration.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(),
                        StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null){
                        String[] keyAndClass = line.split("=");
                        cacheClass.put(keyAndClass[0],Class.forName(keyAndClass[1]));
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("loadClassed error: {}", e.getMessage());
        }
    }

}
