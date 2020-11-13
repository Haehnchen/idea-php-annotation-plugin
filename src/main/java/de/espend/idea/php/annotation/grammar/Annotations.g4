grammar Annotations;

start : base EOF;
base : ANNOTATION_TITLE (annotation_content |);
annotation_content : OPENING_BRACKET (content_list|) CLOSING_BRACKET;

content_list : content (',' content_list | ',' | );

content : ((ANNOTATION_NAME | STRING) ('=' | ':' )|) (value | array | base) | base;

value : ANNOTATION_NAME | STRING | INTEGER ;
array : OPENING_CURLY_BRACKET (content_list|) CLOSING_CURLY_BRACKET;

ANNOTATION_TITLE : AT_CHARACTER ANNOTATION_NAME;
AT_CHARACTER : '@';
OPENING_BRACKET : '(';
CLOSING_BRACKET : ')';
OPENING_CURLY_BRACKET : '{';
CLOSING_CURLY_BRACKET : '}';
STRING : '"' ~('"')* '"' ;
INTEGER : [0-9]+ ('.' [0-9]*|);
ANNOTATION_NAME : ~[()\n=,"{} ]+;
WS  : [ \t\r\n*]+ -> skip ;
