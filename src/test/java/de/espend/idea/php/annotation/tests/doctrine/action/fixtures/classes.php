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

    /**
     * @Annotation
     */
    class Entity
    {
    }
}

namespace App\Entity
{
    class CarRepository {}
}
