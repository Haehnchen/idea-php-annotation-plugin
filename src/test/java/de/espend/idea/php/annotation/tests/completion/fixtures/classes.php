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
     * })
     *
     * @Attributes(
     *     @Attribute("accessControl", type="string"),
     * )
     */
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
         * @var mixed|array|boolean
         */
        public $mixed;

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
    class AClazzDeprecated
    {
    }

    /**
     * @Annotation
     * @Target("CLASS")
     */
    class Clazz
    {
    }

    /**
     * @Annotation
     * @Target("CLASS")
     * @deprecated
     */
    class ClazzDeprecated
    {
    }

    /**
     * @Annotation
     * @Target("PROPERTY")
     */
    class Property
    {
    }

    /**
     * @Annotation
     * @Target("METHOD")
     */
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
    class Entity {}
}
