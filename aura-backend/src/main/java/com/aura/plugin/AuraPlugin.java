package com.aura.plugin;

import java.util.Map;

/**
 * Aura 插件接口
 * 
 * 所有插件必须实现此接口
 */
public interface AuraPlugin {

    /**
     * 获取插件名称
     */
    String getName();

    /**
     * 获取插件描述
     */
    String getDescription();

    /**
     * 获取插件类型
     */
    default String getType() {
        return "tool";
    }

    /**
     * 获取 Tool Schema（用于 Function Calling）
     * 
     * @return OpenAPI 格式的 Schema JSON
     */
    String getToolSchema();

    /**
     * 执行插件
     * 
     * @param params 参数
     * @return 执行结果
     */
    Object execute(Map<String, Object> params);

    /**
     * 检查插件是否可用
     */
    default boolean isAvailable() {
        return true;
    }
}
