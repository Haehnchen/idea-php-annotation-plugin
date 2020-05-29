<?php

namespace Doctrine\ORM\Mapping
{
    /**
     * @Annotation()
     */
    class Entity
    {
    }

    class Foobar
    {
    }
}

namespace Foobar\Bar
{
    class FooBar {}

    /**
     * @deprecated
     */
    class FooBarDeprecated
    {
        /**
         * @deprecated
         */
        public const I_AM_DEPRECATED = 'oh no';

        public const I_AM_NOT_DEPRECATED = 'nice';
    }
}