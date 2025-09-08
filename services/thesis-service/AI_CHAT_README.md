# AI Chat Service - Hướng dẫn sử dụng

## Tổng quan

AI Chat Service là một tính năng mới được tích hợp vào thesis-service để cung cấp hỗ trợ AI cho sinh viên trong việc:

- Gợi ý đề tài luận văn phù hợp
- Tìm giảng viên có chuyên môn phù hợp và còn chỗ trống
- Kiểm tra capacity của giảng viên
- Tư vấn chung về quy trình đăng ký luận văn

## Cấu hình

### 1. API Key

Cập nhật API key trong `application.yml`:

```yaml
ai:
  openai:
    api-key: your-openai-api-key-here
    model-name: gpt-4o-mini
    temperature: 0.7
```

### 2. Dependencies

Đã thêm các dependencies cần thiết vào `pom.xml`:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>1.4.0</version>
</dependency>

<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>1.4.0</version>
</dependency>
```

## API Endpoints

### 1. Chat chung

**POST** `/api/thesis-service/ai-chat/chat`

**Request Body:**

```json
{
  "message": "Tôi muốn tìm đề tài về trí tuệ nhân tạo",
  "userId": "123",
  "sessionId": "session-123"
}
```

**Response:**

```json
{
  "message": "Tôi đã gợi ý một số đề tài phù hợp với yêu cầu của bạn:",
  "sessionId": "session-123",
  "topicSuggestions": [
    {
      "title": "Hệ thống nhận dạng khuôn mặt sử dụng AI",
      "description": "Xây dựng hệ thống nhận dạng khuôn mặt...",
      "objectives": "Tạo ra hệ thống nhận dạng khuôn mặt chính xác...",
      "methodology": "Sử dụng deep learning và OpenCV",
      "difficultyLevel": "MEDIUM",
      "expectedOutcome": "Hệ thống nhận dạng với độ chính xác > 95%"
    }
  ],
  "responseType": "topic_suggestion"
}
```

### 2. Gợi ý đề tài

**POST** `/api/thesis-service/ai-chat/suggest-topics?message=...&specialization=...`

**Parameters:**

- `message`: Mô tả yêu cầu đề tài
- `specialization`: Chuyên ngành (optional, default: "Công nghệ thông tin")

### 3. Tìm giảng viên

**POST** `/api/thesis-service/ai-chat/find-lecturers?message=...&specialization=...`

**Parameters:**

- `message`: Mô tả yêu cầu tìm giảng viên
- `specialization`: Chuyên ngành (optional, default: "Công nghệ thông tin")

### 4. Kiểm tra capacity giảng viên

**GET** `/api/thesis-service/ai-chat/check-capacity/{lecturerId}`

**Response:**

```json
{
  "message": "Giảng viên có thể nhận tối đa 15 sinh viên. Hiện tại đã nhận 8 sinh viên. Còn lại 7 chỗ trống.",
  "sessionId": "",
  "responseType": "capacity_check"
}
```

### 5. Trợ giúp chung

**GET** `/api/thesis-service/ai-chat/help`

## Cách sử dụng

### 1. Gợi ý đề tài

```bash
curl -X POST "http://localhost:8082/api/thesis-service/ai-chat/suggest-topics" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tôi muốn làm đề tài về machine learning trong y tế",
    "specialization": "Công nghệ thông tin"
  }'
```

### 2. Tìm giảng viên

```bash
curl -X POST "http://localhost:8082/api/thesis-service/ai-chat/find-lecturers" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tôi cần tìm giảng viên chuyên về AI và machine learning",
    "specialization": "Công nghệ thông tin"
  }'
```

### 3. Chat chung

```bash
curl -X POST "http://localhost:8082/api/thesis-service/ai-chat/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tôi cần tư vấn về quy trình đăng ký luận văn",
    "userId": "123",
    "sessionId": "session-123"
  }'
```

## Tính năng AI

### 1. Phân tích Intent

AI sẽ tự động phân tích ý định của người dùng:

- `topic_suggestion`: Muốn gợi ý đề tài
- `lecturer_search`: Muốn tìm giảng viên
- `capacity_check`: Muốn kiểm tra capacity
- `general_help`: Cần trợ giúp chung

### 2. Gợi ý đề tài thông minh

- Phân tích yêu cầu của sinh viên
- Tham khảo các đề tài hiện có trong hệ thống
- Gợi ý đề tài phù hợp với chuyên ngành
- Cung cấp thông tin chi tiết: mục tiêu, phương pháp, độ khó, kết quả mong đợi

### 3. Tìm giảng viên phù hợp

- Tìm giảng viên có chuyên môn phù hợp
- Kiểm tra capacity còn trống
- Cung cấp thông tin liên hệ

### 4. Tư vấn chung

- Hướng dẫn quy trình đăng ký
- Giải đáp thắc mắc về luận văn
- Cung cấp thông tin hữu ích

## Lưu ý

1. **API Key**: Đảm bảo cấu hình đúng OpenAI API key
2. **Rate Limiting**: OpenAI có giới hạn số request, cần quản lý phù hợp
3. **Error Handling**: Service có xử lý lỗi cơ bản, trả về thông báo thân thiện
4. **Logging**: Tất cả request đều được log để debug
5. **Security**: Cần thêm authentication/authorization nếu cần thiết

## Mở rộng

Có thể mở rộng thêm các tính năng:

- Lưu lịch sử chat
- Tích hợp với notification service
- Thêm các loại gợi ý khác
- Cải thiện accuracy của AI
- Thêm support cho nhiều ngôn ngữ
