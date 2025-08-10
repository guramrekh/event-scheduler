# Huddle - Event Scheduler Platform

**Huddle** is designed to streamline event organization and participation management. It provides a robust platform where users can:

- **Create events** with detailed information (title, description, date/time, location)
- **Invite participants** with a structured invitation workflow
- **Track attendance** with role-based permissions (Organizer vs Attendee)
- **Receive notifications** for all event-related activities
- **Manage user profiles** with profile picture support via cloud storage


## Features

### Event Management
- **Event Creation**: Users can create events with comprehensive details
- **Editing and Cancellation**: Organizers can modify event details or completely cancel it with participant notification options 
- **Role Granting**: Make attendees organizers and vice versa 
- **Attendance Tracking**: Mark participants as attended, track withdrawals, and manage exclusions

### Invitation System
- **Structured Invitations**: Organizers can invite users to events
- **Response Management**: Invitees can accept, decline, or leave invitations pending
- **Status Tracking**: Complete visibility of invitation statuses
- **Automatic Registration**: Accepted invitations automatically register users as attendees

### Notification System
- **Real-time Notifications**: Instant updates for all event-related activities
- **Comprehensive Coverage**: Notifications for invitations, responses, event updates, cancellations, and role changes
- **Read Status Management**: Mark individual or all notifications as read

### Profile Customization
- **Add bio**: Personalize your profile with a custom bio
- **Upload profile picture**: Upload and manage profile pictures with secure signed URLs
- **Password Management**: Secure password change with validation and encryption
- **Delete Account**: Permanently remove your account and all associated data with proper cleanup 


## Tech Stack & Tooling

- ### Spring Boot
    - Built-in security, data access, validation, and web capabilities

- ### PostgreSQL/H2
    - PostgreSQL for production
    - H2 in-memory database for development
    - JPA/Hibernate for database operations

- ### Maven
    - Dependency management and build automation tool
    - Maven Wrapper included for consistent builds across environments
    - Spring Boot plugin for packaging and running applications

- ### Cloudinary: Cloud-based image and video management
  - Secure upload signatures
  - Automatic image optimization
  - CDN delivery for fast loading


## Frontend Repository

The [client-side of this app](https://github.com/guramkhech/event-scheduler-frontend) is developed with **Cursor** and **Lovable**


## Getting Started

### Backend Setup

#### Prerequisites
- **Java 17+** (JDK 17 or higher)
- **Maven 3.9+** (or use included Maven Wrapper)
- **PostgreSQL** (for production deployment)

#### Environment Setup

1. **Clone the repository**
   ```bash
   git clone git@github.com:guramrekh/event-scheduler.git
   cd event-scheduler
   ```

2. **Configure application properties**
   Create `src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/event_scheduler
   spring.datasource.username=your_db_username
   spring.datasource.password=your_db_password

   # JPA Configuration
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false

   # Cloudinary Configuration
   cloudinary.cloud_name=your_cloud_name
   cloudinary.api_key=your_api_key
   cloudinary.api_secret=your_api_secret
   ```

3. **Build and run the application**
   ```bash
   # Using Maven Wrapper (recommended)
   ./mvnw spring-boot:run

   # Or using installed Maven
   mvn spring-boot:run
   ```


#### Development Setup

For development with H2 in-memory database:
```properties
# H2 Database (Development)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Building for Production
```bash
./mvnw clean package
java -jar target/event-scheduler-0.0.1-SNAPSHOT.jar
```

### Frontend Setup

#### Prerequisites
- **Node.js** (v14.0+ recommended)
- **npm** (v6.0+ recommended)

#### Environment Setup

1. **Clone the frontend repository**
   ```bash
   git clone git@github.com:guramrekh/event-scheduler-frontend.git
   cd event-scheduler-frontend
   ```

2. **Install dependencies**
   ```bash
   # Using npm
   npm install
   ```

3. **Run the development server**
   ```bash
   # Using npm
   npm start
   ```

4. **Access the application**
   - Frontend URL: `http://localhost:3000`

#### Building for Production

```bash
# Using npm
npm run build
```

The build artifacts will be stored in the `build/` directory, ready to be deployed to a static hosting service.
