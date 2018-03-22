<?php

namespace My\Annotations
{

    /**
     * @Annotation
     * @Target("PROPERTY")
     */
    class Property
    {
    }

    /**
     * @Annotation
     * @Target("ALL")
     */
    class All
    {
    }

    /**
     * @Annotation
     * @Target("PROPERTY", "METHOD")
     */
    class PropertyMethod
    {
    }

    /**
     * @Annotation
     * @Target(["PROPERTY", "METHOD"])
     */
    class PropertyMethodArray
    {
    }

    /**
     * @Annotation
     */
    class Undefined
    {
    }

    /**
     * @Target("ALL")
     */
    class Unknown
    {
    }
}
