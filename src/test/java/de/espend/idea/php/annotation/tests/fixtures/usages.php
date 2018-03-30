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
}

namespace My\Model
{
    use \Doctrine\ORM\Mapping as ORM;

    /**
     * @ORM\Embedded
     */
    class Item
    {
        /**
         * @ORM\CustomIdGenerator
         */
        private $foo;
    }
}