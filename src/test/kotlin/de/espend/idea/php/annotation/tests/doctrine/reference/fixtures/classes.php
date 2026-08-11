<?php

namespace Doctrine\ORM\Mapping
{
    /**
     * @Annotation
     */
    final class Entity implements Annotation
    {
    }

    /**
     * @Annotation
     */
    final class Column implements Annotation
    {
    }

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

namespace App\Repository
{
    class UserRepository
    {
    }
}

namespace Doctrine\DBAL\Types
{
    abstract class Type
    {
    }
}

namespace App\Doctrine
{
    class MyType extends \Doctrine\DBAL\Types\Type
    {
        public function getName()
        {
            return 'my_type';
        }
    }
}
