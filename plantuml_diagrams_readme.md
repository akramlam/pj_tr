# Binome Matcher PlantUML Diagrams

This directory contains PlantUML diagrams for the Binome Matcher application architecture.

## Diagrams

### 1. Class Diagram (`binome_matcher_class_diagram.puml`)
A detailed class diagram showing:
- Core domain entities and their attributes
- API controllers, services, and repositories
- Security components (JWT authentication)
- Android client main components
- Relationships between all components

### 2. Architecture Diagram (`binome_matcher_architecture_diagram.puml`)
A high-level architecture diagram showing:
- Android Mobile App layer
- Spring Boot Backend layer
- Data Persistence layer
- Communication flow between components

## How to View the Diagrams

### Option 1: PlantUML Online Server
1. Visit http://www.plantuml.com/plantuml/uml/
2. Copy the content of the `.puml` file
3. Paste it in the text area
4. Click "Submit" to generate the diagram

### Option 2: VS Code Extension
1. Install the "PlantUML" extension in VS Code
2. Open the `.puml` file
3. Press `Alt+D` to preview the diagram

### Option 3: Command Line
```bash
# Install PlantUML
java -jar plantuml.jar binome_matcher_class_diagram.puml
java -jar plantuml.jar binome_matcher_architecture_diagram.puml
```

## Color Legend

- **Red** - Domain Entities (User, Profile, Message)
- **Green** - Business Services
- **Blue** - REST Controllers
- **Yellow** - Repositories (Data Access)
- **Gold** - Security Components
- **Purple** - Android Components

## Architecture Overview

The Binome Matcher application follows a typical 3-tier architecture:

1. **Presentation Layer** - Android mobile app with activities and fragments
2. **Business Layer** - Spring Boot services implementing business logic
3. **Data Layer** - JPA repositories with PostgreSQL database

The application uses JWT tokens for authentication and follows RESTful API design principles.