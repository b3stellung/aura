package com.aura.plugin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 插件管理器
 * 
 * 负责插件的注册、加载和执行
 */
@Slf4j
@Service
public class PluginManager {

    /**
     * 已注册的插件
     */
    private final Map<String, AuraPlugin> plugins = new ConcurrentHashMap<>();

    /**
     * 构造函数，注入所有插件
     */
    public PluginManager(List<AuraPlugin> pluginList) {
        for (AuraPlugin plugin : pluginList) {
            plugins.put(plugin.getName(), plugin);
            log.info("注册插件: {} - {}", plugin.getName(), plugin.getDescription());
        }
        log.info("共注册 {} 个插件", plugins.size());
    }

    /**
     * 获取插件
     */
    public AuraPlugin getPlugin(String name) {
        AuraPlugin plugin = plugins.get(name);
        if (plugin == null) {
            throw new IllegalArgumentException("插件不存在: " + name);
        }
        if (!plugin.isAvailable()) {
            throw new IllegalStateException("插件不可用: " + name);
        }
        return plugin;
    }

    /**
     * 获取所有可用插件
     */
    public List<AuraPlugin> getAvailablePlugins() {
        return plugins.values().stream()
                .filter(AuraPlugin::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户订阅的插件（暂返回所有可用插件）
     * TODO: 后续从数据库查询用户订阅
     */
    public List<AuraPlugin> getUserPlugins(String userId) {
        // 暂时返回所有可用插件
        return getAvailablePlugins();
    }

    /**
     * 执行插件
     */
    public Object executePlugin(String pluginName, Map<String, Object> params) {
        AuraPlugin plugin = getPlugin(pluginName);
        log.info("执行插件: {}，参数: {}", pluginName, params);
        
        long startTime = System.currentTimeMillis();
        try {
            Object result = plugin.execute(params);
            long duration = System.currentTimeMillis() - startTime;
            log.info("插件执行完成: {}，耗时: {}ms", pluginName, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("插件执行失败: {}，耗时: {}ms，错误: {}", pluginName, duration, e.getMessage());
            // 返回错误信息而不是抛异常，让ReAct引擎可以继续推理
            return Map.of("error", e.getMessage(), "plugin", pluginName, "note", "插件执行失败，请基于已有信息继续推理");
        }
    }

    /**
     * 获取插件的 Tool Schema 列表（用于 Function Calling）
     */
    public List<Map<String, Object>> getToolSchemas() {
        return getAvailablePlugins().stream()
                .map(plugin -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> schema = new com.fasterxml.jackson.databind.ObjectMapper()
                                .readValue(plugin.getToolSchema(), Map.class);
                        return schema;
                    } catch (Exception e) {
                        log.error("解析插件 Schema 失败: {}", plugin.getName(), e);
                        return null;
                    }
                })
                .filter(schema -> schema != null)
                .collect(Collectors.toList());
    }
}
