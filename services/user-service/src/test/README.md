# Unit Tests cho User Service

## Tổng quan

Đây là các unit test cho `user-service` trong hệ thống quản lý luận văn Phenikaa. Các test được viết bằng JUnit 5 và Mockito để kiểm tra các chức năng chính của service.

## Cấu trúc Test

### 1. SimpleUserServiceTest.java

File test chính chứa 5 test cases cơ bản:

#### Test Case 1: `testGetAllUsers_Success()`

- **Mục đích**: Kiểm tra việc lấy danh sách tất cả user thành công
- **Input**: Mock data với 1 user
- **Expected**: Trả về danh sách 1 user với thông tin đúng
- **Verify**: `userRepository.findAll()` và `userMapper.toDTO()` được gọi

#### Test Case 2: `testGetUserById_Success()`

- **Mục đích**: Kiểm tra việc lấy user theo ID thành công
- **Input**: User ID = 1, user tồn tại
- **Expected**: Trả về user response với thông tin đúng
- **Verify**: `userRepository.findById()` và `userMapper.toDTO()` được gọi

#### Test Case 3: `testChangeStatusUser_Success()`

- **Mục đích**: Kiểm tra việc thay đổi trạng thái user thành công
- **Input**: User ID = 1, status hiện tại = 1
- **Expected**: Status được thay đổi từ 1 sang 2
- **Verify**: `userRepository.findById()` và `userRepository.save()` được gọi

#### Test Case 4: `testGetUserById_UserNotFound_ThrowsException()`

- **Mục đích**: Kiểm tra exception khi user không tồn tại
- **Input**: User ID = 999, user không tồn tại
- **Expected**: Throw `ResponseStatusException` với status 404
- **Verify**: `userRepository.findById()` được gọi, `userMapper.toDTO()` không được gọi

#### Test Case 5: `testGetAllUsersPaged_Success()`

- **Mục đích**: Kiểm tra việc lấy danh sách user có phân trang
- **Input**: Page = 0, Size = 8
- **Expected**: Trả về Page object với 1 user
- **Verify**: `userRepository.findAll(Pageable)` và `userMapper.toDTO()` được gọi

## Cách chạy Test

### Chạy tất cả test

```bash
mvn test
```

### Chạy test cụ thể

```bash
mvn test -Dtest=SimpleUserServiceTest
```

### Chạy test với report chi tiết

```bash
mvn test -Dtest=SimpleUserServiceTest -X
```

## Dependencies Test

Các dependencies cần thiết cho testing đã được thêm vào `pom.xml`:

```xml
<!-- Test Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

## Kết quả Test

```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Tất cả 5 test cases đều PASS thành công, đảm bảo các chức năng cơ bản của UserService hoạt động đúng.

## Lưu ý

- Các test sử dụng Mockito để mock các dependencies
- Test data được setup trong `@BeforeEach` method
- Mỗi test case có comment rõ ràng về mục đích, input, expected output và verify
- Test cases bao phủm cả happy path và error cases
