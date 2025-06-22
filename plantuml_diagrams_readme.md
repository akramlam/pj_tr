# Binome Matcher PlantUML Diagrams

Clean black and white PlantUML diagrams for the Binome Matcher application.

## 🎯 Presentation Diagrams (Easy to Explain)

### 1. Simple Flow (`binome_matcher_simple_flow_diagram.puml`) ⭐
**Best for explaining the concept** - Shows user journey:
- How users interact with the system
- Main flow: Register → Profile → Match → Connect
- Minimal classes with clear relationships
- Step-by-step note included

### 2. Presentation View (`binome_matcher_presentation_diagram.puml`) ⭐
**Best for presentations** - Shows core concepts only:
- 3 main entities (User, Profile, Message)
- 3 core services (Auth, Match, Message)
- Clear flow from App → Services → Database
- Includes helpful note about matching

### 3. Vertical Layout (`binome_matcher_vertical_diagram.puml`) ⭐
**Best for architecture overview** - Shows layers:
- Top-to-bottom flow (App → API → Services → Data → DB)
- Each layer's responsibilities
- Clean vertical alignment
- Perfect for slide presentations

## 📚 Detailed Diagrams (For Documentation)

### 4. Full Class Diagram (`binome_matcher_simple_diagram.puml`)
Complete class diagram with all attributes and methods

### 5. Clean Class Diagram (`binome_matcher_clean_class_diagram.puml`)
Balanced version with essential details

### 6. Component Overview (`binome_matcher_minimal_diagram.puml`)
High-level system components

## How to View

1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy any `.puml` file content
3. Paste and click "Submit"

## 💡 Presentation Tips

- **Start with**: Simple Flow diagram to explain what the app does
- **Then show**: Presentation View to explain the main components
- **End with**: Vertical Layout to show the technical architecture
- All diagrams are black & white for easy printing/projection

## What is Binome Matcher?

A platform that matches users based on their skills and preferences:
- Users create profiles with their skills
- The system finds compatible matches
- Users can message their matches
- Built with Android app + Spring Boot API + PostgreSQL

## Key Classes

- **User**: Core authentication entity
- **Profile**: User details and preferences
- **Message**: Communication between users
- **Controllers**: REST API endpoints
- **Services**: Business logic
- **Repositories**: Data access layer

All diagrams use monochrome styling for clean, professional documentation.