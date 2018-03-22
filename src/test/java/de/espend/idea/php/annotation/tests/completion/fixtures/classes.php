<?php

namespace My\Annotations
{

    /**
     * @Annotation
     * @Target("ALL")
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
