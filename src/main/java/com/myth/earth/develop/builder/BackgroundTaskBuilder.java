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

package com.myth.earth.develop.builder;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 后台任务构建器（可自定义是否EDT中执行）
 *
 * @author zhouchao
 * @date 2026/8/18 下午7:10
 **/
public class BackgroundTaskBuilder {
    private static final Logger                                   logger             = Logger.getInstance(BackgroundTaskBuilder.class);
    private static final int                                      DEFAULT_WAIT_TIME  = 1000;
    // 必需参数
    private final        Project                                  project;
    private final        String                                   title;
    private              Consumer<ProgressIndicator>              taskConsumer;
    // 可选参数 - 任务执行相关
    private              Consumer<ProgressIndicator>              completionConsumer;
    private              BiConsumer<ProgressIndicator, Exception> errorHandler;
    // 可选参数 - 任务配置
    private              boolean                                  cancellable        = true;
    private              int                                      waitTimeMs         = DEFAULT_WAIT_TIME;
    private              boolean                                  taskRunInEdt       = false;
    private              boolean                                  completionRunInEdt = true;

    /**
     * 私有构造函数，通过静态方法创建
     */
    private BackgroundTaskBuilder(Project project, String title) {
        this.project = project;
        this.title = title;
    }

    /**
     * 创建新的构建器实例
     */
    public static BackgroundTaskBuilder create(Project project, @NotNull String title) {
        return new BackgroundTaskBuilder(project, title);
    }

    /**
     * 设置任务执行逻辑（默认独立线程）
     */
    public BackgroundTaskBuilder execute(Consumer<ProgressIndicator> taskConsumer) {
        return execute(taskConsumer, false);
    }

    /**
     * 设置任务执行逻辑
     */
    public BackgroundTaskBuilder execute(Consumer<ProgressIndicator> taskConsumer, boolean runInEdt) {
        this.taskConsumer = taskConsumer;
        this.taskRunInEdt = runInEdt;
        return this;
    }

    /**
     * 设置成功后的处理器（默认 EDT）
     */
    public BackgroundTaskBuilder onComplete(Consumer<ProgressIndicator> completionConsumer) {
        return onComplete(completionConsumer, true);
    }

    /**
     * 设置成功后的处理器
     */
    public BackgroundTaskBuilder onComplete(Consumer<ProgressIndicator> completionConsumer, boolean runInEdt) {
        this.completionConsumer = completionConsumer;
        this.completionRunInEdt = runInEdt;
        return this;
    }

    /**
     * 设置错误处理器
     */
    public BackgroundTaskBuilder onError(BiConsumer<ProgressIndicator, Exception> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * 设置是否可取消
     */
    public BackgroundTaskBuilder cancellable(boolean cancellable) {
        this.cancellable = cancellable;
        return this;
    }

    /**
     * 设置等待时间（毫秒）
     */
    public BackgroundTaskBuilder waitTime(int waitTimeMs) {
        this.waitTimeMs = waitTimeMs;
        return this;
    }

    /**
     * 构建并执行任务
     */
    public void queue() {
        validateConfiguration();
        // 创建上下文
        new Task.Backgroundable(project, title, cancellable) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                Semaphore done = new Semaphore(0);
                Runnable taskRunner = () -> {
                    try {
                        taskConsumer.accept(progressIndicator);
                    } catch (Exception e) {
                        handleError(progressIndicator, e);
                    } finally {
                        done.release();
                    }
                };

                // 选择执行线程
                if (taskRunInEdt) {
                    ApplicationManager.getApplication().invokeLater(taskRunner);
                } else {
                    ApplicationManager.getApplication().executeOnPooledThread(taskRunner);
                }

                // 等待任务完成或取消
                while (true) {
                    try {
                        if (done.tryAcquire(waitTimeMs, TimeUnit.MILLISECONDS)) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    if (cancellable && progressIndicator.isCanceled()) {
                        break;
                    }
                }

                // 任务完成后消费上下文
                if (completionConsumer != null) {
                    if (completionRunInEdt) {
                        ApplicationManager.getApplication().invokeLater(() -> completionConsumer.accept(progressIndicator));
                    } else {
                        completionConsumer.accept(progressIndicator);
                    }
                }
            }
        }.queue();
    }

    /**
     * 验证配置完整性
     */
    private void validateConfiguration() {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (taskConsumer == null) {
            throw new IllegalStateException("Task cannot be null");
        }
    }

    /**
     * 处理错误
     */
    private void handleError(@NotNull ProgressIndicator progressIndicator, Exception e) {
        if (errorHandler != null) {
            errorHandler.accept(progressIndicator, e);
        } else {
            // 默认错误处理
            logger.warn("Background Task [" + title + "] execute error", e);
        }
    }
}