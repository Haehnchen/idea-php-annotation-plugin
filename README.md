idea-php-annotation-plugin
==========================

Provides php annotation support for PhpStorm and IntelliJ

### Annotation Class Detection

* Every class with `@Annotation` inside class doc block is detected on file indexing
* Annotation Properties on property names
* Property value types on default type

```php
/**
 * @Annotation
 */
class NotBlank extends Constraint {
    public $message = 'This value should not be blank.';
    public $groups = array();    
}
```

### Annotation Target Detection
```php
`@Target` is used to attach annotation, if non provided its added to "ALL list"
/**
 * @Annotation
 * @Target("PROPERTY", "METHOD", "CLASS", "ALL")
 */
class NotBlank extends Constraint {
    public $message = 'This value should not be blank.';
}
```