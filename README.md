# 🛒 E-Commerce REST API

A full-featured, production-ready E-Commerce backend built with
Spring Boot 3.x, following Low-Level Design (LLD) principles
throughout.

---

## 🚀 Tech Stack

| Layer              | Technology                        |
|--------------------|-----------------------------------|
| Framework          | Spring Boot 3.5.x                 |
| Language           | Java 17                           |
| Database           | MySQL 8.x                         |
| ORM                | Spring Data JPA + Hibernate       |
| Security           | Spring Security + JWT             |
| Caching            | Redis                             |
| Async Messaging    | RabbitMQ                          |
| Email              | JavaMail (SMTP)                   |
| Payment            | Razorpay                          |
| API Documentation  | Swagger UI (SpringDoc OpenAPI)    |
| Build Tool         | Maven                             |

---

## ✨ Features

### 🔐 Authentication
- User registration with OTP email verification
- JWT-based stateless authentication
- Login blocked until OTP verified
- BCrypt password encoding

### 📦 Products
- Public product browsing (no login required)
- Paginated product listing with sort support
- Search by name or category
- Redis caching (10-minute TTL, auto-evict on update)
- Admin-only: Create, Update, Delete products

### 🛒 Cart
- Add, update, remove cart items
- View cart with subtotals
- Cart auto-cleared after order placement

### 📋 Orders
- Place order from cart with delivery address
- Stock validation before order placement
- Auto stock reduction on order, restore on cancel
- Order cancellation (only if status is PLACED)
- Admin: view all orders, update order status

### 💳 Payment (Razorpay)
- Create Razorpay payment order
- Verify payment signature (HMAC SHA256)
- Auto-update order to PAID + CONFIRMED on success
- Double-payment prevention

### 📧 Async Email (RabbitMQ)
- OTP emails sent asynchronously via RabbitMQ
- Order confirmation emails sent asynchronously
- Non-blocking — API responds instantly

### 👤 User Profile
- View profile
- Update name
- Change password (with current password verification)

### ⚠️ Exception Handling
- Global exception handler
- Consistent error response format
- Proper HTTP status codes (400, 401, 403, 404, 500)

---

## 📁 Project Structure
src/main/java/com/arun/ecommerce/
│
├── EcommerceApplication.java
│
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── AuthServiceImpl.java
│   ├── OtpService.java
│   ├── OtpServiceImpl.java
│   ├── EmailService.java
│   ├── CustomUserDetailsService.java
│   ├── UserRepository.java
│   └── dto/
│       ├── RegisterRequest.java
│       ├── LoginRequest.java
│       ├── OtpRequest.java
│       ├── OtpVerifyRequest.java
│       └── AuthResponse.java
│
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserServiceImpl.java
│   └── dto/
│       ├── UserProfileResponse.java
│       ├── UpdateProfileRequest.java
│       └── ChangePasswordRequest.java
│
├── product/
│   ├── ProductController.java
│   ├── ProductService.java
│   ├── ProductServiceImpl.java
│   ├── ProductRepository.java
│   └── dto/
│       ├── ProductRequest.java
│       ├── ProductResponse.java
│       └── PagedResponse.java
│
├── cart/
│   ├── CartController.java
│   ├── CartService.java
│   ├── CartServiceImpl.java
│   ├── CartRepository.java
│   └── dto/
│       ├── CartRequest.java
│       └── CartResponse.java
│
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderServiceImpl.java
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   └── dto/
│       ├── OrderRequest.java
│       ├── OrderResponse.java
│       ├── OrderItemResponse.java
│       └── OrderStatusUpdateRequest.java
│
├── payment/
│   ├── PaymentController.java
│   ├── PaymentService.java
│   ├── PaymentServiceImpl.java
│   └── dto/
│       ├── PaymentOrderRequest.java
│       ├── PaymentOrderResponse.java
│       ├── PaymentVerifyRequest.java
│       └── PaymentVerifyResponse.java
│
├── messaging/
│   ├── EmailPublisher.java
│   ├── EmailConsumer.java
│   ├── EmailMessage.java
│   └── OrderConfirmationMessage.java
│
├── entity/
│   ├── BaseEntity.java
│   ├── User.java
│   ├── Product.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   └── OrderItem.java
│   └── enums/
│       ├── Role.java
│       ├── OrderStatus.java
│       └── PaymentStatus.java
│
├── config/
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   ├── RabbitMQConfig.java
│   └── SwaggerConfig.java
│
├── security/
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
│
└── exception/
├── GlobalExceptionHandler.java
├── ResourceNotFoundException.java
└── ErrorResponse.java



