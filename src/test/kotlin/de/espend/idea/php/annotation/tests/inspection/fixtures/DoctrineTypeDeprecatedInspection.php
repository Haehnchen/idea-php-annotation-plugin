<?php

namespace Doctrine\ORM\Mapping
{
    /**
     * @Annotation
     * @Target({"PROPERTY","ANNOTATION"})
     */
    final class Column
    {
    }
}

namespace Doctrine\DBAL\Types
{
    abstract class Type
    {
        abstract public function getName();
    }
}

namespace App
{
    use Doctrine\DBAL\Types\Type;

    /**
     * @deprecated Use JsonType instead
     */
    class JsonArrayType extends Type
    {
        public function getName()
        {
            return 'json_array';
        }
    }

    class JsonType extends Type
    {
        public function getName()
        {
            return 'json';
        }
    }
}


