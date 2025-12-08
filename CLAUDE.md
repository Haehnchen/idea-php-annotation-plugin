# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an IntelliJ IDEA/PhpStorm plugin that provides PHP annotation and PHP 8 Attribute support. The plugin extends the IDE to recognize annotation classes marked with `@Annotation`, provides code completion, navigation, inspections, and integrates with Doctrine ORM and Symfony frameworks.

**Plugin ID**: `de.espend.idea.php.annotation`

## Build Commands

### Build the plugin
```bash
./gradlew buildPlugin
```
The plugin ZIP will be in `build/distributions/`.

### Run tests
```bash
./gradlew test
```

### Run a single test class
```bash
./gradlew test --tests "de.espend.idea.php.annotation.tests.AnnotationStubIndexTest"
```

### Run a single test method
```bash
./gradlew test --tests "de.espend.idea.php.annotation.tests.AnnotationStubIndexTest.testThatAnnotationClassIsInIndex"
```

### Run the plugin in a test IDE instance
```bash
./gradlew runIde
```

### Verify plugin compatibility
```bash
./gradlew verifyPlugin
```

### Clean build
```bash
./gradlew clean buildPlugin
```

## Architecture

### Extension Point System

The plugin's core architecture is based on extension points that allow both internal components and external plugins (like Symfony Support or PHP Toolbox) to extend functionality.

**Key extension point interfaces** (in `src/main/java/de/espend/idea/php/annotation/extension/`):

- **PhpAnnotationCompletionProvider**: Provides code completion for annotation property values
- **PhpAnnotationReferenceProvider**: Provides references and navigation for annotation elements
- **PhpAnnotationDocTagGotoHandler**: Handles "Go to Declaration" for annotation tags
- **PhpAnnotationDocTagAnnotator**: Provides custom highlighting/error annotations for doc tags
- **PhpAnnotationGlobalNamespacesLoader**: Loads global annotation namespace mappings
- **PhpAnnotationVirtualProperties**: Provides virtual properties for annotation classes
- **PhpAnnotationUseAlias**: Maps custom class aliases (e.g., "ORM" => "Doctrine\\ORM\\Mapping")

Extension points are registered in `src/main/resources/META-INF/plugin.xml` and can be used by other plugins.

### Indexing System

The plugin uses two main file-based indices for fast lookup:

- **AnnotationStubIndex**: Indexes all PHP classes marked with `@Annotation` in their doc block
- **AnnotationUsageIndex**: Indexes where annotations are used in the codebase

These indices power the navigation features (find usages, line markers) and are updated automatically when files change.

### Module Organization

- **annotator/**: Provides inline error highlighting and warnings
- **completion/**: Code completion contributors and providers
- **dict/**: Data transfer objects and dictionary classes
- **doctrine/**: Doctrine ORM-specific features (property generators, repository class handling, column type support)
- **extension/**: Extension point interfaces and parameters
- **inspection/**: Code inspections (missing imports, deprecated usage, etc.)
- **navigation/**: Navigation handlers and line marker providers
- **pattern/**: PSI pattern matching for identifying annotation contexts
- **reference/**: Reference contributors for navigation and "Find Usages"
- **symfony/**: Symfony-specific annotation support
- **toolbox/**: PHP Toolbox integration
- **ui/**: Settings forms and configuration UI
- **util/**: Utility classes, particularly `AnnotationUtil` which contains core logic for annotation detection and processing

### Dual Support: DocBlock Annotations and PHP 8 Attributes

The plugin supports both:
- **DocBlock annotations**: `/** @Route("/path") */`
- **PHP 8 Attributes**: `#[Route('/path')]`

Extension points work transparently with both formats, allowing feature implementations to support both simultaneously.

## Key Concepts

### Annotation Detection

Classes are recognized as annotation classes if they have `@Annotation` in their doc block:
```php
/**
 * @Annotation
 * @Target("METHOD", "CLASS")
 */
class Route {
    public $path;
}
```

### Target Filtering

The `@Target` annotation restricts where annotations can be used (METHOD, CLASS, PROPERTY, ALL). The plugin uses this for completion filtering.

### Property Type Detection

Annotation properties support type hints via doc comments:
- Simple types: `@var string`, `@var bool`
- Arrays: `@var array<string>`
- Enums: `@Enum({"GET", "POST", "PUT"})`
- Mixed: `@var mixed|string|bool`

### Use Alias System

The plugin supports configurable namespace aliases (Settings > PHP > Annotations / Attributes > Use Alias):
- Maps short names to FQCNs: `ORM` => `Doctrine\ORM\Mapping`
- Auto-import suggestions use these mappings
- External plugins can provide their own mappings via `PhpAnnotationUseAlias` extension point

## Test Structure

Tests extend `AnnotationLightCodeInsightFixtureTestCase` which provides a test fixture framework.

Test data files are in `src/test/java/de/espend/idea/php/annotation/tests/fixtures/`.

Tests use the `myFixture` field to:
- Copy test PHP files: `myFixture.copyFileToProject("classes.php")`
- Check completion: `myFixture.completeBasic()`
- Navigate: `myFixture.getReferenceAtCaretPosition()`
- Assert index contents: `assertIndexContains(AnnotationStubIndex.KEY, "My\\Class")`

## Configuration

Build configuration is in `gradle.properties`:
- `platformVersion`: Target IntelliJ/PhpStorm version (currently 2025.2.5)
- `pluginSinceBuild` / `pluginUntilBuild`: Supported IDE version range
- `javaVersion`: Java language level (21)

The plugin requires these IntelliJ plugins as dependencies:
- `com.jetbrains.php` (PhpStorm PHP support)
- `com.jetbrains.twig` (Twig template support)
- `com.intellij.modules.json` (JSON support)

Optional integration:
- `de.espend.idea.php.toolbox` (PHP Toolbox)

## Publishing

See `MAINTENANCE.md` for the full release process. Key steps:
1. Update `CHANGELOG.md`
2. Commit changes
3. Tag release: `git tag X.Y.Z`
4. Build: `./gradlew clean buildPlugin`
5. Publish: `IJ_TOKEN=yourtoken ./gradlew publishPlugin`
