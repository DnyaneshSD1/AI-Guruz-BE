# AI-Guruz-BE

## Project Structure

The backend is organized using package-based feature modules:

- `com.aiguruz.config`
  - `AwsConfig.java`
  - `MongoConfig.java`
  - `SecurityConfig.java`
  - `WebClientConfig.java`
  - `OpenApiConfig.java`
- `com.aiguruz.auth`
  - `controller/AuthController.java`
  - `service/AuthService.java`
  - `service/JwtService.java`
  - `filter/JwtAuthFilter.java`
  - `dto/SignupRequest.java` (also contains `LoginRequest`, `AuthResponse`, `SwitchRoleRequest`)
- `com.aiguruz.user`
  - `controller/UserController.java`
  - `service/UserService.java`
  - `repository/UserRepository.java`
  - `model/User.java`
  - `dto/UserDto.java`
  - `dto/CreateUserRequest.java`
  - `dto/UpdateRoleRequest.java`
- `com.aiguruz.document`
  - `controller/DocumentController.java`
  - `service/DocumentService.java`
  - `service/S3Service.java`
  - `service/TextractService.java`
  - `service/TextractSqsWorker.java`
  - `repository/DocumentRepository.java`
  - `model/Document.java`
  - `dto/DocumentDto.java`
  - `dto/UploadResponse.java`
- `com.aiguruz.ai`
  - `controller/AiController.java`
  - `service/AiService.java`
  - `service/SummaryService.java`
  - `repository/AiSessionRepository.java`
  - `repository/SummaryRepository.java`
  - `model/AiSession.java`
  - `model/Summary.java`
  - `dto/ChatRequest.java`
  - `dto/ChatResponse.java`
  - `dto/SummaryResponse.java`
- `com.aiguruz.analytics`
  - `controller/AnalyticsController.java`
  - `service/AnalyticsService.java`
  - `dto/AnalyticsResponse.java`
- `com.aiguruz.audit`
  - `controller/AuditController.java`
  - `service/AuditService.java`
  - `repository/AuditRepository.java`
  - `model/AuditLog.java`
- `com.aiguruz.library`
  - `controller/LibraryController.java`
  - `service/LibraryService.java`
  - `repository/LibraryRepository.java`
  - `model/LibraryDocument.java`
- `com.aiguruz.tenant`
  - `controller/TenantController.java`
  - `service/TenantService.java`
  - `repository/TenantRepository.java`
  - `model/Tenant.java`
- `com.aiguruz.common`
  - `exception/BadRequestException.java`
  - `exception/GlobalExceptionHandler.java`
  - `exception/ResourceNotFoundException.java`
  - `exception/UnauthorizedException.java`
  - `model/ApiResponse.java`
  - `model/PageResponse.java`
  - `util/SecurityUtils.java`

## Added Features

- Document ingestion with AWS S3 upload and Textract extraction
- AI summarization using Anthropic API
- JWT authentication and role-based authorization
- User management with tenant support
- Audit logging for key actions
- Analytics overview endpoints
- Shared library document management
- Tenant administration

## Notes

- `application.yml` should define AWS credentials, Textract/SQS/S3 settings, JWT secret, Anthropic API key, and other environment configuration.
- `@EnableAsync` and `@EnableScheduling` are enabled in `AiguruzApplication.java` for background tasks and SQS polling.

## Run

This project is configured for Java 17 source/target compatibility via Maven.

If `mvn` is installed on your system, run:

```bash
mvn clean package
mvn spring-boot:run
```

If Maven is not on `PATH`, use the included local Maven wrapper:

```powershell
.\apache-maven-3.9.6\bin\mvn clean package
.\apache-maven-3.9.6\bin\mvn spring-boot:run
```

Or build the jar and run it directly:

```bash
mvn clean package
java -jar target/aiguruz-backend-1.0.0.jar
```
