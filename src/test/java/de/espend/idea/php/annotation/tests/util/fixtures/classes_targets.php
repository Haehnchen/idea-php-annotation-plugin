<?php

namespace My\Annotations
{
    /**
     * @Annotation
     * @Target("PROPERTY")
     */
    class PropertyOnly
    {
    }

    /**
     * @Annotation
     * @Target("METHOD", "ALL")
     */
    class MethodAndAll
    {
    }
}
