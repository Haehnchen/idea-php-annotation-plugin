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

### Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "fr.adrienbrault.idea.symfony2plugin.tests.dic.SymfonyContainerTypeProviderTest"

# Run tests matching a pattern
./gradlew test --tests "*ContainerTest"
```

## Architecture

- Tests extend `AnnotationLightCodeInsightFixtureTestCase` which provides a test fixture framework.

Test data files are in `src/test/java/de/espend/idea/php/annotation/tests/fixtures/`.


## Decompiler Tools

For analyzing bundled plugins like Twig and PHP you MUST use **vineflower** and NOT **Fernflower** from IntelliJ (less quality):

**vineflower**

- **GitHub:** https://github.com/Vineflower/vineflower
- **Download:** https://repo1.maven.org/maven2/org/vineflower/vineflower/1.11.2/vineflower-1.11.2.jar
- **Local copy:** `decompiled/vineflower.jar`
- **Usage:** `java -jar vineflower.jar input.jar output/`

**Bundled Plugin JARs (for decompilation):**
- **Location:** `~/.gradle/caches/[gradle-version]/transforms/*/transformed/com.jetbrains.[plugin]-[intellij-version]/[plugin]/lib/[plugin].jar`
- **Example:** `~/.gradle/caches/9.3.0/transforms/*/transformed/com.jetbrains.twig-253.28294.322/twig/lib/twig.jar`
