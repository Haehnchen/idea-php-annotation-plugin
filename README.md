IntelliJ IDEA / PhpStorm PHP Annotations
==========================
[![Build Status](https://github.com/Haehnchen/idea-php-annotation-plugin/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/Haehnchen/idea-php-annotation-plugin/actions/workflows/gradle.yml)
[![Version](http://phpstorm.espend.de/badge/7320/version)](https://plugins.jetbrains.com/plugin/7320)
[![Downloads](http://phpstorm.espend.de/badge/7320/downloads)](https://plugins.jetbrains.com/plugin/7320)
[![Downloads last month](http://phpstorm.espend.de/badge/7320/last-month)](https://plugins.jetbrains.com/plugin/7320)
[![Donate to this project using Paypal](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.me/DanielEspendiller)

Provides PHP annotation support for PhpStorm / IntelliJ IDEA and references for "Code > Optimize Imports" action. Code extraction of [Symfony Plugin](https://github.com/Haehnchen/idea-php-symfony2-plugin)

Key         | Value
----------- | -----------
Plugin url  | https://plugins.jetbrains.com/plugin/7320
Id          | de.espend.idea.php.annotation
Changelog   | [CHANGELOG](CHANGELOG.md)

### Install
* [Download plugin](http://plugins.jetbrains.com/plugin/7320) or install directly out of PhpStorm
* Force file reindex if necessary with: `File -> Invalidate Cache`

### Settings

`Languages & Framework > PHP > Annotations`

#### Round brackets

```php
/**
 * @Foo<caret>()
 * @Foo<caret>
 */
class NotBlank extends Constraint {}
```

#### Use / Import alias

`Languages & Framework > PHP > Annotations -> Use Alias`

```php

use Doctrine\ORM\Mapping as ORM;

/**
 * @Id() -> @ORM\Id()
 */
class Foo {}
```

#### Class LineMarker

LineMarker which provide navigation to annotation class usages

```php
namespace Doctrine\ORM\Mapping;

/**
 * @Annotation
 */
final class Entity implements Annotation
{
}
```

Targeting

```php

/**
 * @ORM\Entity()
 */
```

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
     * @var bool|boolean
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
    
    /**
     * @var mixed|foobar|bool
     */
    public $mixed;
}
```

https://www.doctrine-project.org/projects/doctrine-annotations/en/latest/custom.html#attribute-types

```php
/**
 * @Annotation
 *
 * @Attributes({
 *   @Attribute("stringProperty", type = "string"),
 *   @Attribute("annotProperty",  type = "bool"),
 * })
 */
 *
 * @Attributes(
 *   @Attribute("stringProperty", type = "string"),
 *   @Attribute("annotProperty",  type = "bool"),
 * )
 */
class Foobar {}
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

```xml
<extensionPoints>
      <extensionPoint name="PhpAnnotationCompletionProvider" interface="de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider"/>
      <extensionPoint name="PhpAnnotationReferenceProvider" interface="de.espend.idea.php.annotation.extension.PhpAnnotationReferenceProvider"/>
      <extensionPoint name="PhpAnnotationDocTagGotoHandler" interface="de.espend.idea.php.annotation.extension.PhpAnnotationDocTagGotoHandler"/>
      <extensionPoint name="PhpAnnotationDocTagAnnotator" interface="de.espend.idea.php.annotation.extension.PhpAnnotationDocTagAnnotator"/>
      <extensionPoint name="PhpAnnotationGlobalNamespacesLoader" interface="de.espend.idea.php.annotation.extension.PhpAnnotationGlobalNamespacesLoader"/>
      <extensionPoint name="PhpAnnotationVirtualProperties" interface="de.espend.idea.php.annotation.extension.PhpAnnotationVirtualProperties"/>
      
      <!-- Custom class alias mapping: "ORM" => "Doctrine\\ORM\\Mapping" -->
      <extensionPoint name="PhpAnnotationUseAlias" interface="de.espend.idea.php.annotation.extension.PhpAnnotationUseAlias"/>
</extensionPoints>
```

Usage

```xml
<extensions defaultExtensionNs="de.espend.idea.php.annotation">
  <PhpAnnotationExtension implementation="de.espend.idea.php.annotation.completion.PhpAnnotationTypeCompletionProvider"/>
</extensions>
```

### PHP Attributes Bridge

All extensions points providing support to DocBlock and PHP Attributes at the same time.   

```php
use Symfony\Component\Validator\Constraints\NotBlank;

#[NotBlank(message: 'An empty file is not allowed.')]
/* @NotBlank({message: "An empty file is not allowed."}) */
```

```php
#[Template('foo.html.twig')]
/* @Template("foo.html.twig") */
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

### PHP Toolbox

Provides integration for [PHP Toolbox](https://github.com/Haehnchen/idea-php-toolbox)

#### Default and property values

```php
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route("<caret>")
 * @Route(condition="<caret>")
 */
```

```javascript
{
  "registrar":[
    {
      "signatures":[
        {
          "class": "Symfony\\Component\\Routing\\Annotation\\Route",
          "type": "annotation"
        },
        {
          "class": "Symfony\\Component\\Routing\\Annotation\\Route",
          "field": "condition",
          "type": "annotation"
        }
      ],
      "provider":"foo",
      "language":"php"
    }
  ],
}
```

#### Property array values

```php
use Symfony\Component\Routing\Annotation\Route;

/**
 * @Route(methods={"<caret>"})
 */
```

```
{
  "registrar":[
    {
      "language":"php",
      "provider":"methods",
      "signatures":[
        {
          "class": "Symfony\\Component\\Routing\\Annotation\\Route",
          "type": "annotation_array",
          "field": "methods"
        }
      ]
    }
  ],
  "providers": [
    {
      "name": "methods",
      "items":[
        {
          "lookup_string": "POST"
        }
      ]
    }
  ]
}
```