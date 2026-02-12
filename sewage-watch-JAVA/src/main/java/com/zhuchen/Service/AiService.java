package com.zhuchen.Service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.util.*;

@Service
public class AiService {
    
    @Value("${ai.deepseek.enabled:true}")
    private boolean enabled;
    
    @Value("${ai.deepseek.api-key}")  // ← 对应 yml 的 api-key
    private String apiKey;
    
    @Value("${ai.deepseek.base-url}") // ← 对应 yml 的 base-url  
    private String baseUrl;
    
    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;
    
    /**
     * 提供给Controller调用的方法
     */
    public String getAiReply(String userQuestion) {
        return callRealDeepSeekAPI(userQuestion);
    }
    
    /**
     * 调用真实DeepSeek API
     */
    public String callRealDeepSeekAPI(String userQuestion) {
        System.out.println("=== 开始处理用户问题: " + userQuestion + " ===");
    
    // 测试1: 直接返回，绕过所有逻辑
    // return "【测试模式】这是直接返回，不调用API";
    
    // 测试2: 测试配置
        System.out.println("API Key是否存在: " + (apiKey != null && !apiKey.isEmpty()));
        System.out.println("API Key值: " + (apiKey != null ? "长度=" + apiKey.length() : "null"));
    
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("placeholder")) {
            System.out.println("❌ API密钥未配置或为占位符");
            return getFallbackResponse(userQuestion) + "\n【调试：API密钥未正确配置】";
        }
    
    // 测试3: 简化API调用，只测试连通性
        try {
            System.out.println("尝试调用API，URL: " + baseUrl);
        // 这里可以添加一个最简单的HTTP调用测试
        // ...
        } catch (Exception e) {
            System.out.println("❌ API调用异常: " + e.getClass().getName() + " - " + e.getMessage());
            return getFallbackResponse(userQuestion) + "\n【调试：" + e.getMessage() + "】";
        }
  
        if (!enabled) {
            return "AI服务当前已禁用，请联系管理员。";
        }
        
        try {
            // 1. 构建系统指令（让AI扮演专业角色）
            String systemPrompt = """
                你是一位资深的污水监控与处理专家，具备20年行业经验。
                请根据以下原则回答问题：
                1. 专业准确：使用行业术语（如COD、BOD、氨氮、总磷）
                2. 结构清晰：按"问题分析→风险评估→处理建议"组织回复
                3. 安全第一：涉及化学品的操作必须强调安全防护
                4. 格式美观：适当使用Markdown格式（如**加粗**、列表）
                
                当前时间：%s
                用户问题：""".formatted(new Date());
            
            // 2. 构建API请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1500);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userQuestion));
            requestBody.put("messages", messages);
            
            // 3. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = 
                new HttpEntity<>(requestBody, headers);
            
            // 4. 发送请求（设置超时）
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000); // 连接超时10秒
            factory.setReadTimeout(30000);    // 读取（响应）超时30秒
            RestTemplate restTemplate = new RestTemplate(factory);
            // 暂时注释掉错误处理器
            // restTemplate.setErrorHandler(new MyErrorHandler());

            System.out.println("正在调用DeepSeek API，URL: " + baseUrl);
            // 记录开始时间，用于计算实际耗时
            long apiCallStartTime = System.currentTimeMillis();
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl, HttpMethod.POST, requestEntity, Map.class);
            long apiCallEndTime = System.currentTimeMillis();
            System.out.println("API调用完成，耗时: " + (apiCallEndTime - apiCallStartTime) + "ms");
            System.out.println("HTTP状态码: " + response.getStatusCode());
            // 暂时注释掉，或创建MyErrorHandler类
            // restTemplate.setErrorHandler(new MyErrorHandler());
            
           
            // 5. 解析复杂的JSON响应
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = 
                    (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = 
                        (Map<String, String>) firstChoice.get("message");
                    return message.get("content");
                }
            }
            
            return "未能从AI服务获取有效回复。";
            
        } catch (HttpClientErrorException.Unauthorized e) {
            return "API密钥无效或已过期，请检查配置。";
        } catch (HttpClientErrorException.TooManyRequests e) {
            return "API调用频率超限，请稍后再试。";
        } catch (org.springframework.web.client.ResourceAccessException e) {
         // 这个异常通常包装了连接超时、读取超时等底层I/O错误
            System.err.println("❌ 网络或资源访问异常（可能是超时）: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈，看根本原因
            return getFallbackResponse(userQuestion) + "\n【网络请求失败，请检查超时设置与网络连接】";
        }catch (Exception e) {
            // 异常时降级到模拟回复
            return getFallbackResponse(userQuestion);
        }
    }
    
    /**
     * 降级策略：当真实API失败时，使用智能模拟回复
     * 改为public，以便Controller调用
     */
    public String getFallbackResponse(String question) {
        question = question.toLowerCase();
        
        // 保留原有的关键词匹配逻辑
        if (question.contains("风险") || question.contains("评估")) {
            return "【系统评估（模拟模式）】\n**风险等级**：黄色（中等关注）\n**发现污染物**：3类\n**建议**：24小时内处理，并提交中期评估报告";
        } else if (question.contains("报告") || question.contains("模板")) {
            return "【污水检测报告模板】\n一、概况\n二、检测指标\n三、污染物识别\n四、风险等级\n五、处理建议\n\n*系统提示：真实AI服务暂时不可用，此为模拟回复*";
        } else if (question.contains("塑料") || question.contains("污染物")) {
            return "**污染物识别**：PET塑料瓶（聚对苯二甲酸乙二醇酯）\n**处理建议**：立即物理打捞，分类回收。\n*此为模拟回复*";
        }
        
        return "已收到您的查询。作为污水监控AI，我主要擅长：污染识别、处理工艺、风险评估、标准解读。\n*当前为模拟模式，如需更精准回答，请稍后重试真实AI服务*";
    }
}
