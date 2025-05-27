# CipherTalk - Secure Chat Application

CipherTalk is a modern, secure chat application built with Android (Kotlin) frontend and Spring Boot (Kotlin) backend. It features end-to-end encryption, real-time messaging via WebSockets, and JWT-based authentication.

## 🏗️ Architecture

- **Frontend**: Android app built with Jetpack Compose and Kotlin
- **Backend**: Spring Boot REST API with Kotlin
- **Database**: MySQL (production) / H2 (development)
- **Real-time Communication**: WebSockets
- **Authentication**: JWT tokens with BCrypt password hashing
- **Security**: End-to-end encryption for messages

## 📱 Features

### Core Features

- **User Authentication**: Secure registration and login
- **Real-time Chat**: WebSocket-based instant messaging
- **End-to-End Encryption**: Client-side message encryption
- **JWT Security**: Stateless authentication with token-based sessions
- **Modern UI**: Material Design 3 with Jetpack Compose
- **Cross-platform Backend**: RESTful API accessible from any client

### Security Features

- Password hashing with BCrypt
- JWT token authentication
- CORS configuration for web security
- Input validation and sanitization
- Secure WebSocket connections

## 🚀 Quick Start

### Prerequisites

- **Android Development**: Android Studio with Kotlin support
- **Backend Development**: Java 17+, Maven 3.6+
- **Database**: MySQL 8.0+ (or H2 for development)
- **Tools**: Git, curl (for API testing)

### Backend Setup

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd cipher/backend/backend
   ```

2. **Configure Database**

   **For MySQL (Production):**

   - Ensure MySQL is running on localhost:3306
   - Create database: `CREATE DATABASE cipher;`
   - Update `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/cipher?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=admin
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

   **For H2 (Development):**

   - Use profile: `--spring.profiles.active=dev`
   - H2 console available at: http://localhost:8080/h2-console

3. **Run the Backend**

   ```bash
   # Using Maven
   ./mvnw spring-boot:run

   # Or using VS Code task
   # Run task: "Run CipherTalk Kotlin Backend"
   ```

4. **Verify Backend**

   ```bash
   # Test registration
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "testuser", "password": "testpass123"}'

   # Test login
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "testuser", "password": "testpass123"}'
   ```

### Android App Setup

1. **Open in Android Studio**

   ```bash
   cd cipher/app
   # Open this folder in Android Studio
   ```

2. **Sync Gradle Dependencies**

   - Android Studio will automatically prompt to sync
   - Or manually: `File > Sync Project with Gradle Files`

3. **Configure Network**

   - For emulator: Backend URL is `http://10.0.2.2:8080`
   - For physical device: Update IP to your computer's local IP
   - Check `ApiClient.kt` for BASE_URL configuration

4. **Build and Run**
   ```bash
   # Via Android Studio: Run > Run 'app'
   # Or via command line:
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## 🛠️ Development

### Backend Development

#### Project Structure

```
backend/backend/
├── src/main/kotlin/com/ciphertalk/backend_kotlin/
│   ├── CiphertalkBackendKotlinApplication.kt  # Main application
│   ├── controller/
│   │   └── AuthController.kt                  # Authentication endpoints
│   ├── service/
│   │   ├── AuthService.kt                     # Authentication logic
│   │   └── UserDetailsServiceImpl.kt          # Spring Security integration
│   ├── model/
│   │   └── User.kt                            # User entity
│   ├── repository/
│   │   └── UserRepository.kt                  # Data access layer
│   ├── config/
│   │   ├── SecurityConfig.kt                  # Security configuration
│   │   ├── JwtAuthFilter.kt                   # JWT authentication filter
│   │   └── WebSocketConfig.kt                 # WebSocket configuration
│   ├── util/
│   │   └── JwtUtil.kt                         # JWT token utilities
│   └── dto/
│       ├── AuthRequest.kt                     # Authentication request DTO
│       └── AuthResponse.kt                    # Authentication response DTO
└── src/main/resources/
    ├── application.properties                 # Main configuration
    ├── application-dev.properties             # Development profile
    └── application-prod.properties            # Production profile
```

#### Key Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get user profile (requires JWT)
- `WebSocket /ws/chat` - Real-time chat connection

#### Adding New Features

1. Create controller in `controller/` package
2. Add service logic in `service/` package
3. Update security configuration if needed
4. Add tests in `src/test/kotlin/`

### Android Development

#### Project Structure

```
app/app/src/main/java/com/example/cipher/
├── MainActivity.kt                            # Main activity
├── ui/
│   ├── screen/
│   │   ├── LoginScreen.kt                     # Login UI
│   │   ├── RegisterScreen.kt                  # Registration UI
│   │   └── ChatScreen.kt                      # Chat interface
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt                   # Authentication state
│   │   └── ChatViewModel.kt                   # Chat state management
│   └── theme/
│       ├── Color.kt                           # App colors
│       ├── Theme.kt                           # Material theme
│       └── Type.kt                            # Typography
├── data/
│   ├── network/
│   │   ├── ApiClient.kt                       # HTTP client configuration
│   │   ├── AuthService.kt                     # Authentication API calls
│   │   └── websocket/
│   │       └── ChatWebSocketService.kt        # WebSocket management
│   ├── repository/
│   │   └── CipherRepository.kt                # Data repository
│   └── model/
│       ├── User.kt                            # User data model
│       ├── Message.kt                         # Message data model
│       └── AuthRequest.kt                     # API request models
└── util/
    ├── TokenManager.kt                        # JWT token storage
    └── CryptoUtils.kt                         # Encryption utilities
