/*
 * Copyright (c) 2025 周潮. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.myth.earth.develop.ui.toolkit;

import com.intellij.openapi.diagnostic.Logger;
import com.myth.earth.develop.ui.toolkit.core.Tool;
import com.myth.earth.develop.ui.toolkit.core.ToolCategory;
import com.myth.earth.develop.ui.toolkit.core.ToolView;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 工具加载器
 *
 * @author zhouchao
 * @date 2025/9/7 下午10:01
 **/
public class ToolkitLoader {

    private static final Logger LOGGER = Logger.getInstance(ToolkitLoader.class);

    private final Map<ToolCategory, List<Class<? extends ToolView>>> categorizedTools = new EnumMap<>(ToolCategory.class);

    private final Map<Class<? extends ToolView>, ToolView> instanceMap = new ConcurrentHashMap<>();

    public ToolkitLoader(String basePackage) {
        try {
            // 将包名转换为文件路径
            String path = basePackage.replace('.', '/');
            Enumeration<URL> resources = getClass().getClassLoader().getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource == null) {
                    LOGGER.info("无法找到包路径: " + basePackage);
                    continue;
                }

                String protocol = resource.getProtocol();
                if ("file".equals(protocol)) {
                    // 处理文件系统中的类
                    try {
                        File directory = new File(resource.toURI());
                        if (directory.exists()) {
                            Set<Class<? extends ToolView>> toolClasses = findClassesInPackage(directory, basePackage);
                            processToolClasses(toolClasses);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("处理文件系统资源异常: " + resource, e);
                    }
                } else if ("jar".equals(protocol)) {
                    // 处理JAR包中的类 (包括某些情况下被识别为file协议的JAR)
                    scanJarFile(resource, basePackage);
                } else {
                    LOGGER.info("不支持的协议类型: " + protocol + ", 资源: " + resource);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Tool扫描异常...", e);
        }
    }

    /**
     * 处理找到的工具类
     */
    private void processToolClasses(Set<Class<? extends ToolView>> toolClasses) {
        for (Class<? extends ToolView> clazz : toolClasses) {
            if (clazz.isAnnotationPresent(Tool.class)) {
                Tool annotation = clazz.getAnnotation(Tool.class);
                ToolCategory category = annotation.category();
                categorizedTools.computeIfAbsent(category, k -> new ArrayList<>()).add(clazz);
            }
        }
    }

    /**
     * 扫描JAR文件中的类
     */
    private void scanJarFile(URL resource, String basePackage) {
        try {
            // 解析jar文件路径
            String resourcePath = resource.getPath();
            String jarPath;

            if (resourcePath.contains("!")) {
                jarPath = resourcePath.substring(0, resourcePath.indexOf("!"));
            } else {
                jarPath = resourcePath;
            }

            // 如果路径以"file:"开头，去掉前缀
            if (jarPath.startsWith("file:")) {
                jarPath = jarPath.substring(5);
            }

            // 处理Windows路径前的额外斜杠
            if (jarPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win") && jarPath.length() > 3 && jarPath.charAt(2) == ':') {
                jarPath = jarPath.substring(1);
            }

            // 使用 URLDecoder 解码路径，以正确处理空格和特殊字符
            String decodedPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
            File jarFile = new File(decodedPath);
            if (!jarFile.exists()) {
                LOGGER.warn("JAR文件不存在: " + jarPath);
                return;
            }

            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            String packagePath = basePackage.replace('.', '/') + "/";

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 检查是否是目标包下的class文件
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class") && !entry.isDirectory()) {
                    String className = entryName.replace('/', '.').replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (ToolView.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                            @SuppressWarnings("unchecked") Class<? extends ToolView> toolClazz = (Class<? extends ToolView>) clazz;
                            Set<Class<? extends ToolView>> toolSet = new HashSet<>();
                            toolSet.add(toolClazz);
                            processToolClasses(toolSet);
                        }
                    } catch (ClassNotFoundException e) {
                        LOGGER.warn("无法加载类: " + className, e);
                    } catch (Exception e) {
                        LOGGER.warn("处理类时发生异常: " + className, e);
                    }
                }
            }
            jar.close();
        } catch (Exception e) {
            LOGGER.warn("扫描JAR文件异常: " + resource.getPath(), e);
        }
    }

    /**
     * 扫描某个目录下所有.class文件，并尝试加载为 ToolView 的子类
     */
    private Set<Class<? extends ToolView>> findClassesInPackage(File directory, String packageName) throws ClassNotFoundException {
        Set<Class<? extends ToolView>> classes = new HashSet<>();
        if (!directory.exists())
            return classes;
        File[] files = directory.listFiles();
        if (files == null)
            return classes;
        for (File file : files) {
            if (file.isDirectory()) {
                // 递归子目录
                String subPackage = packageName + "." + file.getName();
                classes.addAll(findClassesInPackage(file, subPackage));
            } else if (file.getName().endsWith(".class")) {
                // 是一个类文件
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = Class.forName(className);
                if (ToolView.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                    // 是 ToolView 的非抽象实现类
                    @SuppressWarnings("unchecked") Class<? extends ToolView> toolClazz = (Class<? extends ToolView>) clazz;
                    classes.add(toolClazz);
                }
            }
        }
        return classes;
    }

    /**
     * 打印所有已发现工具，按分类分组，用于本地调试显示
     */
    public void listAllTools() {
        if (categorizedTools.isEmpty()) {
            System.out.println("未发现任何工具。请确保工具类在指定包下并且正确使用了 @Tool 注解。");
            return;
        }
        for (Map.Entry<ToolCategory, List<Class<? extends ToolView>>> entry : categorizedTools.entrySet()) {
            ToolCategory category = entry.getKey();
            List<Class<? extends ToolView>> toolClasses = entry.getValue();
            System.out.println("\n=== 分类: " + category + " ===");
            for (Class<? extends ToolView> clazz : toolClasses) {
                Tool tool = clazz.getAnnotation(Tool.class);
                System.out.printf("  - 工具名: %s | 描述: %s | 类: %s%n", tool.name(), tool.description(), clazz.getSimpleName());
            }
        }
    }

    public ToolView getInstance(Class<? extends ToolView> clz) {
        // 第一次检查缓存（无锁）
        ToolView cachedInstance = instanceMap.get(clz);
        if (cachedInstance != null) {
            return cachedInstance;
        }
        try {
            synchronized (this) {
                // 检查缓存中是否已存在实例
                cachedInstance = instanceMap.get(clz);
                if (cachedInstance != null) {
                    return cachedInstance;
                }

                // 创建新实例并缓存
                ToolView newInstance = clz.getDeclaredConstructor().newInstance();
                instanceMap.put(clz, newInstance);
                return newInstance;
            }
        } catch (Exception e) {
            LOGGER.info("无法加载到实例: " + clz.getName(), e);
        }
        return null;
    }

    /**
     * 获取分类后的工具映射（供外部进一步处理）
     */
    public Map<ToolCategory, List<Class<? extends ToolView>>> getAllCategorizedTools() {
        return categorizedTools;
    }
}