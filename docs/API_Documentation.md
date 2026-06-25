# Nexus API Documentation

## Base URL
`http://localhost:8081/api/v1`

## Swagger URL
`http://localhost:8081/swagger-ui.html`

## H2 Console URL
`http://localhost:8081/h2-console`

## Authentication
Most endpoints require a JWT token in the Authorization header:
`Authorization: Bearer your-token-here`

## Endpoints

### Auth Service

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login & get token | No |
| GET | `/auth/verify` | Verify JWT token | Yes |
| GET | `/auth/health` | Health check | No |

### News Service 

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/health/news` | Get latest health news | Yes |
| GET | `/health/news/{id}` | Get single news article | Yes |
| POST | `/health/news` | Add news article (Admin) | Yes |
GET | `/health/news{id}` | Get single news article by ID | NO|
POST | `/health/news` | Publish a new news article | Admin only | 
DELETE | `/health/news/{id}` | Delete a news article | Admin only|
POST |	/health/news/refresh |	Force refresh news feed |	Admin Only

### Admin Service
|Method|Endpoint | Description|	Auth Required
GET|	`/admin/users`|	Get all users with pagination|	Admin Only
GET|	`/admin/users/{userId}`|	Get user by ID|	Admin Only
PUT|	`/admin/users/{userId}`| role	Update user role|	Admin Only
PUT|	`/admin/users/{userId}/status`|	Enable/disable user	|Admin Only
DELETE|	`/admin/users/{userId}`|	Delete user account|	Admin Only
GET|	`/admin/audit-logs`|	Get audit logs with pagination|	Admin Only
GET|	`/admin/audit-logs/{id}`|	Get audit log by ID|	Admin Only
GET|	`/admin/audit-logs/user/{userId}`|	Get audit logs for specific user|	Admin Only
GET|	`/admin/dashboard/stats`|	Get dashboard statistics|	Admin Only
GET|	`/admin/system/health`|	System health check|	Admin Only
GET|	`/admin/system/info`|	System information|	Admin Only


### Media Service (Coming Soon)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/media/first-aid` | List first-aid videos | Yes |
| POST | `/media/first-aid` | Upload video (Admin) | Yes |

### Translate Service (Coming Soon)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/translate/text-to-sign` | Convert text to sign language | Yes |
| POST | `/translate/speech-to-text` | Convert speech to text | Yes |