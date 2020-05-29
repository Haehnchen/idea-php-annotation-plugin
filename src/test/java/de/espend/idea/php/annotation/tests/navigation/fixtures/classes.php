<?php

namespace Foo
{
    /**
     * @Annotation
     *
     * @Attributes({
     *   @Attribute("stringProperty", type = "string"),
     *   @Attribute("annotProperty",  type = "SomeAnnotationClass"),
     * })
     *
     * @Attributes(
     *     @Attribute("accessControl", type="string"),
     * )
     */
    class Bar
    {
        public $foo;
    }
}

namespace My
{
    class Bar
    {
        const MY_VAR = 'BAR';
    }
}