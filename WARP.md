# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

PhoenixBot is a Discord bot built with Java 21 using JDA (Java Discord API) for handling Discord interactions. The bot provides ticket management, voting systems, FAQ handling, and support for "The Broken Script" modpack community.

## Development Commands

### Build & Run
- **Build**: `.\gradlew.bat build` (Windows) or `./gradlew build` (Unix)
- **Run**: `.\gradlew.bat run` (Windows) or `./gradlew run` (Unix)
- **Build Shadow JAR**: `.\gradlew.bat shadowJar` (Windows) or `./gradlew shadowJar` (Unix)

### Testing
- **Run all tests**: `.\gradlew.bat test` (Windows) or `./gradlew test` (Unix)
- **Run specific test**: `.\gradlew.bat test --tests "TestClassName"` (Windows) or `./gradlew test --tests "TestClassName"` (Unix)

### Development
- **Clean build**: `.\gradlew.bat clean build` (Windows) or `./gradlew clean build` (Unix)
- **Continuous build**: `.\gradlew.bat build --continuous` (Windows) or `./gradlew build --continuous` (Unix)

## Architecture Overview

### Core Components

#### Configuration System
- **Config.java**: Central configuration management using YAML (SnakeYAML)
- **config.yml**: Auto-generated configuration file with Discord IDs and bot settings
- Singleton pattern for configuration access with automatic default config creation
- Supports hot-reloading via `/reloadconfig` command

#### Storage Layer
- **VoteStorage.java**: JSON-based persistent storage for suggestion voting data
- **TicketStorage.java**: JSON-based storage for ticket management with global counters
- Uses Jackson for JSON serialization/deserialization
- Thread-safe synchronized operations for concurrent access

#### Event-Driven Architecture
The bot follows JDA's event-driven pattern with specialized listener classes:

**Core Listeners:**
- **Listener.java**: Main ready event handler, command registration, and bot initialization
- **SupportListener.java**: Handles support forum thread creation and closing
- **SuggestionListener.java**: Manages voting on suggestion forum posts
- **BugReportListener.java**: Processes bug report submissions
- **ThreadDeleteListener.java**: Cleanup when threads are deleted

**Ticket System:**
- **Panel.java**: Ticket panel creation, button interactions, and automated ticket lifecycle
- **TicketCloseHandler.java**: Handles ticket closing confirmations and cleanup
- **CloseHandler.java**: Generic close handling for forum posts

#### Command System
- **CommandRegistry.java**: Central slash command registration
- Individual command classes (InfoCommand, FAQCommand, TBSCommand, CloseCommand)
- Commands are registered as event listeners in the main Bot class

### Data Flow

1. **Bot Initialization**: Config loads → Storage initializes → Commands register → Event listeners attach
2. **Ticket Creation**: User clicks panel button → Thread creates → Storage saves → Auto-delete schedules
3. **Ticket Response**: User responds → Auto-delete cancels → Moderators ping → Storage updates
4. **Vote Management**: User votes → Storage updates → Vote counts sync with Discord reactions
5. **Configuration Changes**: `/reloadconfig` → Config reloads → All components refresh

### Key Design Patterns

#### Singleton Pattern
- **Config**: Single instance with lazy initialization and reload capability

#### Observer Pattern
- JDA event listeners for Discord events (messages, interactions, thread events)

#### Strategy Pattern
- Different handlers for different forum types (support, suggestions, bug reports)

#### Factory Pattern
- Ticket name generation with global counters and username sanitization

### Storage Architecture

#### Persistent Storage
- **votes.json**: Stores vote data per thread with user voting history
- **tickets.json**: Stores ticket metadata, creation times, and response status
- **config.yml**: Bot configuration with Discord channel/role IDs

#### In-Memory Storage
- **pendingTickets**: ConcurrentHashMap tracking scheduled ticket deletions
- **scheduler**: ScheduledExecutorService for automated ticket cleanup

### Integration Points

#### Discord Integration
- **JDA Library**: Main Discord API wrapper
- **Slash Commands**: Modern Discord interaction system
- **Forum Channels**: Specialized channel type handling for tickets/suggestions
- **Thread Management**: Automatic thread creation and lifecycle management

#### External Dependencies
- **Jackson**: JSON processing for storage operations
- **SnakeYAML**: YAML configuration parsing
- **Logback**: Structured logging with custom Discord-friendly formatting
- **Apache HttpClient**: HTTP operations for log uploading

## Configuration Notes

The bot expects a `config.yml` file in the root directory. If missing, it auto-generates a template. Key sections:
- **bot**: Token, activity, owner ID, guild ID
- **logging**: Log channel configuration
- **voting/support/bugReport**: Forum channel IDs
- **roles**: Moderator roles for permissions
- **faq**: FAQ channel and entries configuration
- **tickets**: Ping roles for ticket notifications

## Development Guidelines

### Adding New Commands
1. Create command class extending ListenerAdapter
2. Implement onSlashCommandInteraction method
3. Add command to CommandRegistry.registerCommands()
4. Register listener in Bot.main() event listeners list

### Adding New Storage
1. Create storage class following VoteStorage/TicketStorage patterns
2. Use synchronized methods for thread safety
3. Initialize in Bot.initStorage() method
4. Add getter method to Bot class for global access

### Forum Integration
- Each forum type (support, suggestions, bugs) has dedicated listener
- Thread creation triggers automatic embed and button setup
- Use ThreadChannel.getId() for unique identification
- Always check forum channel IDs against configuration

### Error Handling
- Use SLF4J logging with Bot.class logger reference
- Handle IOException for all storage operations
- Provide user-friendly error messages for Discord interactions
- Log errors with context (thread ID, user ID, action attempted)