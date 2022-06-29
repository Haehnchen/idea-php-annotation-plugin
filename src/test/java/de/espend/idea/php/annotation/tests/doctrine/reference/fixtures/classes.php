<?php

namespace Doctrine\ORM\Mapping
{
    /**
     * @Annotation
     * @Target("PROPERTY")
     */
    final class Embedded implements Annotation
    {
    }

    /**
     * @Annotation
     * @Target("PROPERTY")
     */
    final class CustomIdGenerator implements Annotation
    {
    }

    /**
     * @Annotation
     * @Target("PROPERTY")
     */
    final class ManyToMany implements Annotation
    {
    }
}

namespace My\FooClass
{
    class Bar
    {
        private $bar;
    }

    class Bar2
    {
        private $bar2;
    }
}