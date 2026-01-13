<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Earth Develop Tool** is an IntelliJ IDEA plugin providing a collection of developer utilities. It's a Gradle-based Java project that uses the JetBrains IntelliJ Plugin SDK.

- **Plugin ID**: com.myth.earth.earth-develop-tool
- **Current Version**: 1.4
- **Target IDE**: IntelliJ 2022.1+
- **Language**: Java
- **Build System**: Gradle

## Build Commands

### Build the plugin
```bash
./gradlew buildPlugin
```

### Run tests
```bash
./gradlew test
```

### Run a single test
```bash
./gradlew test --tests com.myth.earth.develop.path.TestClassName
```

### Run the plugin in sandbox (for local testing)
```bash
./gradlew runIde
```

### Copy built plugin to Documents folder (custom task)
```bash
./gradlew copyPluginZip
```

### Clean build artifacts
```bash
./gradlew clean
```

## Code Architecture

### Plugin Extension Points

The plugin integrates with IntelliJ through extensions defined in `src/main/resources/META-INF/plugin.xml`:

- **Status Bar Widget**: Displays toolkit access in the IDE status bar (DevelopToolStatusBarWidgetFactory)
- **Project Service**: ToolkitProjectService manages toolkit state per project
- **Action Handlers**:
  - QuickOpenToolKitAction - Opens toolkit via keyboard shortcut (Alt+F2 Windows/Linux, Option+F2 macOS)
  - GenerateSQLAction - Right-click menu in console to convert MyBatis logs to SQL

### Plugin Architecture

**Three-layer structure**:

1. **Core Framework** (`ui/toolkit/core/`)
   - `ToolView`: Base interface for all tools
   - `ToolCategory`: Enum organizing tools by category
   - `Tool`: Annotation marking ToolView implementations
   - `ToolkitLoader`: Reflection-based dynamic tool discovery from classpath

2. **Loader System** (`ui/toolkit/ToolkitLoader.java`)
   - Automatically discovers all classes with `@Tool` annotation
   - Supports both file system and JAR-packaged classes
   - Implements lazy initialization and instance caching via ConcurrentHashMap
   - Thread-safe instance creation with double-checked locking

3. **UI Components** (`ui/toolkit/`)
   - `ToolkitProjectService`: Project-level service managing toolkit state
   - `ToolMainPopupPanel`: Main popup UI displaying categorized tools
   - `AbstractToolView`: Base class for tool UI implementations

### Tools Organization

Each tool is a class annotated with `@Tool` that extends `AbstractToolView`:

```java
@Tool(
    category = ToolCategory.ENCRYPTION,
    name = "Base64",
    description = "Base64 Encoding/Decoding",
    iconPath = "icons/..."
)
public class Base64ToolViewImpl extends AbstractToolView {
    // Tool implementation
}
```

Tools are categorized into groups (ENCRYPTION, CONVERSION, COMPARISON, etc.) and automatically discovered by `ToolkitLoader`.

**Available Tools** (in `ui/toolkit/views/`):
- Base64ToolViewImpl
- TextEnDecodeToolViewImpl (supports AES, others)
- TimestampToolViewImpl
- UUIDToolViewImpl
- JsonFormatToolViewImpl, JsonDiffViewImpl
- RadixConversionToolViewImpl
- DruidToolViewImpl (Druid connection pool encryption)
- PropertiesDiffViewImpl, YamlDiffViewImpl, XmlDiffViewImpl
- StringEscapeViewImpl
- SqlConverterToolViewImpl (MyBatis log conversion)

### Key Dependencies

- **fastjson (1.2.83)**: JSON parsing and serialization
- **hutool (5.8.35)**: Utilities for core and cryptography
- **snakeyaml (2.0)**: YAML parsing and manipulation
- **commons-text (1.14.0)**: Text utilities (escaping, etc.)
- **lombok (1.18.20)**: Java code generation (annotations)
- **JUnit 5**: Testing framework

### Custom UI Components

Located in `ui/component/`:
- `CardPanel`: Styled container for tool sections
- `LabelTextField`: Text field with associated label
- `MyEditorTextField`: Enhanced editor field from IntelliJ
- `EarthSupportPanel`: Custom support panel
- `CollapsibleTitledSeparator`: Collapsible section divider
- `CopyableMessageDialog`: Dialog with copy-to-clipboard capability

### Utility Classes

- **kit/**: Plugin utilities
  - `ClipboardKit`: Clipboard operations
  - `PluginNotifyKit`: User notifications
- **service/**: Core service implementations
  - `MybatisLogParser`: Parses MyBatis logs to SQL
- **utils/**: Comparison and conversion utilities
  - `JsonCompareUtil`, `XmlCompareUtil`, `YamlCompareUtil`, `PropertiesCompareUtil`
  - `UnicodeUtil`, `KeyPathBuilder`

## Testing

Tests are configured to use JUnit 5 (Jupiter). Place test files in `src/test/java/` using the same package structure as source code. Tests run with:

```bash
./gradlew test
```

## Adding a New Tool

1. Create a new class extending `AbstractToolView` in `src/main/java/com/myth/earth/develop/ui/toolkit/views/`
2. Annotate with `@Tool`, specifying category, name, description, and optional icon path
3. Implement required UI and functionality methods
4. The `ToolkitLoader` will automatically discover and register it

Example structure:
```java
@Tool(category = ToolCategory.CONVERSION, name = "MyTool", description = "...", iconPath = "icons/mytool.svg")
public class MyToolViewImpl extends AbstractToolView {
    public MyToolViewImpl(Project project) {
        super(project);
    }

    @Override
    protected void initializeUI() {
        // UI setup
    }
}
```

## IDE Integration Notes

- Plugin targets IntelliJ 2022.1+ (IJ version 212.4746.92+)
- Depends on `com.intellij.modules.platform` and `com.intellij.modules.json`
- Plugin XML uses standard IntelliJ extension points
- For UI development, use IntelliJ's Swing-based UI components

## UTF-8 Encoding

All Java compilation is explicitly set to UTF-8 encoding (build.gradle line 48). This is important for Chinese characters in the codebase.