```

#### Key Dependencies

- **Jetpack Compose**: Modern Android UI toolkit
- **Retrofit**: HTTP client for API calls
- **OkHttp**: WebSocket and HTTP networking
- **Coroutines**: Asynchronous programming
- **ViewModel**: UI state management
- **Material 3**: Design system

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the backend directory:

```properties
JWT_SECRET=your-super-secret-jwt-key-here
DB_PASSWORD=your-database-password
CORS_ORIGINS=http://localhost:3000,http://10.0.2.2:8080
```

### Database Configuration

- **Development**: H2 in-memory database (auto-configured)
- **Production**: MySQL with persistent storage
- **Schema**: Auto-generated from JPA entities

### Security Configuration

- JWT expiration: 24 hours (configurable)
- Password encryption: BCrypt with strength 10
- CORS: Configured for local development

## 🧪 Testing

### Backend Testing

```bash
cd backend/backend

# Run unit tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Integration tests
./mvnw verify
```

### API Testing with curl

```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d @test_registration.json

# Login and get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "testpass123"}' | jq -r '.token')

# Access protected endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/auth/profile
```

### Android Testing

```bash
cd app

# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# UI tests
./gradlew connectedCheck
```

## 📝 API Documentation

### Authentication Endpoints

#### POST /api/auth/register

Register a new user account.

**Request Body:**

```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**

```json
{
  "message": "User registered successfully"
}
```

#### POST /api/auth/login

Authenticate user and receive JWT token.

**Request Body:**

```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**

```json
{
  "token": "jwt-token-here",
  "username": "string"
}
```

#### GET /api/auth/profile

Get authenticated user's profile (requires JWT token).

**Headers:**

```
Authorization: Bearer <jwt-token>
```

**Response:**

```json
{
  "id": 1,
  "username": "string",
  "publicKey": "string"
}
```

### WebSocket Endpoints

#### /ws/chat

Real-time chat WebSocket connection.

**Connection:** `ws://localhost:8080/ws/chat`

**Message Format:**

```json
{
  "type": "CHAT_MESSAGE",
  "content": "encrypted-message-content",
  "sender": "username",
  "timestamp": "2025-05-27T12:00:00Z"
}
```

## 🚨 Troubleshooting

### Common Backend Issues

**Port 8080 already in use:**

```bash
# Find process using port 8080
netstat -ano | findstr :8080
# Kill process (replace PID)
taskkill /PID <PID> /F
```

**Database connection failed:**

- Verify MySQL is running: `netstat -an | findstr :3306`
- Check credentials in `application.properties`
- Ensure database `cipher` exists

**JWT token issues:**

- Check JWT secret configuration
- Verify token expiration settings
- Clear browser/app storage for fresh tokens

### Common Android Issues

**Network connection failed:**

- Verify backend is running on correct port
- Check network security config for HTTP traffic
- Update BASE_URL in ApiClient.kt for your environment

**Build errors:**

- Clean and rebuild: `./gradlew clean build`
- Sync Gradle files in Android Studio
- Check Android SDK and build tools versions

**WebSocket connection issues:**

- Verify WebSocket endpoint URL
- Check network permissions in AndroidManifest.xml
- Test WebSocket connection with browser dev tools

### Performance Issues

**Backend performance:**

- Enable JPA query logging for debugging
- Monitor database connection pool
- Check memory usage with profiler

**Android performance:**

- Use Android Profiler for memory/CPU analysis
- Optimize Compose recompositions
- Check network request frequency

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Style

- **Kotlin**: Follow official Kotlin coding conventions
- **Android**: Use Android Kotlin style guide
- **Spring Boot**: Follow Spring Boot best practices

### Commit Guidelines

- Use conventional commits: `feat:`, `fix:`, `docs:`, `refactor:`
- Keep commits atomic and well-described
- Include tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android UI toolkit
- [Kotlin](https://kotlinlang.org/) - Programming language
- [Material Design](https://material.io/) - Design system
- [OkHttp](https://square.github.io/okhttp/) - HTTP client library

## 📞 Support

For questions and support:

- Create an issue in the GitHub repository
- Check the troubleshooting section above
- Review the API documentation

---

**Built with ❤️ using Kotlin and Spring Boot**
