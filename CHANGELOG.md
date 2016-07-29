# Changelog

## Versions
* 4.x: PhpStorm 2016.1.2+
* 3.x: PhpStorm 2016.1+
* 2.x: PhpStorm9, 10
* 1.x: PhpStorm8

## 4.0
* Support nested annotations [#8](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/8), [#55](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/55)
* Add button in settings form to force reindex of annotation classes [#55](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/55)
* Drop project references in settings form; prevent memory leaks
* Java8 migration
* Use newest api level and migrate internal code usage

## 3.0.1
* Custom use aliases do not work after restarting the IDE [#54](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/54)

## 3.0
* Change minimal api level to PhpStorm 2016.1
* Replace deprecated PhpUse usages [#52](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/52) @artspb
* Add application level settings to set auto-complete without "()" [#42](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/42)
* Add option to always use an aliased import for some annotations or namespaces [#50](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/50)
* Doctrine orm class initialize should add an use alias
* Fix inline annotations are not recognized [#24](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/24)
* Replace deprecated PhpStorm method calls [#31](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/31)
* Doctrine repository creation intention is now available in attribute value

## 2.6.2
* Add PHP Toolbox support [#49](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/49)
* Implement integration tests for main plugin functionality

## 2.6.1

* Replace Doctrine static column types with parser to support simple_array and json_array [#39](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/39)
* On ORM annotations generation missing ORM alias will add automatically

## 2.6 / 1.6
* Add Doctrine repository create quickfix
* Add intention creation for Doctrine: @Column properties
* Add generator for Doctrine entity class and properties

## 1.5.1
* fix "missing import" inspection highlights all doc blocks #25

## 1.5
* Fix property value pattern completion for PhpStorm8
* Add whitelist for annotation PhpDoc on next siblings, to filter inline doc blocks [#24](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/24)
* Move "Missing Import" annotator to inspection [#19](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/19)

## 1.4
* API: add new proxy method "getPropertyValue" to get representing psielements
* Completion and goto for class constants inside doctags [#18](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/18)
* Annotation are now valid in namespace less files; this also brings some performance improvements because of lower search scope

## 1.3
* Typo fix that class completion are detected as method
* Attach annotation insertHandler for alias completion
* Rename getRootValue to more naming getDefaultPropertyValue
* Remove unused configuration form [#15](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/15)
* Fix that docblock completion confidence is valid in comments and provides autopopup

## 1.2
* Fix annotation class inside composer libraries where not autocompleted [#13](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/13)
* Support fqn classes annotation [#17](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/17)
* Add java annotations for all extension points
* Pipe additional util methods in extension parameters

## 1.1
* Add doc tag property value utils and dicts
* Add alias annotation class completion
* Improve performance on annotation class completion
* Use php class statement scope instead of file scope for namespace collection

## 1.0.1
* Detect annotation classes in same namespace

## 1.0
* Remove all PhpStorm6 hacks, support new Api and only allow PhpStorm7 builds
* Add more property value type detections
* Add and change extension points to reflect latest api features
* Add class import annotator
* Add docblock property value provider for @Enum
* Add Doctrine providers for: targetEntity, repositoryClass, mappedBy, inversedBy
* Migrate pattern to allow multiple docblocks [#12](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/12), [#8](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/8)
* Plugin dont need to explicit enable

## 0.4
* Only support PhpStorm > 7
* Activate annotation class reference provider and use it where possible

## 0.3
* Pattern fix to support eap 131.235
* Support "@" completion WI-20265
* Optimize doc tag name insert handler

## 0.2
* Better property completion pattern and type detection
* Optimize property value insert handler
* Extension points for property goto and completion eg @Template("file.html.twig")

## 0.1
* Init version support PhpStorm6 and 7