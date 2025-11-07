# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目架构和结构

这是一个Maven多模块项目，`toolkit-parent`作为父项目，管理`toolkit-core`和`toolkit-test`两个子模块。

### `toolkit-core` 模块
包含核心业务逻辑，例如`requestlimiter`限流功能。主要组件包括：
*   `RequestLimiterConfig`: 负责Redis连接和Redisson客户端的配置，以及`ProxyManager`的创建。
*   `RequestLimiterInterceptor`: 实现了`HandlerInterceptor`接口，用于在请求处理前进行限流。
*   `RateLimitProperties`: 用于加载限流相关的配置属性。

### `toolkit-test` 模块
用于测试`toolkit-core`模块的功能。

## 常用命令

*   **构建整个项目:** `mvn clean install`
*   **运行测试:** `mvn test`
*   **跳过测试构建:** `mvn clean install -DskipTests`