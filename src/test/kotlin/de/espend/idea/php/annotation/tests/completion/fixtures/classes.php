<?php

namespace My\Annotations
{

    /**
     * @Annotation
     * @Target("ALL")
     *
     * @Attributes({
     *   @Attribute("stringProperty", type = "string"),
     *   @Attribute("annotProperty",  type = "SomeAnnotationClass"),
     *   @Attribute("attribute_no_type"),
     *   @Attribute("attribute_blank_type",  type = ""),
     * })
     *
     * @Attributes(
     *     @Attribute("accessControl", type="string"),
     *     @Attribute("has_access", type="bool"),
     * )
     */
     #[\Attribute(\Attribute::TARGET_ALL)]
    class All
    {
        /**
         * @var array<string>
         */
        public $cascade;

        /**
         * @var Boolean
         */
        public $option = false;

        /**
         * @var boolean
         */
        public $boolValue;

        /**
         *
         * @Enum({"AUTO", "SEQUENCE", "TABLE", "IDENTITY", "NONE", "UUID", "CUSTOM"})
         */
        public $strategy = 'AUTO';

        /**
         * @var string
         */
        private $myPrivate;
    }

    /**
     * @Annotation
     * @Target("CLASS")
     * @deprecated
     */
    #[\Attribute(\Attribute::TARGET_CLASS)]
    class AClazzDeprecated
    {
    }

    /**
     * @Annotation
     * @Target("CLASS")
     */
    #[\Attribute(\Attribute::TARGET_CLASS)]
    class Clazz
    {
    }

    /**
     * @Annotation
     * @Target("CLASS")
     * @deprecated
     */
    #[\Attribute(\Attribute::TARGET_CLASS)]
    class ClazzDeprecated
    {
    }

    /**
     * @Annotation
     * @Target("PROPERTY")
     */
    #[\Attribute(\Attribute::TARGET_PROPERTY)]
    class Property
    {
    }

    /**
     * @Annotation
     * @Target("METHOD")
     */
    #[\Attribute(\Attribute::TARGET_METHOD)]
    class Method
    {
    }

    class Constants
    {
        const FOO = null;
    }
}

namespace Doctrine\ORM\Mapping
{
    /**
     * @Annotation
     */
    #[\Attribute]
    class Entity {}
}
