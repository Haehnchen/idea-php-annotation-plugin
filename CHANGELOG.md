# Changelog

## Versions
* 9.x: PhpStorm 2022.3+
* 8.x: PhpStorm 2020.3+ (no support)
* 7.x: PhpStorm 2020.1+ (no support)
* 6.x: PhpStorm 2019.1+ (no support)
* 5.x: PhpStorm 2017.1+ (no support)
* 4.x: PhpStorm 2016.1.2+ (no support)
* 3.x: PhpStorm 2016.1+ (no support)
* 2.x: PhpStorm9, 10 (no support)
* 1.x: PhpStorm8 (no support)

## 9.2.1
* Fix path detection for repository and support adding "()" for annotation inserts (Daniel Espendiller)
* Fix for preview feature "Add Doctrine repository": "Cannot invoke "com.intellij.psi.PsiDirectory.getParentDirectory()" because the return value of "com.intellij.psi.PsiFile.getContainingDirectory()" is null" (Daniel Espendiller)

## 9.2.0
* [#266](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/266) provide attribute alias support for completion (Daniel Espendiller)
* Provide extension points language tags (Daniel Espendiller)
* Replace some deprecated API usages (Daniel Espendiller)

## 9.1.0
* Improve index performance (Daniel Espendiller)
* Support "Attribute" usages resolving for linemarker (Daniel Espendiller)

## 9.0.0
* Support attributes for Entity::repository (Daniel Espendiller)
* Add generator for repositoryClass (Daniel Espendiller)
* Fix some detection issue for attribute in inspection, indention and generator (Daniel Espendiller)
* Provide template for Symfony server repository and use if bundle exists (Daniel Espendiller)
* Java language level code migration (Daniel Espendiller)
* Update gradle / build infrastructure to 2022.3.2 (Daniel Espendiller)

## 8.3.0
* Support "Embeddable" as attributes for generator (Daniel Espendiller)
* Support attribute generation for entities on class scope (Daniel Espendiller)
* Support attribute for generate Doctrine fields ("Add Doctrine column") (Daniel Espendiller)
* Add abstraction for attribute default completion extension (Daniel Espendiller)
* Support deprecation Doctrine field types for attributes https://github.com/Haehnchen/idea-php-symfony2-plugin/issues/2018 (Daniel Espendiller)

## 8.2.3
* fix: rename toolbox.xml to avoid name collisions, fixes #248 (Shyim)

## 8.2.2
* Support Doctrine completion and navigation inside "mappedBy" and "inversedBy" php attribute: https://github.com/Haehnchen/idea-php-symfony2-plugin/issues/1653 (Daniel Espendiller)

## 8.2.1
* Add plugin error submitter (Daniel Espendiller)
* Add common "OpenApi\Annotations" alias (Daniel Espendiller)

## 8.2.0
* [#204](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/204) Annotation with identical name to a method name is found as usage (Daniel Espendiller)
* [#209](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/209) [#222](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/222) move annotation completion to top of completion (Daniel Espendiller)
* [#237](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/237) attribute value completion for single property values should work as expected (Daniel Espendiller)
* [#237](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/237) onDelete should complete only on single value (Daniel Espendiller)

## 8.1.1
* Use new toolbox version (Daniel Espendiller)
* Move toolbox extension to an optional config (Daniel Espendiller)
* Dynamic plugin support (Daniel Espendiller)

## 8.1.0
* Fix Doctrine column type is not an array to completion (Daniel Espendiller)
* Update build to IU-2021.3 (Daniel Espendiller)
* [#230](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/230) Embedded classes => @ORM\Embeddable instead of @ORM\Embedded (Daniel Espendiller)
* Allow plugin to be dynamic (Daniel Espendiller)

## 8.0.0
* Provide attribute default value "navigation / references" bridge for PHP8 Attributes (Daniel Espendiller)
* Provide attribute property value "navigation / references" bridge for PHP8 Attributes (Daniel Espendiller)
* Provide array completion bridge for PHP8 Attributes (Daniel Espendiller)

## 7.1.3
* Remove class existing inspection for too much noise (Daniel Espendiller)
* Support private properties again as is commonly used for navigation and documentation purpose #205 (Daniel Espendiller)

## 7.1.2
* Stop marking keywords for BDD [#202](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/202) (Daniel Espendiller)

## 7.1.1
* Fine tuning for annotation blacklist [#199](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/199) (Daniel Espendiller)

## 7.1.0
* Provide IntelliJ platform annotation icon for annotation classes [#67](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/67) (Daniel Espendiller)
* Provide plugin url for vendor link [#155](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/155) (Daniel Espendiller)
* Fix: "java.lang.IllegalArgumentException: Argument for @NotNull parameter must not be null" on indexing [#154](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/154) (Daniel Espendiller)
* Support class constant navigation on same namespace and refactor import scope usage [#165](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/165) (Daniel Espendiller)

## 7.0.2
* Fix "Argument for @NotNull parameter .../dict/AnnotationPropertyEnum.fromString must not be null" [#194](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/194) (Daniel Espendiller)

## 7.0.1
* Fix PHPStorm @noinspection causes Missing Import warning [#190](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/190) (Daniel Espendiller)

## 7.0.0
* Smarter ORM column field detection for insert @Column tag (Daniel Espendiller)
* Optimize imports and its references should also take into account constants with namespaces (Daniel Espendiller)
* Support attribute types also on its attribute values (Daniel Espendiller)
* Provide type detection on column also on the Field type it self to support typed properties of PHP (Daniel Espendiller)
* Add property suggestions from Doctrine @Attribute annotations [#112](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/112) (https://www.doctrine-project.org/projects/doctrine-annotations/en/latest/custom.html#attribute-types) (Daniel Espendiller)
* Hide non public properties on DocBlock attribute list completion [#139](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/139) (Daniel Espendiller)
* Provide deprecated hint on importing class on DocBlock [#163](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/163) (Daniel Espendiller)
* Provide inspections for deprecated constant and class constant in DocBlocks (Daniel Espendiller)
* Prefix inspection messages (Daniel Espendiller)
* Support namespace in class constant annotation string (Daniel Espendiller)
* Provide importing annotation classes based on the alias (Daniel Espendiller)
* Provide inspection to check that a class behind a class constant in DocBlocks exists (Daniel Espendiller)
* Provide inspection to check if there is a real class behind a annotation doc block based on the use statements (Daniel Espendiller)
* DocBlock annotations should also check alias and check for a valid import; provide lazy and higher the scope for DocBlock to be the parent document reducing element processing (Daniel Espendiller)
* Drop support older PhpStorm versions (Daniel Espendiller)

## 6.3.0
* Provide annotation deprecation inspection for Doctrine column (Daniel Espendiller)
* Insert Embeddable instead of Embedded annotation for class (Konstantin Myakshin)
* Strikeout whole LookupElement, if deprecated, as PHP plugin does (Cedric Ziel)
* Assign lower priority to deprecated classes' LookupElements (Cedric Ziel)

## 6.2.2
* Present deprecated annotation items as striked-out (Cedric Ziel) [#157](https://github.com/Haehnchen/idea-php-annotation-plugin/pull/157)

## 6.2.1
* Fix duplicated file template preventing saving (Jack Bentley) [#152](https://github.com/Haehnchen/idea-php-annotation-plugin/pull/152)

## 6.2
* Update entity repository template to closer match maker bundle (Jack Bentley)
* Update to intelligently select correct repository directory (Jack Bentley)
* Fix unable to find template (Jack Bentley)
* Update quick action to show when existing repository class annotation is invalid (Jack Bentley)
* Update doctrine repository class generation to use a file template (Jack Bentley)

## 6.1
* Insert import must be full fqn name (Daniel Espendiller)
* Add inspection for deprecated annotations (Cedric Ziel) [#123](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/123) [#149](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/149)

## 6.0
* Replace deprecated code usages and drop support for old PhpStorm versions (Daniel Espendiller) [#148](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/148)
* Add IntelliJ plugin icon (Daniel Espendiller) [#145](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/145)
* WI-46553 Register PhpDocIdentifierReference on doc token instead of registering it on parent doc tag and adjusting the range (Kirill Smelov) [#143](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/143)

## 5.3
* Support latest EAP version [#107](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/107) [#105](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/105) @vkhramtsov
* Add new api to access class constants psi element in PhpDocTag properties

## 5.2.1
* Add extension point: Create use alias setting from third party plugin [#99](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/99) [#97](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/97)
* Add alias for Swagger-PHP [#96](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/96) @derrabus

## 5.2
* API: add annotation util to access property and default values of PhpDocTag attribute list
* Move Doctrine repositoryClass class check to inspection and provide and help message to Symfony documentation
* Drop PluginUtil.isEnabled checks
* Drop old api workarounds for annotation @Targets and support some more edge cases in property extraction
* Provide Annotation class usage linemarker [#79](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/79)
* API: Provide an index with annotated elements stubs [#53](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/53)
* Add Doctrine @Embedded class generator [#88](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/88) and refactoring of orm generator
* Drop old workarounds for old PhpStorm in getDocBlockTag completion pattern
* Drop workaround for class interface fqn class name of old PhpStorm
* Update string value resolving to support class constants
* Migration annotation import annotator to inspection; prevent memory leaks; use visitor pattern and provider better inspection overlay for multiple and single class
* Prevent possible memory leaks issue in Doctrine repositoryClass quickfix

## 5.1
* Allow multiple PhpTypes declaration for bool type detection
* Add PhpStorm 2017.2 travis environment
* Fix npe in Doctrine repository annotator [#83](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/83)
* Replace deprecation usage of annotation indexer externalizer
* Imported interfaces @Query(FooInterface::class) are marked as unused when used for constant [#82](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/82)

## 5.0
* PhpStorm 2017.1 build
* Add FOSRest alias [#87](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/87) @Koc

## 4.3.2
* Replace deprecated api usages

## 4.3.1
* Add virtual annotation classes properties / fields [#80](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/80)

## 4.3
* Index issue on YAML File; drop usage of DefaultFileTypeSpecificInputFilter [#72](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/72)
* Add autosuggest and ctrl+click for Doctrine CustomIdGenerator [#48](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/48)
* Add extension point to register global namespace prefixes [#81](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/81)

## 4.2
* Add alias for VichUploadableBundle [#69](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/69) @Koc
* Add references for Doctrine @ORM\Embedded.class [#68](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/68)
* Add autocomplete for array values of properties as extension point [#62](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/62)
* Add Symfony internal route array completion provider [#62](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/62)
* Add PHP-Toolbox provider for new array annotations as "annotation_array" [#62](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/62)

## 4.1.2
* PhpStorm 2016.3: Switch from PhpResolveResult#create to PsiElementResolveResult#createResults [#66](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/66)

## 4.1.1
* Fix class cast issue for reference contributor [#64](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/64)

## 4.1
* Add class constant support for import optimization and provide references. [#22](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/22), [#26](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/26), [#38](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/38), [#40](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/40)
* Implement workaround for class constant usage in doc array [WI-32801](https://youtrack.jetbrains.com/issue/WI-32801)

## 4.0.1
* Fails to recognize import useful for PHPDoc Annotation(s), PHPStorm 2016.2 [#59](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/59), [#63](https://github.com/Haehnchen/idea-php-annotation-plugin/issues/63) @artspb

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