idea-php-annotation-plugin
==========================

Provides php annotation support for PhpStorm and IntelliJ

### Install
* [Download plugin](http://plugins.jetbrains.com/plugin/7320) or install directly out of PhpStorm
* Enabled Plugin for project: `Settings -> PHP -> Annotation`
* Force file reindex if necessary with: `File -> Invalidate Cache`

### Annotation Class Detection

* Every class with `@Annotation` inside class doc block is detected on file indexing
* Annotation Properties on property names
* Property value types
* @ENUM Tags

```php
/**
 * @Annotation
 */
class NotBlank extends Constraint {
    public $message = 'This value should not be blank.';
    public $groups = array();

    /**
     * @var Boolean
     */
    public $option = false;

    /**
     *
     * @Enum({"AUTO", "SEQUENCE", "TABLE", "IDENTITY", "NONE", "UUID", "CUSTOM"})
     */
    public $strategy = 'AUTO';

    /**
     * @var array<string>
     */
    public $cascade;

}
```

### Annotation Target Detection

`@Target` is used to attach annotation, if non provided its added to "ALL list"

```php
/**
 * @Annotation
 * @Target("PROPERTY", "METHOD", "CLASS", "ALL")
 */
class NotBlank extends Constraint {
    public $message = 'This value should not be blank.';
}
```

### Extension Points

React on completion and reference events inside annotation property values.

Example for extension points.

```java
<extensionPoints>
      <extensionPoint name="PhpAnnotationCompletionProvider" interface="de.espend.idea.php.annotation.PhpAnnotationCompletionProvider"/>
      <extensionPoint name="PhpAnnotationReferencesProvider" interface="de.espend.idea.php.annotation.PhpAnnotationReferencesProvider"/>
</extensionPoints>

<extensions defaultExtensionNs="de.espend.idea.php.annotation">
  <PhpAnnotationExtension implementation="de.espend.idea.php.annotation.extension.PhpAnnotationTypeProvider"/>
</extensions>
```

### Other
* Doctrine repositoryName
* Completion confidence to auto popup completion in docblocks