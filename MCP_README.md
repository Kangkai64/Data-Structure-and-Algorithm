# MCP Configuration for Clinic Management System

## Overview
This project has been configured with Model Context Protocol (MCP) to enhance AI-assisted development capabilities.

## Configuration Files

### `.cursorrules`
Defines project-specific rules and conventions for AI assistance:
- Project architecture and structure
- Coding conventions and patterns
- Technology stack information
- Security considerations

### `.cursorignore`
Specifies files and directories to exclude from AI analysis:
- Build artifacts (`target/`, `*.class`, `*.jar`)
- IDE files (`.idea/`, `*.iml`)
- Temporary and system files

### `cursor.json`
MCP configuration with:
- Project metadata
- Context inclusion/exclusion patterns
- AI preferences and focus areas
- Development environment settings

## Project Structure
```
src/main/java/
├── entity/     # Data models and entities
├── control/    # Business logic and controllers
├── boundary/   # User interface components
├── dao/        # Data Access Objects
├── utility/    # Helper classes
└── adt/        # Abstract Data Types
```

## MCP Features Enabled

### Context Awareness
- Full source code analysis
- Project structure understanding
- Architecture pattern recognition
- Database schema awareness

### Tool Integration
- File operations (read, write, create, delete)
- Terminal command execution
- Code analysis and refactoring
- Dependency management

### AI Preferences
- Java 23 syntax and features
- Layered architecture patterns
- MySQL database operations
- Maven build system

## Usage Guidelines

### For AI Assistance
1. The AI understands your project's layered architecture
2. It can work with entity, control, boundary, dao, utility, and adt packages
3. It follows Java naming conventions and best practices
4. It's aware of your MySQL database and HikariCP connection pooling

### Development Workflow
1. **Entity Layer**: Define data models and entities
2. **DAO Layer**: Implement data access operations
3. **Control Layer**: Add business logic and validation
4. **Boundary Layer**: Create user interfaces
5. **Utility Layer**: Add helper functions and utilities
6. **ADT Layer**: Implement custom data structures

### Database Operations
- Use prepared statements for all database queries
- Implement proper connection pooling with HikariCP
- Handle transactions appropriately
- Follow the DAO pattern consistently

## Best Practices
- Keep business logic in the control layer
- Use entity classes for data transfer
- Implement proper input validation
- Follow the established naming conventions
- Add appropriate error handling

## Troubleshooting
If MCP features aren't working as expected:
1. Check that all configuration files are in the project root
2. Ensure Cursor is up to date
3. Restart Cursor to reload MCP configuration
4. Verify that the project structure matches the expected layout 