package com.aura.plugin.impl;

import com.aura.plugin.AuraPlugin;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 天气查询插件
 * 
 * 获取当前天气信息，用于穿搭推荐
 */
@Slf4j
@Component
public class WeatherPlugin implements AuraPlugin {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WeatherPlugin(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String getName() {
        return "WeatherPlugin";
    }

    @Override
    public String getDescription() {
        return "查询指定城市的天气信息，包括温度、湿度、天气状况等";
    }

    @Override
    public String getToolSchema() {
        return """
                {
                    "name": "WeatherPlugin",
                    "description": "查询指定城市的天气信息，用于推荐合适的穿搭",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "city": {
                                "type": "string",
                                "description": "城市名称，如：北京、上海、广州"
                            }
                        },
                        "required": ["city"]
                    }
                }
                """;
    }

    @Override
    public Object execute(Map<String, Object> params) {
        String city = (String) params.get("city");
        
        if (city == null || city.isBlank()) {
            city = "北京";
        }
        
        log.info("查询天气: city={}", city);

        try {
            // 使用 wttr.in 免费天气API
            String url = "https://wttr.in/" + city + "?format=j1";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Aura/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseWeatherResponse(response.body(), city);
            } else {
                log.warn("天气API返回异常: status={}", response.statusCode());
                return getDefaultWeather(city);
            }

        } catch (Exception e) {
            log.error("天气查询失败: city={}", city, e);
            return getDefaultWeather(city);
        }
    }

    /**
     * 解析天气API响应
     */
    private Map<String, Object> parseWeatherResponse(String responseBody, String city) {
        try {
            Map<String, Object> data = objectMapper.readValue(responseBody, Map.class);
            
            // 提取当前天气
            Map<String, Object> current = (Map<String, Object>) ((List<?>) data.get("current_condition")).get(0);
            
            int tempC = Integer.parseInt((String) current.get("temp_C"));
            int humidity = Integer.parseInt((String) current.get("humidity"));
            String weatherDesc = (String) ((Map<?, ?>) ((List<?>) current.get("weatherDesc")).get(0)).get("value");
            String windSpeed = (String) current.get("windspeedKmph");
            
            // 推荐穿搭风格
            String outfitAdvice = getOutfitAdvice(tempC, weatherDesc);
            
            return Map.of(
                "city", city,
                "temperature", tempC,
                "humidity", humidity,
                "weather", weatherDesc,
                "windSpeed", windSpeed + " km/h",
                "outfitAdvice", outfitAdvice,
                "date", LocalDate.now().toString()
            );
            
        } catch (Exception e) {
            log.error("解析天气数据失败", e);
            return getDefaultWeather(city);
        }
    }

    /**
     * 根据温度和天气给出穿搭建议
     */
    private String getOutfitAdvice(int tempC, String weather) {
        StringBuilder advice = new StringBuilder();
        
        // 温度建议
        if (tempC < 5) {
            advice.append("天气寒冷，建议穿厚外套、羽绒服，注意保暖。");
        } else if (tempC < 15) {
            advice.append("天气较凉，建议穿外套、毛衣，可以叠穿。");
        } else if (tempC < 25) {
            advice.append("温度适宜，建议穿薄外套、长袖衬衫。");
        } else if (tempC < 32) {
            advice.append("天气较热，建议穿短袖、短裤，选择透气面料。");
        } else {
            advice.append("高温天气，建议穿轻薄透气衣物，注意防晒。");
        }
        
        // 天气状况建议
        if (weather.contains("rain") || weather.contains("雨")) {
            advice.append("有雨，记得带伞，选择防水面料。");
        } else if (weather.contains("snow") || weather.contains("雪")) {
            advice.append("有雪，注意防滑，穿保暖防水鞋。");
        } else if (weather.contains("wind") || weather.contains("风")) {
            advice.append("有风，建议穿防风外套。");
        }
        
        return advice.toString();
    }

    /**
     * 获取默认天气（API失败时使用）
     */
    private Map<String, Object> getDefaultWeather(String city) {
        log.info("使用默认天气数据: city={}", city);
        
        return Map.of(
            "city", city,
            "temperature", 22,
            "humidity", 60,
            "weather", "晴",
            "windSpeed", "10 km/h",
            "outfitAdvice", "温度适宜，建议穿薄外套或长袖。",
            "date", LocalDate.now().toString(),
            "note", "天气数据为默认值，请根据实际情况调整穿搭"
        );
    }
}
