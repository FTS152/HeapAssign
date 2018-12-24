// Define a grammar called Hello
grammar HeapAssign;

prog  			: statement*;

statement		: assignment | if_branch ;

assignment		: expression '=' expression ';';
if_branch		:'if(' guard '){' statement* '}else{' statement* '}';

expression		: CONSTANT 
				| VARIABLE 
				| VARIABLE obj_property 
				| concat
				;

obj_property 	: ('[' expression ']' obj_property)*;
concat			: '(' expression '+' expression ')';

guard			: booling 
				| equation 
				| membership 
				| nondeterministic 
				| '(' guard '|' guard ')' 
				| '(' guard '&' guard ')' 
				| '!' guard
				;

booling			: 'TRUE' | 'FALSE' ;
equation		: expression '==' expression ;
membership		: 'hasProp(' expression ',' expression ')' ;
nondeterministic: '*' ;


CONSTANT        : '"' ([A-Za-z0-9])+ '"';
VARIABLE		: ([A-Za-z0-9])+;
WS            	: [\t\r\n\f ]+ -> skip;