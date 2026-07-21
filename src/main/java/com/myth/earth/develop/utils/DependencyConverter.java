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

import com.myth.earth.develop.common.DependencyStyle;

import java.util.Locale;

/**
 * 依赖转换器
 *
 * @author zhouchao
 * @date 2026/6/2 下午6:56
 **/
public class DependencyConverter {

    /**
     * 转换为目标依赖格式
     *
     * @param groupId    groupId
     * @param artifactId artifactId
     * @param version    version
     * @param scope      compile / provided / runtime / test
     * @param style      目标风格
     * @return 转换后的依赖字符串
     */
    public static String convert(String groupId, String artifactId, String version, String scope, DependencyStyle style) {
        if (groupId == null || artifactId == null || version == null) {
            throw new IllegalArgumentException("groupId/artifactId/version 不能为空");
        }

        String safeScope = scope == null ? "compile" : scope.toLowerCase(Locale.ROOT);

        if (style == DependencyStyle.MAVEN) {
            return toMaven(groupId, artifactId, version, safeScope);
        } else if (style == DependencyStyle.GRADLE_GROOVY_LONG) {
            return toGradleLong(groupId, artifactId, version, safeScope);
        } else if (style == DependencyStyle.GRADLE_GROOVY_SHORT) {
            return toGradleShort(groupId, artifactId, version, safeScope);
        }

        throw new IllegalArgumentException("不支持的依赖风格：" + style);
    }

    private static String toMaven(String groupId, String artifactId, String version, String scope) {
        StringBuilder sb = new StringBuilder();
        sb.append("<dependency>\n")
          .append("    <groupId>")
          .append(groupId)
          .append("</groupId>\n")
          .append("    <artifactId>")
          .append(artifactId)
          .append("</artifactId>\n")
          .append("    <version>")
          .append(version)
          .append("</version>\n");

        if (!"compile".equals(scope)) {
            sb.append("    <scope>").append(scope).append("</scope>\n");
        }
        sb.append("</dependency>");
        return sb.toString();
    }

    private static boolean isDefaultMavenScope(String scope) {
        return "compile".equals(scope);
    }


    private static String toGradleLong(String groupId, String artifactId, String version, String scope) {
        String config = mapGradleConfiguration(scope);
        return String.format("%s group: '%s', name: '%s', version: '%s'", config, groupId, artifactId, version);
    }


    private static String toGradleShort(String groupId, String artifactId, String version, String scope) {
        String config = mapGradleConfiguration(scope);
        return String.format("%s '%s:%s:%s'", config, groupId, artifactId, version);
    }

    private static String mapGradleConfiguration(String scope) {
        if ("provided".equals(scope)) {
            return "compileOnly";
        }
        if ("runtime".equals(scope)) {
            return "runtimeOnly";
        }
        if ("test".equals(scope)) {
            return "testImplementation";
        }
        return "implementation";
    }
}