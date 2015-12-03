idea-php-annotation-plugin
==========================
[![Build Status](https://travis-ci.org/Haehnchen/idea-php-annotation-plugin.svg?branch=master)](https://travis-ci.org/Haehnchen/idea-php-annotation-plugin)
[![Version](http://phpstorm.espend.de/badge/7320/version)](https://plugins.jetbrains.com/plugin/7320)
[![Downloads](http://phpstorm.espend.de/badge/7320/downloads)](https://plugins.jetbrains.com/plugin/7320)
[![Downloads last month](http://phpstorm.espend.de/badge/7320/last-month)](https://plugins.jetbrains.com/plugin/7320)

Provides PHP annotation support for PhpStorm / IntelliJ and provides references support for "Code > Optimize Imports" action

### Install
* [Download plugin](http://plugins.jetbrains.com/plugin/7320) or install directly out of PhpStorm
* Force file reindex if necessary with: `File -> Invalidate Cache`

### Version

* 2.x: PhpStorm9
* 1.x: PhpStorm8

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

Plugins provides several extension points, which allows external plugins to provide additional. See some examples on [Symfony2 Plugin](https://github.com/Haehnchen/idea-php-symfony2-plugin/blob/master/META-INF/plugin.xml)

Example for extension points.

```java
<extensionPoints>
      <extensionPoint name="PhpAnnotationCompletionProvider" interface="de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider"/>
      <extensionPoint name="PhpAnnotationReferenceProvider" interface="de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider"/>
      <extensionPoint name="PhpAnnotationDocTagGotoHandler" interface="de.espend.idea.php.annotation.extension.PhpAnnotationDocTagGotoHandler"/>
      <extensionPoint name="PhpAnnotationDocTagAnnotator" interface="de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator"/>
</extensionPoints>

<extensions defaultExtensionNs="de.espend.idea.php.annotation">
  <PhpAnnotationExtension implementation="de.espend.idea.php.annotation.completion.PhpAnnotationTypeCompletionProvider"/>
</extensions>
```

### Completion confidence

Annoying pressing completion shortcut? Plugin provides a nice completion confidence to open completion popover on several conditions

```php
/**
 * @<caret>
 * <caret>
 */
```

### Static values
```php
    /**
     * @DI\Observe(SomethingEvents::PRE_UPDATE)
     */
```

### Doctrine

#### ORM: Property generator

```php
class Foo {
    public $id<caret>;

    /**
     * @ORM\Id
     * @ORM\GeneratedValue(strategy="AUTO")
     * @ORM\Column(type="integer")
     */
    public $id<caret>;
}
```

#### ORM: class entity generator

```php
/**
 * @ORM\Entity(repositoryClass="Foo")
 * @ORM\Table(name="bike")
 */
class Foo { }
```

#### ORM: repository class generator / intention

```php
/**
 * @ORM\Entity(repositoryClass="UnknownClass")
 */
class Foo { }
```

```php
/**
 * @ORM\Entity<caret>
 */
class Foo { }
```

#### ORM: repository class completion

```php
/**
 * @ORM\Entity(repositoryClass="<caret>")
 */
```

### PhpStorm9
* will see :)
