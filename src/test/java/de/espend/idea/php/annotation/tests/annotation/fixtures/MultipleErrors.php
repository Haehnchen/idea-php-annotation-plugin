<?php

use Swagger\Annotations as SWG;

 /**
  * A unrelated comment
  *
  * @SWG\Response(
  *     collectionOperations={},
  *     itemOperations={
  *          "getOperationName"={
  *              "method" = "get"
  *              "path"="/some/route/with/{id}/xxx",
  *              "name"="GetSomeModel,
  *              "normalization_context"= {"groups"={"normalization-context-group"}},
  *              "swagger_context" = {
  *                  "parameters" =
  *                      {
  *                          "name" = "id",
  *                          "in" = "path",
  *                          "required" = "true",
  *                          "type" : "string"
  *                      }
  *                  },
  *              }
  *         }
  *     }
  * )
  */
class Foo {}
