lexer grammar BeamNewLexer;

/*
 * Lexer Rules
 */

fragment DIGIT  : [0-9] ;
fragment LETTER : [A-Za-z] ;
fragment UNDER_SCORE : [_] ;
fragment STRING : ~('\''|'"')* ;
fragment STAR : '*' ;
fragment TILDE : '~' ;
fragment COMMON : LETTER | DIGIT | DASH | UNDER_SCORE | DOT | SLASH | STAR | RBLOCK | LBLOCK | TILDE;
fragment NO_START : LBRACET ;
fragment NO_END : RBRACET ;

END
    : 'end'
    ;

DONE
    : 'done'
    ;

QUOTED_STRING
    : '"' STRING '"' | '\'' STRING '\''
    ;

COLON
    : ':'
    ;

HASH          : '#' ;
LPAREN        : '(' ;
RPAREN        : ')' ;
LBLOCK        : '{' ;
RBLOCK        : '}' ;
LBRACET       : '[' ;
RBRACET       : ']' ;
DOLLAR        : '$' ;
AT            : '@' ;
COMMA         : ',' ;
PIPE          : '|' ;
DOT           : '.' ;
SLASH         : '/' ;
DASH          : '-' ;

TOKEN
    : (NO_END+ (COMMON | NO_START)+)+ | COMMON+ (NO_START (NO_START | COMMON)*)* (NO_END+ (COMMON | NO_START)+)*
    ;

// Whitespace
WS            : [ \t]+ -> skip ;
NEWLINE       : [\r\n] ;
WHITESPACE    : [ \u000C\t]+ -> channel(HIDDEN) ;
COMMENT       : '//' ~[\r\n]* '\r'? '\n' -> channel(HIDDEN) ;
