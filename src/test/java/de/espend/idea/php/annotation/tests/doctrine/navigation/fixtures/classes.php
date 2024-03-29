<?php


namespace Doctrine\ORM\Mapping
{
    use Attribute;

    /**
     * @Annotation
     * @NamedArgumentConstructor()
     * @Target({"PROPERTY","ANNOTATION"})
     */
    #[Attribute(Attribute::TARGET_PROPERTY | Attribute::IS_REPEATABLE)]
    final class JoinColumn implements Annotation
    {
        /** @var string|null */
        public $name;

        /** @var string */
        public $referencedColumnName = 'id';

        /** @var bool */
        public $unique = false;

        /** @var bool */
        public $nullable = true;

        /** @var mixed */
        public $onDelete;

        /** @var string|null */
        public $columnDefinition;

        /**
         * Field name used in non-object hydration (array/scalar).
         *
         * @var string|null
         */
        public $fieldName;

        public function __construct(
            ?string $name = null,
            string $referencedColumnName = 'id',
            bool $unique = false,
            bool $nullable = true,
                    $onDelete = null,
            ?string $columnDefinition = null,
            ?string $fieldName = null
        ) {
            $this->name                 = $name;
            $this->referencedColumnName = $referencedColumnName;
            $this->unique               = $unique;
            $this->nullable             = $nullable;
            $this->onDelete             = $onDelete;
            $this->columnDefinition     = $columnDefinition;
            $this->fieldName            = $fieldName;
        }
    }

}
