<?php

declare(strict_types=1);

namespace Doctrine\DBAL\Types
{
    abstract class Type {}
}

namespace App {
    class MyType extends \Doctrine\DBAL\Types\Type
    {
        public function getName()
        {
            return 'my_type';
        }
    }
}



