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
package com.myth.earth.develop.helper;

import com.intellij.openapi.util.SystemInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 指令操作助手
 *
 * @author zhouchao
 * @date 2025-07-20 下午8:50
 */
public class CommandHelper {

    public static int getPid(int port) throws Exception {
        if (SystemInfo.isWindows) {
            return getPidByPortOnWindows(port);
        } else if (SystemInfo.isMac || SystemInfo.isLinux) {
            int pidByPortOnUnix = getPidByPortOnUnix(port);
            if (pidByPortOnUnix == -1) {
                return getPidByPortOnUnixWithNetstat(port);
            }
            return pidByPortOnUnix;
        } else {
            throw new UnsupportedOperationException("Unsupported operating system");
        }
    }

    private static int getPidByPortOnWindows(int port) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] {"cmd.exe", "/c", "netstat -ano | findstr " + port});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("LISTENING")) {
                String[] parts = line.trim().split("\\s+");
                return Integer.parseInt(parts[parts.length - 1]);
            }
        }
        return -1;
    }

    private static int getPidByPortOnUnix(int port) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] {"sh", "-c", "lsof -i :" + port + " | grep LISTEN"});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        if (line != null) {
            String[] parts = line.split("\\s+");
            return Integer.parseInt(parts[1]);
        }
        return -1; // 未找到
    }

    private static int getPidByPortOnUnixWithNetstat(int port) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] {"sh", "-c", "netstat -tulnp | grep :" + port});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        if (line != null) {
            String[] parts = line.split("\\s+");
            String pidPart = parts[6].split("/")[0];
            return Integer.parseInt(pidPart);
        }
        return -1; // 未找到
    }

    public static boolean killProcess(int pid) throws Exception {
        if (SystemInfo.isWindows) {
            return killProcessOnWindows(pid);
        } else if (SystemInfo.isMac || SystemInfo.isLinux) {
            return killProcessOnUnix(pid);
        } else {
            throw new UnsupportedOperationException("Unsupported operating system");
        }
    }

    private static boolean killProcessOnWindows(int pid) throws Exception {
        Process process = Runtime.getRuntime().exec("taskkill /F /PID " + pid);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("SUCCESS")) {
                return true;
            }
        }
        return false;
    }

    private static boolean killProcessOnUnix(int pid) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[] {"sh", "-c", "kill -9 " + pid});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        if (line == null) {
            return true; // 成功时没有输出
        }
        return !line.contains("No such process");
    }
}
