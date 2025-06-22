# Binome Matcher PlantUML Diagrams

Clean black and white PlantUML diagrams for the Binome Matcher application.

## 📊 Report & Documentation Diagram

### **Detailed Report Diagram** (`binome_matcher_detailed_report_diagram.puml`) ⭐⭐⭐
**Best for technical reports and documentation** - Complete system architecture:
- **All 4 domain entities** with complete attributes (User, Profile, Message, DebugLog)
- **6 REST controllers** with all endpoint methods
- **6 business services** with key operations
- **4 data repositories** with data access methods  
- **3 security components** (JWT authentication flow)
- **Android client architecture** (Activities, Fragments, API client)
- **8 DTOs** for data transfer
- **Complete relationships** showing all dependencies
- **Database schema** representation
- Uses UML stereotypes (<<Entity>>, <<Service>>, <<Repository>>, etc.)

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

## 📚 Other Diagrams

### 4. Full Class Diagram (`binome_matcher_simple_diagram.puml`)
Complete class diagram with all attributes and methods (less organized than report diagram)

### 5. Clean Class Diagram (`binome_matcher_clean_class_diagram.puml`)
Balanced version with essential details

### 6. Component Overview (`binome_matcher_minimal_diagram.puml`)
High-level system components

## How to View

1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy any `.puml` file content
3. Paste and click "Submit"

## 💡 Usage Recommendations

### For Technical Reports
Use the **Detailed Report Diagram** - it includes:
- Complete class structure with attributes and methods
- All system components organized by layers
- Proper UML notation and stereotypes
- Comprehensive relationships
- Suitable for technical documentation

### For Presentations
Start with **Simple Flow** → **Presentation View** → **Vertical Layout**

## What is Binome Matcher?

A platform that matches users based on their skills and preferences:
- **Backend**: Spring Boot REST API with JWT authentication
- **Frontend**: Native Android application
- **Database**: PostgreSQL with JPA/Hibernate
- **Features**: User profiles, skill-based matching, real-time messaging, debug logging