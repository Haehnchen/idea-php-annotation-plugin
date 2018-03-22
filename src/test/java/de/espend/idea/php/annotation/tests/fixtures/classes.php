<?php

namespace My\Annotations
{

    /**
     * @Annotation
     */
    class Route
    {
    }
}

namespace My\Annotations\Foo
{

    /**
     * @Annotation()
     */
    class RouteFoo
    {
    }
    /**
     * @Annotation(FOOO!!!)
     */
    class RouteBar
    {
    }
}