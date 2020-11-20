<?php

use Swagger\Annotations as SWG;

/**
* @SWG\Response(
*     response=200
*     description="Some Description",
*     @SWG\Schema(
*        type="json"
*     )
* )
* @SWG\Response("/pretty/deep/route", name="someGetRoute", methods={"GET"})
*/
class Foo {}