<?php

namespace Doctrine\ORM\Mapping
{
    /**
     * @Annotation
     * @Target({"PROPERTY","ANNOTATION"})
     */
    #[\Attribute()]
    final class Column
    {
        public function __construct(
            ?string $name = null,
            ?string $type = null,
            ?int $length = null,
            ?int $precision = null,
            ?int $scale = null,
            bool $unique = false,
            bool $nullable = false,
            bool $insertable = true,
            bool $updatable = true,
            ?string $enumType = null,
            array $options = [],
            ?string $columnDefinition = null,
            ?string $generated = null
        ) {}
    }
}
