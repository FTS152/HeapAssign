// Define a grammar called Hello
grammar HeapAssign;

prog  			: (statement ';')*;

statement		: expression '=' expression | 'if (' guard ') {' statement '}' 'else {' statement '}';

expression		: constant | variable | variable + obj_property | exp '+' exp;

obj_property 	: '[' exp ']' + obj_property;

exp 			: constant | variable | variable + obj_property;

guard			: '(' grd '|' guard ')' | '(' grd '&' guard ')' | '!' guard;

grd 			: 'TRUE' | 'FALSE' | expression '==' expression | 'hasProp(' expression ',' expression ')' | '*';

constant		: '\\"' [a-zA-Z_]+ '\\"';

variable		: [a-zA-Z0-9]+;

WS            	: [\t\r\n\f ]+ -> skip;