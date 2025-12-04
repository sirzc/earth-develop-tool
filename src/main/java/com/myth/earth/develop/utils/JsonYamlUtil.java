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

package com.myth.earth.develop.utils;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;

/**
 * json yaml转换类
 *
 * @author zhouchao
 * @date 2025-12-04 上午10:25
 */
public class JsonYamlUtil {

    /**
     * yaml字符串转json字符串
     *
     * @param yamlString yaml 字符串
     * @return json字符串
     */
    public static List<String> yamlConvertToJson(String yamlString) {
        Yaml yaml = new Yaml();
        //yaml中可以通过 --- 实现同一个yaml中配置多个资源，loadAll会根据 --- 进行拆分，生成多个对象，所以是List
        Iterable<Object> object = yaml.loadAll(yamlString);
        List<String> yamlList = new ArrayList<>();
        object.forEach(y -> {
            if (ObjectUtil.isNotNull(y)) {
                yamlList.add(JSON.toJSONString(y));
            }
        });
        return yamlList;
    }

    /**
     * json字符串转yaml字符串
     *
     * @param jsonString json字符串
     * @return yaml字符串
     */
    public static String jsonConvertToYaml(String jsonString) throws JsonProcessingException {
        JsonNode jsonNode = new ObjectMapper().readTree(jsonString);
        String string = new YAMLMapper().writeValueAsString(jsonNode);
        //因为writeValueAsString生成的yaml字符串会带有 ---\n 所以进行替换操作
        return string.replaceAll("---\n", "");
    }

}
