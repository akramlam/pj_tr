# Binome Matcher PlantUML Diagrams

Simple black and white PlantUML diagrams for the Binome Matcher application.

## Diagrams

### 1. Simplified Architecture (`binome_matcher_simple_diagram.puml`)
A clean class diagram showing:
- Core domain entities (User, Profile, Message)
- Backend API components (Controllers, Services, Repositories)
- Android app structure
- Database connections

### 2. Minimal Overview (`binome_matcher_minimal_diagram.puml`)
An ultra-minimal architecture view showing:
- High-level components only
- Basic data flow
- System boundaries

## How to View

### Online
1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy the `.puml` file content
3. Paste and click "Submit"

### VS Code
1. Install PlantUML extension
2. Open `.puml` file
3. Press `Alt+D` to preview

## Architecture Summary

**Android App** → **Spring Boot API** → **PostgreSQL Database**

The system uses JWT for security and follows a standard 3-tier architecture.