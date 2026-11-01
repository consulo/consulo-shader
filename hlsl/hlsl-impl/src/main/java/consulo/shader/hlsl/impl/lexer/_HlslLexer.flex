package consulo.shader.hlsl.impl.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import consulo.shader.hlsl.impl.psi.HlslTypes;

@SuppressWarnings({"ALL"})
@org.jspecify.annotations.NullUnmarked
%%

%public
%unicode
%class _HlslLexer
%extends LexerBase
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE_CHAR=[\ \t\f\r\n]
NEW_LINE=\r\n|\r|\n

IDENTIFIER=[a-zA-Z_] [a-zA-Z_0-9]*

BLOCK_COMMENT="/*" ([^*] | "*"+ [^*/])* ("*"+ "/")?
LINE_COMMENT="/""/"[^\r\n]*

/* a whole directive line, continued by a backslash at the end of a line or by a block comment spanning lines */
PREPROCESSOR_LINE="#" ([^\r\n\\/] | \\ {NEW_LINE} | \\ [^\r\n] | "/" | {BLOCK_COMMENT})* \\?

DIGIT=[0-9]
DIGITS={DIGIT}+
HEX_DIGIT=[0-9a-fA-F]

/* u: unsigned, l/ul/ull: 64-bit */
INTEGER_SUFFIX=[uU]? [lL]? [lL]? | [lL] [uU]
INTEGER_LITERAL=({DIGITS} | 0 [xX] {HEX_DIGIT}+ | 0 [bB] [01]+) {INTEGER_SUFFIX}

EXPONENT=[eE] [+-]? {DIGITS}
/* f: float, h: half, l/lf: double */
FLOAT_SUFFIX=[fFhHlL] | "lf" | "LF"
FLOAT_LITERAL=({DIGITS} "." {DIGIT}* {EXPONENT}? | "." {DIGITS} {EXPONENT}? | {DIGITS} {EXPONENT}) {FLOAT_SUFFIX}? | {DIGITS} [fFhH]
/* the special values FXC prints and accepts: 1.#INF, 1.#IND, 1.#QNAN */
SPECIAL_FLOAT_LITERAL={DIGITS} "." "#" ("INF" | "IND" | "QNAN" | "SNAN")

STRING_LITERAL=\"([^\\\"\r\n]|\\[^\r\n])*\"? | "'"([^\\\'\r\n]|\\[^\r\n])*"'"?

%%

<YYINITIAL> {
  {WHITE_SPACE_CHAR}+ { return HlslTokens.WHITE_SPACE; }

  {BLOCK_COMMENT} { return HlslTokens.BLOCK_COMMENT; }
  {LINE_COMMENT}  { return HlslTokens.LINE_COMMENT; }

  {PREPROCESSOR_LINE} { return HlslTokens.PREPROCESSOR_LINE; }

  {FLOAT_LITERAL}   { return HlslTypes.FLOAT_LITERAL; }
  {SPECIAL_FLOAT_LITERAL} { return HlslTypes.FLOAT_LITERAL; }
  {INTEGER_LITERAL} { return HlslTypes.INTEGER_LITERAL; }
  {STRING_LITERAL}  { return HlslTypes.STRING_LITERAL; }

  {IDENTIFIER} { return HlslKeywords.tokenOf(yytext()); }

  "<<=" { return HlslTypes.LEFT_ASSIGN; }
  ">>=" { return HlslTypes.RIGHT_ASSIGN; }
  "+="  { return HlslTypes.ADD_ASSIGN; }
  "-="  { return HlslTypes.SUB_ASSIGN; }
  "*="  { return HlslTypes.MUL_ASSIGN; }
  "/="  { return HlslTypes.DIV_ASSIGN; }
  "%="  { return HlslTypes.MOD_ASSIGN; }
  "&="  { return HlslTypes.AND_ASSIGN; }
  "^="  { return HlslTypes.XOR_ASSIGN; }
  "|="  { return HlslTypes.OR_ASSIGN; }
  "++"  { return HlslTypes.INC_OP; }
  "--"  { return HlslTypes.DEC_OP; }
  "&&"  { return HlslTypes.AND_OP; }
  "||"  { return HlslTypes.OR_OP; }
  "<<"  { return HlslTypes.LEFT_OP; }
  ">>"  { return HlslTypes.RIGHT_OP; }
  "<="  { return HlslTypes.LE_OP; }
  ">="  { return HlslTypes.GE_OP; }
  "=="  { return HlslTypes.EQ_OP; }
  "!="  { return HlslTypes.NE_OP; }
  "::"  { return HlslTypes.COLON_COLON; }

  "("   { return HlslTypes.LEFT_PAREN; }
  ")"   { return HlslTypes.RIGHT_PAREN; }
  "["   { return HlslTypes.LEFT_BRACKET; }
  "]"   { return HlslTypes.RIGHT_BRACKET; }
  "{"   { return HlslTypes.LEFT_BRACE; }
  "}"   { return HlslTypes.RIGHT_BRACE; }
  "."   { return HlslTypes.DOT; }
  ","   { return HlslTypes.COMMA; }
  ":"   { return HlslTypes.COLON; }
  ";"   { return HlslTypes.SEMICOLON; }
  "?"   { return HlslTypes.QUESTION; }
  "="   { return HlslTypes.EQUAL; }
  "!"   { return HlslTypes.BANG; }
  "-"   { return HlslTypes.DASH; }
  "~"   { return HlslTypes.TILDE; }
  "+"   { return HlslTypes.PLUS; }
  "*"   { return HlslTypes.STAR; }
  "/"   { return HlslTypes.SLASH; }
  "%"   { return HlslTypes.PERCENT; }
  "<"   { return HlslTypes.LEFT_ANGLE; }
  ">"   { return HlslTypes.RIGHT_ANGLE; }
  "|"   { return HlslTypes.VERTICAL_BAR; }
  "^"   { return HlslTypes.CARET; }
  "&"   { return HlslTypes.AMPERSAND; }

  [^] { return HlslTokens.BAD_CHARACTER; }
}
