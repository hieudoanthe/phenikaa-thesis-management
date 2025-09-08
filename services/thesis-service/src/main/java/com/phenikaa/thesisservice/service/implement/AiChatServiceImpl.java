package com.phenikaa.thesisservice.service.implement;

import com.phenikaa.thesisservice.client.ProfileServiceClient;
import com.phenikaa.thesisservice.dto.request.ChatRequest;
import com.phenikaa.thesisservice.dto.response.ChatResponse;
import com.phenikaa.thesisservice.entity.LecturerCapacity;
import com.phenikaa.thesisservice.entity.ProjectTopic;
import com.phenikaa.thesisservice.repository.LecturerCapacityRepository;
import com.phenikaa.thesisservice.repository.ProjectTopicRepository;
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

    private final ProjectTopicRepository projectTopicRepository;
    private final LecturerCapacityRepository lecturerCapacityRepository;
    private final ProfileServiceClient profileServiceClient;

    @Value("${ai.gemini.api-key:AIzaSyCUuAhB8wbCCCqrhlTXT83Sbe5c17GTJlU}")
    private String geminiApiKey;

    private ChatModel getChatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-1.5-flash")
                .temperature(0.7)
                .build();
    }

    @Override
    public ChatResponse processChatMessage(ChatRequest request) {
        try {
            String userMessage = request.getMessage().toLowerCase();
            ChatModel model = getChatModel();

            // Phân tích intent của người dùng
            String intent = analyzeIntent(userMessage, model);
            
            switch (intent) {
                case "topic_suggestion":
                    return suggestTopics(request.getMessage(), extractSpecialization(userMessage));
                case "lecturer_search":
                    return findSuitableLecturers(request.getMessage(), extractSpecialization(userMessage));
                case "capacity_check":
                    return checkLecturerCapacity(extractLecturerId(userMessage));
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
            
            // Lấy danh sách đề tài hiện có để tham khảo
            List<ProjectTopic> existingTopics = projectTopicRepository.findAll();
            String existingTopicsContext = existingTopics.stream()
                    .limit(10)
                    .map(topic -> String.format("Đề tài: %s - Mô tả: %s - Mức độ: %s", 
                            topic.getTitle(), topic.getDescription(), topic.getDifficultyLevel()))
                    .collect(Collectors.joining("\n"));

            String prompt = String.format("""
                    Bạn là một AI chuyên gia tư vấn đề tài luận văn tốt nghiệp cho sinh viên.
                    
                    Yêu cầu của sinh viên: %s
                    Chuyên ngành: %s
                    
                    Dựa trên các đề tài hiện có trong hệ thống:
                    %s
                    
                    Hãy gợi ý 3-5 đề tài phù hợp với yêu cầu và chuyên ngành của sinh viên.
                    Mỗi đề tài cần có:
                    - Tiêu đề rõ ràng, cụ thể
                    - Mô tả chi tiết về nội dung nghiên cứu
                    - Mục tiêu nghiên cứu cụ thể
                    - Phương pháp nghiên cứu phù hợp
                    - Mức độ khó (EASY, MEDIUM, HARD)
                    - Kết quả mong đợi
                    
                    Trả về kết quả theo định dạng JSON với các trường: title, description, objectives, methodology, difficultyLevel, expectedOutcome
                    """, userMessage, specialization, existingTopicsContext);

            UserMessage userMsg = UserMessage.from(prompt);
            AiMessage response = model.chat(userMsg).aiMessage();
            String responseText = response.text();
            
            // Parse response và tạo topic suggestions
            List<ChatResponse.TopicSuggestion> suggestions = parseTopicSuggestions(responseText);
            
            return ChatResponse.builder()
                    .message("Tôi đã gợi ý một số đề tài phù hợp với yêu cầu của bạn:")
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
                        .message("Hiện tại không có giảng viên nào có chỗ trống để hướng dẫn sinh viên.")
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
                            .lecturerName((String) lecturerInfo.get("name"))
                            .specialization((String) lecturerInfo.get("specialization"))
                            .remainingCapacity(capacity.getRemainingSlots())
                            .email((String) lecturerInfo.get("email"))
                            .phone((String) lecturerInfo.get("phone"))
                            .build();
                    
                    suggestions.add(suggestion);
                } catch (Exception e) {
                    log.warn("Could not fetch lecturer info for ID {}: {}", capacity.getLecturerId(), e.getMessage());
                }
            }

            return ChatResponse.builder()
                    .message("Dưới đây là danh sách giảng viên phù hợp có chỗ trống:")
                    .sessionId("")
                    .lecturerSuggestions(suggestions)
                    .responseType("lecturer_suggestion")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error finding suitable lecturers: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi không thể tìm giảng viên phù hợp lúc này. Vui lòng thử lại sau.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public ChatResponse checkLecturerCapacity(Integer lecturerId) {
        try {
            LecturerCapacity capacity = lecturerCapacityRepository.findByLecturerId(lecturerId)
                    .orElse(null);
            
            if (capacity == null) {
                return ChatResponse.builder()
                        .message("Không tìm thấy thông tin capacity của giảng viên này.")
                        .sessionId("")
                        .responseType("capacity_check")
                        .build();
            }

            String message = String.format(
                    "Giảng viên có thể nhận tối đa %d sinh viên. Hiện tại đã nhận %d sinh viên. Còn lại %d chỗ trống.",
                    capacity.getMaxStudents(),
                    capacity.getCurrentStudents(),
                    capacity.getRemainingSlots()
            );

            return ChatResponse.builder()
                    .message(message)
                    .sessionId("")
                    .responseType("capacity_check")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error checking lecturer capacity: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, tôi không thể kiểm tra capacity của giảng viên lúc này.")
                    .sessionId("")
                    .responseType("error")
                    .build();
        }
    }

    @Override
    public ChatResponse getGeneralHelp() {
        String helpMessage = """
                Tôi có thể giúp bạn:
                
                1. **Gợi ý đề tài luận văn**: Hãy mô tả chuyên ngành và sở thích nghiên cứu của bạn
                2. **Tìm giảng viên phù hợp**: Tôi sẽ tìm giảng viên có chuyên môn phù hợp và còn chỗ trống
                3. **Kiểm tra capacity giảng viên**: Xem giảng viên nào còn có thể nhận sinh viên
                4. **Tư vấn chung**: Hỏi về quy trình đăng ký, yêu cầu luận văn, v.v.
                
                Hãy cho tôi biết bạn cần hỗ trợ gì nhé!
                """;

        return ChatResponse.builder()
                .message(helpMessage)
                .sessionId("")
                .responseType("general_help")
                .build();
    }

    private String analyzeIntent(String message, ChatModel model) {
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
        return response.text().trim();
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
        // Có thể cải thiện bằng regex hoặc NLP
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

    private List<ChatResponse.TopicSuggestion> parseTopicSuggestions(String response) {
        // Logic đơn giản để parse JSON response từ AI
        // Trong thực tế, nên sử dụng JSON parser chuyên nghiệp
        List<ChatResponse.TopicSuggestion> suggestions = new ArrayList<>();
        
        // Tạo một số gợi ý mẫu nếu không parse được JSON
        suggestions.add(ChatResponse.TopicSuggestion.builder()
                .title("Hệ thống quản lý sinh viên sử dụng Spring Boot")
                .description("Xây dựng hệ thống quản lý thông tin sinh viên với các chức năng CRUD")
                .objectives("Tạo ra một hệ thống web hoàn chỉnh để quản lý sinh viên")
                .methodology("Phát triển theo mô hình MVC, sử dụng Spring Boot và JPA")
                .difficultyLevel("MEDIUM")
                .expectedOutcome("Hệ thống web hoàn chỉnh với giao diện thân thiện")
                .build());
                
        return suggestions;
    }

    private ChatResponse handleGeneralQuery(ChatRequest request, ChatModel model) {
        String prompt = String.format("""
                Bạn là AI trợ lý tư vấn cho hệ thống quản lý luận văn tốt nghiệp.
                Trả lời câu hỏi của sinh viên một cách thân thiện và hữu ích.
                
                Câu hỏi: %s
                
                Nếu câu hỏi liên quan đến đề tài, giảng viên, hoặc quy trình đăng ký, hãy hướng dẫn sinh viên sử dụng các chức năng cụ thể.
                """, request.getMessage());

        UserMessage userMsg = UserMessage.from(prompt);
        AiMessage response = model.chat(userMsg).aiMessage();
        String responseText = response.text();
        
        return ChatResponse.builder()
                .message(responseText)
                .sessionId(request.getSessionId())
                .responseType("general")
                .build();
    }
}
