<?php

namespace Doctrine\ORM\Mapping
{
    interface Annotation
    {
    }

    /**
     * @Annotation
     * @Target({"PROPERTY","ANNOTATION"})
     */
    final class Column
    {
    }

    class Entity
    {
    }
}

namespace App\Entity
{
    class CarRepository {}
}
