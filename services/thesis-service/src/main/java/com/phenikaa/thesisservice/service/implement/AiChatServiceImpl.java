package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.client.ProfileServiceClient;
import com.phenikaa.thesisservice.dto.request.ChatRequest;
import com.phenikaa.thesisservice.dto.response.ChatResponse;
import com.phenikaa.thesisservice.entity.LecturerCapacity;
import com.phenikaa.thesisservice.repository.LecturerCapacityRepository;
import com.phenikaa.thesisservice.service.interfaces.AiChatService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final LecturerCapacityRepository lecturerCapacityRepository;
    private final ProfileServiceClient profileServiceClient;

    @Value("${ai.gemini.api-key:AIzaSyCUuAhB8wbCCCqrhlTXT83Sbe5c17GTJlU}")
    private String geminiApiKey;

    private ChatModel getChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.0-flash")
                .temperature(0.7)
                .build();
    }

    @Override
    public ChatResponse processChatMessage(ChatRequest request) {
        try {
            String userMessage = request.getMessage().toLowerCase();
            ChatModel model = getChatModel();

            // Kiểm tra các từ khóa đơn giản trước khi gọi AI
            String intent = analyzeIntentSimple(userMessage);
            if (intent == null) {
                // Nếu không xác định được bằng logic đơn giản, dùng AI
                intent = analyzeIntent(userMessage, model);
            }
            
            log.info("Detected intent: {} for message: {}", intent, userMessage);
            
            switch (intent) {
                case "topic_suggestion":
                    return suggestTopics(request.getMessage(), extractSpecialization(userMessage));
                case "lecturer_search":
                    return findSuitableLecturers(request.getMessage(), extractSpecialization(userMessage));
                case "capacity_check":
                    Integer lecturerId = extractLecturerId(userMessage);
                    log.info("Extracted lecturer ID: {}", lecturerId);
                    
                    if (lecturerId == null) {
                        // Thử tìm theo tên nếu không tìm thấy ID
                        String lecturerName = extractLecturerName(userMessage);
                        log.info("Extracted lecturer name: '{}'", lecturerName);
                        
                        if (lecturerName != null) {
                            lecturerId = findLecturerIdByName(lecturerName);
                            log.info("Found lecturer ID by name '{}': {}", lecturerName, lecturerId);
                        }
                    }
                    return checkLecturerCapacity(lecturerId);
                case "general_help":
                    return getGeneralHelp();
                default:
                    return handleGeneralQuery(request, model);
            }
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.")
                    .sessionId(request.getSessionId())
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public ChatResponse suggestTopics(String userMessage, String specialization) {
        try {
            ChatModel model = getChatModel();
            
            String prompt = String.format("""
                    Bạn là một AI chuyên gia tư vấn đề tài luận văn tốt nghiệp với hơn 10 năm kinh nghiệm hướng dẫn sinh viên.
                    
                    **THÔNG TIN SINH VIÊN:**
                    - Yêu cầu: %s
                    - Chuyên ngành: %s
                    
                    **YÊU CẦU GỢI Ý:**
                    Hãy TẠO MỚI 3-5 đề tài luận văn CHẤT LƯỢNG CAO, hoàn toàn phù hợp với yêu cầu và chuyên ngành của sinh viên.
                    KHÔNG sử dụng đề tài có sẵn, hãy tạo ra những đề tài mới, sáng tạo và thực tế.
                    
                    **TIÊU CHÍ ĐÁNH GIÁ:**
                    - Tính thực tiễn và ứng dụng cao trong ngành Công nghệ thông tin
                    - Phù hợp với trình độ sinh viên đại học năm cuối
                    - Có tài liệu tham khảo phong phú và dễ tìm
                    - Khả năng hoàn thành trong 4-6 tháng
                    - Đóng góp giá trị thực tế cho doanh nghiệp/xã hội
                    - Cập nhật xu hướng công nghệ hiện tại (AI, Cloud, Mobile, Web, IoT, etc.)
                    
                    **THÔNG TIN MỖI ĐỀ TÀI:**
                    - **Tiêu đề**: Rõ ràng, cụ thể, hấp dẫn, phản ánh đúng nội dung
                    - **Mô tả**: Chi tiết về vấn đề cần giải quyết, tầm quan trọng, đối tượng nghiên cứu
                    - **Mục tiêu**: 3-4 mục tiêu cụ thể, đo lường được, thực tế
                    - **Phương pháp**: Nghiên cứu lý thuyết + thực nghiệm, khảo sát, phát triển hệ thống
                    - **Mức độ khó**: EASY (dễ), MEDIUM (trung bình), HARD (khó)
                    - **Kết quả mong đợi**: Sản phẩm cụ thể (ứng dụng, hệ thống, thuật toán, etc.)
                    - **Công nghệ sử dụng**: Java, Python, React, Node.js, Spring Boot, MySQL, MongoDB, etc.
                    - **Lý do chọn**: Tại sao đề tài này phù hợp với yêu cầu của sinh viên
                    
                    **ĐỊNH DẠNG TRẢ VỀ:**
                    Trả về KẾT QUẢ DUY NHẤT dưới dạng JSON HỢP LỆ (KHÔNG có giải thích kèm theo, KHÔNG markdown, KHÔNG văn bản dư thừa).
                    Chỉ trả về một object JSON có cấu trúc chính xác:
                    {
                      "suggestions": [
                        {
                          "title": "...",
                          "description": "...",
                          "objectives": "...",
                          "methodology": "...",
                          "difficultyLevel": "EASY|MEDIUM|HARD",
                          "expectedOutcome": "...",
                          "technologies": "...",
                          "reason": "..."
                        }
                      ]
                    }
                    """, userMessage, specialization);

            UserMessage userMsg = UserMessage.from(prompt);
            AiMessage response = model.chat(userMsg).aiMessage();
            String responseText = response.text();
            
            log.info("AI generated topics response: {}", responseText);
            
            // Parse response và tạo topic suggestions
            List<ChatResponse.TopicSuggestion> suggestions = parseTopicSuggestions(responseText);
            
            return ChatResponse.builder()
                    .message("**🎓 GỢI Ý ĐỀ TÀI LUẬN VĂN MỚI**\n\nDựa trên yêu cầu của bạn và chuyên ngành \"" + specialization + "\", tôi đã tạo ra các đề tài phù hợp:\n\n" + responseText)
                    .sessionId("")
                    .topicSuggestions(suggestions)
                    .responseType("topic_suggestion")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error suggesting topics: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi không thể gợi ý đề tài lúc này. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public ChatResponse findSuitableLecturers(String userMessage, String specialization) {
        try {
            // Lấy danh sách giảng viên có capacity
            List<LecturerCapacity> availableLecturers = lecturerCapacityRepository.findAll()
                    .stream()
                    .filter(LecturerCapacity::canAcceptMoreStudents)
                    .collect(Collectors.toList());

            if (availableLecturers.isEmpty()) {
                return ChatResponse.builder()
                        .message("""
                                **THÔNG BÁO VỀ CAPACITY GIẢNG VIÊN**
                                
                                Hiện tại không có giảng viên nào có chỗ trống để hướng dẫn sinh viên mới.
                                
                                **GỢI Ý:**
                                - Hãy thử lại sau vài ngày
                                - Liên hệ trực tiếp với phòng đào tạo để được tư vấn
                                - Có thể đăng ký vào đợt đăng ký tiếp theo
                                
                                **LIÊN HỆ HỖ TRỢ:**
                                - Email: support@university.edu.vn
                                - Hotline: 1900-xxxx
                                """)
                        .sessionId("")
                        .responseType("lecturer_suggestion")
                        .build();
            }

            // Tạo danh sách gợi ý giảng viên
            List<ChatResponse.LecturerSuggestion> suggestions = new ArrayList<>();
            
            for (LecturerCapacity capacity : availableLecturers) {
                try {
                    // Gọi API profile service để lấy thông tin giảng viên
                    Map<String, Object> lecturerInfo = profileServiceClient.getLecturerById(capacity.getLecturerId());
                    
                    ChatResponse.LecturerSuggestion suggestion = ChatResponse.LecturerSuggestion.builder()
                            .lecturerId(capacity.getLecturerId())
                            .lecturerName((String) lecturerInfo.get("fullName"))
                            .specialization((String) lecturerInfo.get("specialization"))
                            .remainingCapacity(capacity.getRemainingSlots())
                            .phone((String) lecturerInfo.get("phoneNumber"))
                            .build();
                    
                    suggestions.add(suggestion);
                } catch (Exception e) {
                    log.warn("Could not fetch lecturer info for ID {}: {}", capacity.getLecturerId(), e.getMessage());
                }
            }

            String message = String.format("""
                    **DANH SÁCH GIẢNG VIÊN PHÙ HỢP**
                    
                    Dựa trên yêu cầu của bạn về "%s" và chuyên ngành "%s", tôi đã tìm thấy %d giảng viên có chỗ trống:
                    
                    **THÔNG TIN CHI TIẾT:**
                    """, userMessage, specialization, suggestions.size());

            return ChatResponse.builder()
                    .message(message)
                    .sessionId("")
                    .lecturerSuggestions(suggestions)
                    .responseType("lecturer_suggestion")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error finding suitable lecturers: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("""
                            **LỖI HỆ THỐNG**
                            
                            Xin lỗi, tôi gặp khó khăn khi tìm kiếm giảng viên phù hợp lúc này.
                            
                            **NGUYÊN NHÂN CÓ THỂ:**
                            - Lỗi kết nối cơ sở dữ liệu
                            - Dịch vụ tạm thời không khả dụng
                            - Lỗi xử lý thông tin
                            
                            **GỢI Ý:**
                            - Vui lòng thử lại sau ít phút
                            - Liên hệ trực tiếp với phòng đào tạo
                            - Sử dụng chức năng tìm kiếm khác
                            """)
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public ChatResponse checkLecturerCapacity(Integer lecturerId) {
        try {
            if (lecturerId == null) {
                return ChatResponse.builder()
                        .message("""
                                **THÔNG BÁO**
                                
                                Không thể xác định giảng viên cần kiểm tra capacity.
                                
                                **GỢI Ý:**
                                - Sử dụng ID giảng viên: "Kiểm tra capacity giảng viên ID 1"
                                - Hoặc tên giảng viên: "Giảng viên Hoàng Thiên Bảo có thể nhận thêm bao nhiêu sinh viên?"
                                - Liên hệ phòng đào tạo để được hỗ trợ
                                """)
                        .sessionId("")
                        .responseType("capacity_check")
                        .build();
            }
            
            LecturerCapacity capacity = lecturerCapacityRepository.findByLecturerId(lecturerId)
                    .orElse(null);
            
            if (capacity == null) {
                return ChatResponse.builder()
                        .message("""
                                **THÔNG BÁO**
                                
                                Không tìm thấy thông tin capacity của giảng viên ID %d.
                                
                                **GỢI Ý:**
                                - Kiểm tra lại ID giảng viên
                                - Liên hệ phòng đào tạo để được hỗ trợ
                                - Sử dụng chức năng tìm kiếm giảng viên
                                """.formatted(lecturerId))
                        .sessionId("")
                        .responseType("capacity_check")
                        .build();
            }

            String message = String.format("""
                    **THÔNG TIN CAPACITY GIẢNG VIÊN**
                    
                    **Giảng viên ID:** %d
                    **Số lượng tối đa:** %d sinh viên
                    **Đã nhận:** %d sinh viên
                    **Còn trống:** %d chỗ
                    
                    **Trạng thái:** %s
                    """, 
                    lecturerId,
                    capacity.getMaxStudents(),
                    capacity.getCurrentStudents(),
                    capacity.getRemainingSlots(),
                    capacity.canAcceptMoreStudents() ? "Có thể nhận thêm sinh viên" : "Đã đủ sinh viên"
            );

            return ChatResponse.builder()
                    .message(message)
                    .sessionId("")
                    .responseType("capacity_check")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error checking lecturer capacity: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("""
                            **LỖI HỆ THỐNG**
                            
                            Xin lỗi, tôi không thể kiểm tra capacity của giảng viên lúc này.
                            
                            **NGUYÊN NHÂN:**
                            - Lỗi kết nối cơ sở dữ liệu
                            - Dịch vụ tạm thời không khả dụng
                            
                            **GỢI Ý:**
                            - Vui lòng thử lại sau ít phút
                            - Liên hệ trực tiếp với phòng đào tạo
                            """)
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public ChatResponse getGeneralHelp() {
        String helpMessage = """
                **XIN CHÀO! TÔI LÀ AI TRỢ LÝ TƯ VẤN LUẬN VĂN TỐT NGHIỆP**
                
                Tôi được thiết kế đặc biệt để hỗ trợ sinh viên trong quá trình làm luận văn tốt nghiệp. Dưới đây là những gì tôi có thể giúp bạn:
                
                **1. GỢI Ý ĐỀ TÀI LUẬN VĂN CHI TIẾT**
                   Phân tích sở thích và chuyên ngành của bạn
                   Gợi ý đề tài phù hợp với trình độ và thời gian
                   Cung cấp mục tiêu nghiên cứu cụ thể
                   Đề xuất phương pháp nghiên cứu phù hợp
                   Đánh giá mức độ khó (EASY/MEDIUM/HARD)
                   Dự đoán kết quả mong đợi
                   
                   **Ví dụ câu hỏi:**
                   - "Tôi muốn làm đề tài về trí tuệ nhân tạo"
                   - "Gợi ý đề tài về blockchain cho sinh viên CNTT"
                   - "Tôi thích lập trình web, có đề tài nào phù hợp?"
                
                **2. TÌM GIẢNG VIÊN PHÙ HỢP THÔNG MINH**
                   Tìm kiếm theo chuyên môn và lĩnh vực nghiên cứu
                   Kiểm tra capacity còn trống của giảng viên
                   Cung cấp thông tin liên hệ chi tiết
                   Đánh giá mức độ phù hợp với đề tài
                   Gợi ý giảng viên có kinh nghiệm phù hợp
                   
                   **Ví dụ câu hỏi:**
                   - "Tìm giảng viên chuyên về machine learning"
                   - "Ai có thể hướng dẫn tôi về database?"
                   - "Giảng viên nào có kinh nghiệm về AI?"
                
                **3. KIỂM TRA CAPACITY GIẢNG VIÊN**
                   Xem số lượng sinh viên hiện tại
                   Kiểm tra chỗ trống còn lại
                   Thông tin chi tiết về capacity
                   Cập nhật real-time
                   
                   **Ví dụ câu hỏi:**
                   - "Kiểm tra capacity giảng viên ID 1"
                   - "Giảng viên ABC còn nhận được bao nhiêu sinh viên?"
                
                **4. TƯ VẤN CHUNG TOÀN DIỆN**
                   Hướng dẫn quy trình đăng ký luận văn
                   Giải thích yêu cầu và tiêu chí đánh giá
                   Tư vấn cách chọn đề tài phù hợp
                   Hướng dẫn viết đề cương nghiên cứu
                   Giải đáp thắc mắc về timeline và deadline
                   Tư vấn về phương pháp nghiên cứu
                   Hướng dẫn cách trình bày và bảo vệ
                   
                   **Ví dụ câu hỏi:**
                   - "Quy trình đăng ký luận văn như thế nào?"
                   - "Làm sao để viết đề cương nghiên cứu tốt?"
                   - "Timeline làm luận văn trong bao lâu?"
                   - "Cần chuẩn bị gì cho buổi bảo vệ?"
                
                **CÁCH SỬ DỤNG HIỆU QUẢ:**
                   • Gõ câu hỏi tự nhiên bằng tiếng Việt
                   • Mô tả chi tiết yêu cầu để nhận được gợi ý tốt nhất
                   • Có thể hỏi theo nhiều cách khác nhau
                   • Tôi sẽ tự động hiểu và phân loại câu hỏi
                
                **BẮT ĐẦU NGAY:**
                   Hãy cho tôi biết bạn đang gặp khó khăn gì hoặc cần hỗ trợ về vấn đề nào, tôi sẽ giúp bạn giải quyết một cách chi tiết và hiệu quả nhất!
                """;

        return ChatResponse.builder()
                .message(helpMessage)
                .sessionId("")
                .responseType("general_help")
                .build();
    }

    private String analyzeIntentSimple(String message) {
        // Kiểm tra các từ khóa đơn giản trước
        if (message.contains("bạn có thể") || message.contains("bạn làm gì") || 
            message.contains("giúp gì") || message.contains("chức năng") ||
            message.contains("làm gì") || message.contains("có gì")) {
            return "general_help";
        }
        
        // Kiểm tra capacity cụ thể của giảng viên (ưu tiên cao nhất)
        // Pattern: "Giảng viên [Tên] + [capacity keywords]"
        if (isCapacityCheckPattern(message)) {
            return "capacity_check";
        }
        
        // Kiểm tra tìm kiếm giảng viên chung (chỉ khi không có tên cụ thể)
        if (isGeneralLecturerSearchPattern(message)) {
            return "lecturer_search";
        }
        
        // Kiểm tra gợi ý đề tài (ưu tiên cao hơn general_help)
        if (isTopicSuggestionPattern(message)) {
            return "topic_suggestion";
        }
        
        if (message.contains("giảng viên") || message.contains("thầy") || 
            message.contains("cô") || message.contains("lecturer") ||
            message.contains("hướng dẫn")) {
            return "lecturer_search";
        }
        
        return null; // Không xác định được, dùng AI
    }
    
    private boolean isTopicSuggestionPattern(String message) {
        // Kiểm tra các từ khóa liên quan đến đề tài
        boolean hasTopicKeywords = message.contains("đề tài") || 
                                  message.contains("gợi ý") || 
                                  message.contains("suggest") || 
                                  message.contains("topic") ||
                                  message.contains("muốn làm") ||
                                  message.contains("muốn phát triển") ||
                                  message.contains("muốn xây dựng") ||
                                  message.contains("muốn tạo") ||
                                  message.contains("ứng dụng") ||
                                  message.contains("hệ thống") ||
                                  message.contains("website") ||
                                  message.contains("mobile") ||
                                  message.contains("web") ||
                                  message.contains("app") ||
                                  message.contains("ai") ||
                                  message.contains("machine learning") ||
                                  message.contains("blockchain") ||
                                  message.contains("iot") ||
                                  message.contains("database") ||
                                  message.contains("game") ||
                                  message.contains("quản lý") ||
                                  message.contains("thương mại điện tử") ||
                                  message.contains("ecommerce");
        
        // Không phải câu hỏi về chức năng chung
        boolean notGeneralHelp = !message.contains("bạn có thể") && 
                                !message.contains("bạn làm gì") && 
                                !message.contains("giúp gì") && 
                                !message.contains("chức năng") &&
                                !message.contains("làm gì") && 
                                !message.contains("có gì");
        
        return hasTopicKeywords && notGeneralHelp;
    }
    
    private boolean isCapacityCheckPattern(String message) {
        // Kiểm tra có tên giảng viên cụ thể không
        boolean hasLecturerName = message.matches(".*giảng viên\\s+[^\\s]+.*") ||
                                 message.matches(".*thầy\\s+[^\\s]+.*") ||
                                 message.matches(".*cô\\s+[^\\s]+.*");
        
        // Kiểm tra có từ khóa capacity không
        boolean hasCapacityKeywords = message.contains("có thể nhận thêm") || 
                                     message.contains("nhận thêm bao nhiêu") ||
                                     message.contains("capacity") || 
                                     message.contains("có thể nhận") || 
                                     message.contains("còn bao nhiêu chỗ") ||
                                     message.contains("còn chỗ") ||
                                     message.contains("có chỗ") ||
                                     message.contains("bao nhiêu chỗ") ||
                                     message.contains("nhận sinh viên") ||
                                     message.contains("còn trống") ||
                                     message.contains("có trống") ||
                                     message.contains("chỗ trống");
        
        return hasLecturerName && hasCapacityKeywords;
    }
    
    private boolean isGeneralLecturerSearchPattern(String message) {
        // Chỉ tìm kiếm chung khi KHÔNG có tên cụ thể
        boolean hasGeneralKeywords = message.contains("giảng viên nào") || 
                                    message.contains("thầy nào") || 
                                    message.contains("cô nào") || 
                                    message.contains("ai có thể") ||
                                    message.contains("giảng viên có") || 
                                    message.contains("thầy có") ||
                                    message.contains("cô có") ||
                                    message.contains("còn chỗ trống") ||
                                    message.contains("chỗ trống") ||
                                    message.contains("nhận sinh viên");
        
        // Không có tên cụ thể
        boolean noSpecificName = !message.matches(".*giảng viên\\s+[^\\s]+.*") &&
                                !message.matches(".*thầy\\s+[^\\s]+.*") &&
                                !message.matches(".*cô\\s+[^\\s]+.*");
        
        return hasGeneralKeywords && noSpecificName;
    }

    private String analyzeIntent(String message, ChatModel model) {
        try {
            String prompt = String.format("""
                    Phân tích ý định của người dùng từ tin nhắn: "%s"
                    
                    Các ý định có thể:
                    - topic_suggestion: Muốn gợi ý đề tài luận văn
                    - lecturer_search: Muốn tìm giảng viên phù hợp
                    - capacity_check: Muốn kiểm tra capacity giảng viên
                    - general_help: Cần trợ giúp chung
                    
                    Chỉ trả về một trong các từ khóa trên.
                    """, message);

            UserMessage userMsg = UserMessage.from(prompt);
            AiMessage response = model.chat(userMsg).aiMessage();
            String result = response.text().trim();
            
            // Fallback nếu AI không trả về đúng format
            if (!result.equals("topic_suggestion") && !result.equals("lecturer_search") && 
                !result.equals("capacity_check") && !result.equals("general_help")) {
                return "general_help";
            }
            
            return result;
        } catch (Exception e) {
            log.warn("Error analyzing intent with AI, falling back to general_help: {}", e.getMessage());
            return "general_help";
        }
    }

    private String extractSpecialization(String message) {
        // Logic đơn giản để extract chuyên ngành từ message
        if (message.contains("công nghệ thông tin") || message.contains("cntt")) {
            return "Công nghệ thông tin";
        } else if (message.contains("kỹ thuật phần mềm") || message.contains("ktpm")) {
            return "Kỹ thuật phần mềm";
        } else if (message.contains("khoa học máy tính") || message.contains("khmt")) {
            return "Khoa học máy tính";
        } else if (message.contains("mạng máy tính") || message.contains("mmt")) {
            return "Mạng máy tính";
        }
        return "Công nghệ thông tin"; // Default
    }

    private Integer extractLecturerId(String message) {
        // Logic đơn giản để extract lecturer ID từ message
        try {
            String[] words = message.split("\\s+");
            for (String word : words) {
                if (word.matches("\\d+")) {
                    return Integer.parseInt(word);
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract lecturer ID from message: {}", message);
        }
        return null;
    }
    
    private String extractLecturerName(String message) {
        // Logic đơn giản để extract tên giảng viên từ message
        try {
            log.info("Extracting lecturer name from message: '{}'", message);
            
            // Tìm pattern "Giảng viên [Tên]" - dừng khi gặp "có thể" hoặc "capacity"
            String[] patterns = {
                "giảng viên\\s+([^\\s]+(?:\\s+[^\\s]+)*?)(?=\\s+có thể|\\s+capacity|\\s+còn|\\s+nhận|\\s+bao nhiêu|$)",
                "thầy\\s+([^\\s]+(?:\\s+[^\\s]+)*?)(?=\\s+có thể|\\s+capacity|\\s+còn|\\s+nhận|\\s+bao nhiêu|$)",
                "cô\\s+([^\\s]+(?:\\s+[^\\s]+)*?)(?=\\s+có thể|\\s+capacity|\\s+còn|\\s+nhận|\\s+bao nhiêu|$)"
            };
            
            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher m = p.matcher(message);
                if (m.find()) {
                    String extractedName = m.group(1).trim();
                    log.info("Extracted name using pattern '{}': '{}'", pattern, extractedName);
                    return extractedName;
                }
            }
            
            // Thử pattern khác: tìm tên trước "có thể nhận" hoặc "capacity"
            String[] alternativePatterns = {
                "([^\\s]+(?:\\s+[^\\s]+)*?)\\s+có thể nhận",
                "([^\\s]+(?:\\s+[^\\s]+)*?)\\s+capacity",
                "([^\\s]+(?:\\s+[^\\s]+)*?)\\s+còn chỗ"
            };
            
            for (String pattern : alternativePatterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher m = p.matcher(message);
                if (m.find()) {
                    String extractedName = m.group(1).trim();
                    // Loại bỏ các từ không phải tên
                    if (!extractedName.toLowerCase().contains("giảng viên") && 
                        !extractedName.toLowerCase().contains("thầy") && 
                        !extractedName.toLowerCase().contains("cô") &&
                        !extractedName.toLowerCase().contains("có thể") &&
                        !extractedName.toLowerCase().contains("capacity") &&
                        !extractedName.toLowerCase().contains("nhận") &&
                        !extractedName.toLowerCase().contains("bao nhiêu") &&
                        extractedName.length() > 2) {
                        log.info("Extracted name using alternative pattern '{}': '{}'", pattern, extractedName);
                        return extractedName;
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("Could not extract lecturer name from message: {}", message, e);
        }
        return null;
    }
    
    private Integer findLecturerIdByName(String lecturerName) {
        try {
            log.info("Searching for lecturer with name: '{}'", lecturerName);
            
            // Tìm kiếm giảng viên theo tên trong danh sách có capacity
            List<LecturerCapacity> allCapacities = lecturerCapacityRepository.findAll();
            log.info("Found {} lecturer capacities to search", allCapacities.size());
            
            for (LecturerCapacity capacity : allCapacities) {
                try {
                    Map<String, Object> lecturerInfo = profileServiceClient.getLecturerById(capacity.getLecturerId());
                    String fullName = (String) lecturerInfo.get("fullName");
                    
                    log.info("Checking lecturer ID {} with name: '{}'", capacity.getLecturerId(), fullName);
                    
                    if (fullName != null) {
                        // So sánh chính xác (case insensitive)
                        if (fullName.toLowerCase().equals(lecturerName.toLowerCase())) {
                            log.info("Exact match found: {} -> ID {}", fullName, capacity.getLecturerId());
                            return capacity.getLecturerId();
                        }
                        
                        // So sánh chứa tên (case insensitive)
                        if (fullName.toLowerCase().contains(lecturerName.toLowerCase())) {
                            log.info("Partial match found: {} contains {} -> ID {}", fullName, lecturerName, capacity.getLecturerId());
                            return capacity.getLecturerId();
                        }
                        
                        // Tách tên thành các từ và so sánh
                        String[] nameParts = lecturerName.toLowerCase().split("\\s+");
                        String[] fullNameParts = fullName.toLowerCase().split("\\s+");
                        
                        boolean allPartsMatch = true;
                        for (String part : nameParts) {
                            boolean partFound = false;
                            for (String fullPart : fullNameParts) {
                                if (fullPart.contains(part) || part.contains(fullPart)) {
                                    partFound = true;
                                    break;
                                }
                            }
                            if (!partFound) {
                                allPartsMatch = false;
                                break;
                            }
                        }
                        
                        if (allPartsMatch) {
                            log.info("Word-based match found: {} matches {} -> ID {}", fullName, lecturerName, capacity.getLecturerId());
                            return capacity.getLecturerId();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch lecturer info for ID {}: {}", capacity.getLecturerId(), e.getMessage());
                }
            }
            
            log.warn("No lecturer found with name: '{}'", lecturerName);
        } catch (Exception e) {
            log.error("Error finding lecturer by name: {}", e.getMessage(), e);
        }
        return null;
    }

    private List<ChatResponse.TopicSuggestion> parseTopicSuggestions(String response) {
        List<ChatResponse.TopicSuggestion> suggestions = new ArrayList<>();
        try {
            // Chuẩn hóa nếu AI trả về theo dạng ```json ... ``` hoặc có markdown
            String normalized = response;
            if (normalized != null && normalized.contains("```")) {
                normalized = normalized
                        .replace("```json", "```")
                        .replace("```JSON", "```")
                        .replace("```", "\n");
            }

            String source = normalized != null ? normalized : response;
            if (source == null) source = "";

            // Ưu tiên trích xuất mảng JSON nếu có
            int arrStart = source.indexOf("[");
            int arrEnd = source.lastIndexOf("]") + 1;
            int objStart = source.indexOf("{");
            int objEnd = source.lastIndexOf("}") + 1;

            String jsonString = null;
            if (arrStart >= 0 && arrEnd > arrStart) {
                jsonString = source.substring(arrStart, arrEnd).trim();
            } else if (objStart >= 0 && objEnd > objStart) {
                jsonString = source.substring(objStart, objEnd).trim();
            }

            if (jsonString != null) {
                log.info("Extracted JSON: {}", jsonString);
                suggestions = parseJsonTopics(jsonString);
            }

            if (suggestions.isEmpty()) {
                log.warn("Could not parse JSON from AI response, returning empty suggestions");
            }
        } catch (Exception e) {
            log.error("Error parsing topic suggestions: {}", e.getMessage(), e);
        }
        return suggestions;
    }
    
    private List<ChatResponse.TopicSuggestion> parseJsonTopics(String jsonString) {
        List<ChatResponse.TopicSuggestion> suggestions = new ArrayList<>();
        try {
            // Thử dùng Jackson nếu có trên classpath
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonString);
                com.fasterxml.jackson.databind.JsonNode arr;
                if (root.isArray()) {
                    arr = root;
                } else {
                    arr = root.get("suggestions");
                }
                if (arr != null && arr.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : arr) {
                        suggestions.add(
                                ChatResponse.TopicSuggestion.builder()
                                        .title(node.path("title").asText(""))
                                        .description(node.path("description").asText(""))
                                        .objectives(node.path("objectives").isArray() ? node.path("objectives").toString() : node.path("objectives").asText(""))
                                        .methodology(node.path("methodology").asText(""))
                                        .difficultyLevel(node.path("difficultyLevel").asText("MEDIUM"))
                                        .expectedOutcome(node.path("expectedOutcome").asText(""))
                                        .technologies(node.path("technologies").isArray() ? node.path("technologies").toString() : node.path("technologies").asText(""))
                                        .reason(node.path("reason").asText(""))
                                        .build()
                        );
                    }
                    return suggestions;
                }
            } catch (Throwable ignore) {
                // bỏ qua để fallback regex bên dưới
            }

            // Fallback đơn giản bằng regex thô
            String[] topicBlocks = jsonString.split("\\{\\s*\\\"title\\\"");
            for (String block : topicBlocks) {
                if (!block.contains("\"")) continue;
                String segment = "{\"title\"" + block;
                String title = extractField(segment, "title");
                if (title == null || title.isEmpty()) continue;
                String description = extractField(segment, "description");
                String objectives = extractField(segment, "objectives");
                String methodology = extractField(segment, "methodology");
                String difficultyLevel = extractField(segment, "difficultyLevel");
                String expectedOutcome = extractField(segment, "expectedOutcome");
                String technologies = extractField(segment, "technologies");
                String reason = extractField(segment, "reason");
                suggestions.add(ChatResponse.TopicSuggestion.builder()
                        .title(title)
                        .description(description != null ? description : "")
                        .objectives(objectives != null ? objectives : "")
                        .methodology(methodology != null ? methodology : "")
                        .difficultyLevel(difficultyLevel != null ? difficultyLevel : "MEDIUM")
                        .expectedOutcome(expectedOutcome != null ? expectedOutcome : "")
                        .technologies(technologies != null ? technologies : "")
                        .reason(reason != null ? reason : "")
                        .build());
            }
        } catch (Exception e) {
            log.error("Error parsing JSON topics: {}", e.getMessage(), e);
        }
        return suggestions;
    }
    
    private String extractField(String text, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1).trim();
            }
        } catch (Exception e) {
            log.debug("Could not extract field {}: {}", fieldName, e.getMessage());
        }
        return null;
    }

    private ChatResponse handleGeneralQuery(ChatRequest request, ChatModel model) {
        try {
            String prompt = String.format("""
                    Bạn là AI trợ lý tư vấn chuyên nghiệp cho hệ thống quản lý luận văn tốt nghiệp của trường đại học.
                    
                    **VAI TRÒ CỦA BẠN:**
                    - Tư vấn toàn diện về quá trình làm luận văn tốt nghiệp
                    - Hướng dẫn chi tiết từ A-Z về luận văn
                    - Giải đáp mọi thắc mắc của sinh viên
                    - Cung cấp lời khuyên thực tế và hữu ích
                    
                    **NGUYÊN TẮC TRẢ LỜI:**
                    - Trả lời chi tiết, cụ thể và dễ hiểu
                    - Sử dụng ngôn ngữ thân thiện, chuyên nghiệp
                    - Cung cấp ví dụ thực tế khi cần thiết
                    - Hướng dẫn từng bước cụ thể
                    - Khuyến khích và động viên sinh viên
                    
                    **CÂU HỎI CỦA SINH VIÊN:** %s
                    
                    **YÊU CẦU:**
                    - Trả lời đầy đủ và chi tiết câu hỏi trên
                    - Nếu liên quan đến đề tài, giảng viên, hoặc quy trình đăng ký, hãy hướng dẫn sinh viên sử dụng các chức năng cụ thể
                    - Cung cấp thông tin bổ sung hữu ích
                    - Kết thúc bằng lời khuyên hoặc gợi ý tiếp theo
                    """, request.getMessage());

            UserMessage userMsg = UserMessage.from(prompt);
            AiMessage response = model.chat(userMsg).aiMessage();
            String responseText = response.text();
            
            return ChatResponse.builder()
                    .message(responseText)
                    .sessionId(request.getSessionId())
                    .responseType("general")
                    .build();
        } catch (Exception e) {
            log.error("Error handling general query: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi gặp khó khăn khi xử lý câu hỏi của bạn. Vui lòng thử lại hoặc hỏi câu hỏi khác.")
                    .sessionId(request.getSessionId())
                    .responseType("error")
                    .build();
        }
    }
}