---

## 🔗 API Endpoints

### Auth
| Method | Endpoint                  | Access |
|--------|---------------------------|--------|
| POST   | /api/auth/register        | Public |
| POST   | /api/auth/send-otp        | Public |
| POST   | /api/auth/verify-otp      | Public |
| POST   | /api/auth/login           | Public |

### Products
| Method | Endpoint                  | Access |
|--------|---------------------------|--------|
| GET    | /api/products             | Public |
| GET    | /api/products/{id}        | Public |
| GET    | /api/products/search      | Public |
| POST   | /api/products             | Admin  |
| PUT    | /api/products/{id}        | Admin  |
| DELETE | /api/products/{id}        | Admin  |

### Cart
| Method | Endpoint                  | Access |
|--------|---------------------------|--------|
| GET    | /api/cart                 | User   |
| POST   | /api/cart/add             | User   |
| PUT    | /api/cart/update          | User   |
| DELETE | /api/cart/remove/{id}     | User   |
| DELETE | /api/cart/clear           | User   |

### Orders
| Method | Endpoint                       | Access |
|--------|--------------------------------|--------|
| POST   | /api/orders/place              | User   |
| GET    | /api/orders                    | User   |
| GET    | /api/orders/{id}               | User   |
| PUT    | /api/orders/{id}/cancel        | User   |
| GET    | /api/orders/all                | Admin  |
| PUT    | /api/orders/{id}/status        | Admin  |

### Payment
| Method | Endpoint                       | Access |
|--------|--------------------------------|--------|
| POST   | /api/payments/create-order     | User   |
| POST   | /api/payments/verify           | User   |

### User Profile
| Method | Endpoint                  | Access |
|--------|---------------------------|--------|
| GET    | /api/users/me             | User   |
| PUT    | /api/users/me             | User   |
| PUT    | /api/users/password       | User   |

---
## 📖 Swagger UI

After running the application, visit:

    http://localhost:8080/swagger-ui/index.html

- Click **Authorize**
- Paste your JWT token from `/api/auth/login`
- Test all endpoints directly from the browser

---

## 🏃 How to Run

### Prerequisites
- Java 17+
- MySQL 8.x running
- Redis running on port 6379
- RabbitMQ running on port 5672

### Steps

    # 1. Clone the repository
    git clone https://github.com/your-username/ecommerce-api.git

    # 2. Configure application.properties

    # 3. Run
    ./mvnw spring-boot:run

---

## 🔒 Security

- JWT tokens required for all protected endpoints
- Role-based access: ROLE_USER and ROLE_ADMIN
- Unverified accounts cannot login
- Payment signature verified with HMAC SHA256

---

## 📐 Design Principles

- SOLID principles followed throughout
- Interface-first design (no direct impl dependencies)
- Constructor injection (no field @Autowired)
- Builder pattern on all entities and DTOs
- No Lombok — explicit, readable code
- Async operations via RabbitMQ (non-blocking email)

## ⚙️ Configuration

### `application.properties`
```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# JWT
jwt.secret=your_secret_key
jwt.expiration=86400000

# Mail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# Razorpay
razorpay.key.id=your_key_id
razorpay.key.secret=your_key_secret




