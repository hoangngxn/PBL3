# Tutoring Platform API

A Spring Boot application that provides APIs for a tutoring platform where tutors can post their services and students can book sessions.

## Features

- User authentication (JWT-based)
- Role-based access control (Student/Tutor)
- Post management for tutors
- Booking system for students
- MongoDB for data persistence

## Prerequisites

- Docker and Docker Compose
- Java 18 (for local development only)
- Maven (for local development only)

## Running with Docker

### Quick Start

1. Clone the repository:
```bash
git clone <repository-url>
cd <project-directory>
```

2. Start the application and MongoDB:
```bash
docker-compose up -d
```

The API will be available at `http://localhost:8080`

### Docker Commands

- Start services:
```bash
docker-compose up -d
```

- Stop services:
```bash
docker-compose down
```

- Rebuild and start services:
```bash
docker-compose up -d --build
```

### Environment Variables

The following environment variables can be configured:

| Variable | Description | Default |
|----------|-------------|---------|
| MONGO_HOST | MongoDB host | mongodb |
| MONGO_PORT | MongoDB port | 27017 |
| MONGO_DB | MongoDB database name | auth_db |
| MONGO_USER | MongoDB username | - |
| MONGO_PASSWORD | MongoDB password | - |
| JWT_SECRET | Secret key for JWT tokens | your-secret-key |
| CORS_ORIGINS | Allowed CORS origins | * |

You can set these variables in a `.env` file or pass them directly to docker-compose:

```bash
MONGO_USER=admin MONGO_PASSWORD=secret docker-compose up -d
```

## API Documentation

Refer to [api-docs.md](api-docs.md) for detailed API documentation.


## Security

- All endpoints except authentication endpoints require JWT token
- Passwords are encrypted using BCrypt
- CORS is configured to allow specified origins
- Role-based access control for different endpoints

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

