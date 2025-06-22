# Binome Matcher PlantUML Diagrams

Simple black and white PlantUML diagrams for the Binome Matcher application.

## Diagrams (from detailed to minimal)

### 1. Simple Class Diagram (`binome_matcher_simple_diagram.puml`)
Shows individual classes without attributes:
- All main classes (User, Profile, Message)
- Controllers, Services, Repositories
- Basic relationships

### 2. Ultra Simple (`binome_matcher_ultra_simple_diagram.puml`)
Abstract class diagram showing:
- Generic Controller → Service → Repository pattern
- Core entities only
- Data flow

### 3. Minimal Overview (`binome_matcher_minimal_diagram.puml`)
Component-level architecture:
- 4 main components
- High-level interactions

## How to View

1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy any `.puml` file content
3. Paste and click "Submit"

## Architecture

**Android App** → **Spring Boot API** → **PostgreSQL**

Simple 3-tier architecture with JWT security.