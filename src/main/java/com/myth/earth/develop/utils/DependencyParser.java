/*
 * Copyright (c) 2026 周潮. All rights reserved.
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

package com.myth.earth.develop.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 依赖解析器
 *
 * @author zhouchao
 * @date 2026/6/2 下午6:56
 **/
public class DependencyParser {

    // Maven
    private static final Pattern MAVEN_PATTERN = Pattern.compile("<dependency>[\\s\\S]*?<groupId>(.*?)</groupId>[\\s\\S]*?<artifactId>(.*?)</artifactId>[\\s\\S]*?<version>(.*?)</version>[\\s\\S]*?</dependency>");

    // Gradle Long
    private static final Pattern GRADLE_LONG_PATTERN = Pattern.compile("(\\w+)\\s+group:\\s*'([^']+)',\\s*name:\\s*'([^']+)',\\s*version:\\s*'([^']+)'");

    // Gradle Short
    private static final Pattern GRADLE_SHORT_PATTERN = Pattern.compile("(\\w+)\\s+'([^':]+):([^':]+):([^']+)'");

    public static ParsedDependency parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("依赖内容不能为空");
        }

        Matcher m;

        // Maven
        m = MAVEN_PATTERN.matcher(input);
        if (m.find()) {
            return new ParsedDependency(
                    m.group(1).trim(),
                    m.group(2).trim(),
                    m.group(3).trim(),
                    "compile"
            );
        }

        // Gradle Long
        m = GRADLE_LONG_PATTERN.matcher(input);
        if (m.find()) {
            return new ParsedDependency(
                    m.group(2).trim(),
                    m.group(3).trim(),
                    m.group(4).trim(),
                    m.group(1).trim()
            );
        }

        // Gradle Short
        m = GRADLE_SHORT_PATTERN.matcher(input);
        if (m.find()) {
            return new ParsedDependency(
                    m.group(2).trim(),
                    m.group(3).trim(),
                    m.group(4).trim(),
                    m.group(1).trim()
            );
        }

        throw new IllegalArgumentException("无法识别的依赖格式");
    }

    public static class ParsedDependency {
        public final String groupId;
        public final String artifactId;
        public final String version;
        public final String scope;

        public ParsedDependency(String groupId, String artifactId, String version, String scope) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.scope = scope;
        }
    }
}