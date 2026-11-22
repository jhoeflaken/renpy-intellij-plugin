package io.mayasoft.renpy.language.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import io.mayasoft.renpy.language.psi.RenpyTokenTypes;

%%

%class _RenpyLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE = [\ \t\f]+
ONE_NL = \r|\n|\r\n
LINE_CONTINUATION = \\{ONE_NL}

IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*
NUMBER = (0[xX][0-9a-fA-F]+)|([0-9]+(\.[0-9]+)?([eE][+-]?[0-9]+)?)
COMMENT = "#"[^\r\n]*

// Strings
SINGLE_QUOTED_STRING = \'([^\\\'\r\n]|\\.)*\'?
DOUBLE_QUOTED_STRING = \"([^\\\"\r\n]|\\.)*\"?
TRIPLE_SINGLE_QUOTED_STRING = "'''" ( [^'\\] | \\. | \'[^'\\] | \'\'[^'\\] )* ("'''")?
TRIPLE_DOUBLE_QUOTED_STRING = "\"\"\"" ( [^\"\\] | \\. | \"[^\"\\] | \"\"[^\"\\] )* ("\"\"\"")?

%state IN_ONE_LINE_PYTHON

%%

<YYINITIAL> {
  {WHITE_SPACE}                 { return TokenType.WHITE_SPACE; }
  {LINE_CONTINUATION}           { return RenpyTokenTypes.LINE_CONTINUATION; }
  {ONE_NL}                      { return RenpyTokenTypes.NEWLINE; }
  {COMMENT}                     { return RenpyTokenTypes.COMMENT; }

  "$"                           { yybegin(IN_ONE_LINE_PYTHON); return RenpyTokenTypes.ONE_LINE_PYTHON; }

  // Keywords
  "label"                       { return RenpyTokenTypes.KW_LABEL; }
  "jump"                        { return RenpyTokenTypes.KW_JUMP; }
  "call"                        { return RenpyTokenTypes.KW_CALL; }
  "return"                      { return RenpyTokenTypes.KW_RETURN; }
  "scene"                       { return RenpyTokenTypes.KW_SCENE; }
  "show"                        { return RenpyTokenTypes.KW_SHOW; }
  "hide"                        { return RenpyTokenTypes.KW_HIDE; }
  "with"                        { return RenpyTokenTypes.KW_WITH; }
  "behind"                      { return RenpyTokenTypes.KW_BEHIND; }
  "as"                          { return RenpyTokenTypes.KW_AS; }
  "at"                          { return RenpyTokenTypes.KW_AT; }
  "onlayer"                     { return RenpyTokenTypes.KW_ONLAYER; }
  "zorder"                      { return RenpyTokenTypes.KW_ZORDER; }
  "menu"                        { return RenpyTokenTypes.KW_MENU; }
  "python"                      { return RenpyTokenTypes.KW_PYTHON; }
  "init"                        { return RenpyTokenTypes.KW_INIT; }
  "define"                      { return RenpyTokenTypes.KW_DEFINE; }
  "default"                     { return RenpyTokenTypes.KW_DEFAULT; }
  "transform"                   { return RenpyTokenTypes.KW_TRANSFORM; }
  "screen"                      { return RenpyTokenTypes.KW_SCREEN; }
  "style"                       { return RenpyTokenTypes.KW_STYLE; }
  "image"                       { return RenpyTokenTypes.KW_IMAGE; }
  "play"                        { return RenpyTokenTypes.KW_PLAY; }
  "stop"                        { return RenpyTokenTypes.KW_STOP; }
  "queue"                       { return RenpyTokenTypes.KW_QUEUE; }
  "voice"                       { return RenpyTokenTypes.KW_VOICE; }
  "sound"                       { return RenpyTokenTypes.KW_SOUND; }
  "music"                       { return RenpyTokenTypes.KW_MUSIC; }
  "pause"                       { return RenpyTokenTypes.KW_PAUSE; }
  "window"                      { return RenpyTokenTypes.KW_WINDOW; }
  "nvl"                         { return RenpyTokenTypes.KW_NVL; }

  // Control flow keywords
  "if"                          { return RenpyTokenTypes.KW_IF; }
  "elif"                        { return RenpyTokenTypes.KW_ELIF; }
  "else"                        { return RenpyTokenTypes.KW_ELSE; }
  "while"                       { return RenpyTokenTypes.KW_WHILE; }
  "for"                         { return RenpyTokenTypes.KW_FOR; }
  "pass"                        { return RenpyTokenTypes.KW_PASS; }
  "in"                          { return RenpyTokenTypes.KW_IN; }
  "not"                         { return RenpyTokenTypes.KW_NOT; }
  "and"                         { return RenpyTokenTypes.KW_AND; }
  "or"                          { return RenpyTokenTypes.KW_OR; }
  "is"                          { return RenpyTokenTypes.KW_IS; }

  // Boolean and None
  "True"                        { return RenpyTokenTypes.KW_TRUE; }
  "False"                       { return RenpyTokenTypes.KW_FALSE; }
  "None"                        { return RenpyTokenTypes.KW_NONE; }

  // String literals
  {TRIPLE_DOUBLE_QUOTED_STRING} { return RenpyTokenTypes.STRING_LITERAL; }
  {TRIPLE_SINGLE_QUOTED_STRING} { return RenpyTokenTypes.STRING_LITERAL; }
  {DOUBLE_QUOTED_STRING}        { return RenpyTokenTypes.STRING_LITERAL; }
  {SINGLE_QUOTED_STRING}        { return RenpyTokenTypes.STRING_LITERAL; }

  // Number & Identifier
  {NUMBER}                      { return RenpyTokenTypes.NUMBER; }
  {IDENTIFIER}                  { return RenpyTokenTypes.IDENTIFIER; }

  // Operators and Punctuation
  ":"                           { return RenpyTokenTypes.COLON; }
  ","                           { return RenpyTokenTypes.COMMA; }
  "."                           { return RenpyTokenTypes.DOT; }
  "=="                          { return RenpyTokenTypes.EQEQ; }
  "!="                          { return RenpyTokenTypes.NEQ; }
  "<="                          { return RenpyTokenTypes.LEQ; }
  ">="                          { return RenpyTokenTypes.GEQ; }
  "="                           { return RenpyTokenTypes.EQ; }
  "<"                           { return RenpyTokenTypes.LT; }
  ">"                           { return RenpyTokenTypes.GT; }
  "+"                           { return RenpyTokenTypes.PLUS; }
  "-"                           { return RenpyTokenTypes.MINUS; }
  "*"                           { return RenpyTokenTypes.STAR; }
  "/"                           { return RenpyTokenTypes.SLASH; }
  "%"                           { return RenpyTokenTypes.PERCENT; }
  "("                           { return RenpyTokenTypes.LPAREN; }
  ")"                           { return RenpyTokenTypes.RPAREN; }
  "["                           { return RenpyTokenTypes.LBRACKET; }
  "]"                           { return RenpyTokenTypes.RBRACKET; }
  "{"                           { return RenpyTokenTypes.LBRACE; }
  "}"                           { return RenpyTokenTypes.RBRACE; }
  "@"                           { return RenpyTokenTypes.AT; }

  [^]                           { return TokenType.BAD_CHARACTER; }
}

<IN_ONE_LINE_PYTHON> {
  {ONE_NL}                      { yybegin(YYINITIAL); return RenpyTokenTypes.NEWLINE; }
  [^\r\n]+                      { return RenpyTokenTypes.PYTHON_CODE; }
}
