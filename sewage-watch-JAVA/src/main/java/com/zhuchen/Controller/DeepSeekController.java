package com.zhuchen.Controller;

import com.zhuchen.Service.AiService; // 导入AiService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class DeepSeekController {
    
    @Autowired
    private AiService aiService;
    
    /**
     * AI聊天接口
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userQuestion = request.get("question");
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            return createErrorResponse("问题不能为空");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 调用AiService的getAiReply方法
            String aiAnswer = aiService.getAiReply(userQuestion);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("answer", aiAnswer);
            response.put("timestamp", System.currentTimeMillis());
            response.put("response_time_ms", responseTime);
            response.put("source", "deepseek-api");
            return response;
            
        } catch (Exception e) {
            // 如果发生异常，调用getFallbackResponse方法
            String fallbackAnswer = aiService.getFallbackResponse(userQuestion);
            long responseTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("answer", fallbackAnswer);
            response.put("timestamp", System.currentTimeMillis());
            response.put("response_time_ms", responseTime);
            response.put("source", "fallback");
            return response;
        }
    }
    
    /**
     * 健康检查接口（保持不变）
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("service", "Sewage Watch AI Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "API健康检查通过，服务运行正常");
        return response;
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", errorMessage);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
