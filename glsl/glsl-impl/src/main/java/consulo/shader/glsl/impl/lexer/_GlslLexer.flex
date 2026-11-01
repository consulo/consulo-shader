package consulo.shader.glsl.impl.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import consulo.shader.glsl.impl.psi.GlslTypes;

@SuppressWarnings({"ALL"})
@org.jspecify.annotations.NullUnmarked
%%

%{
  private boolean myGodot;

  public _GlslLexer(boolean godot) {
    myGodot = godot;
  }
%}

%public
%unicode
%class _GlslLexer
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

/* u: unsigned, l/ul: 64-bit (GL_ARB_gpu_shader_int64), s/us: 16-bit (GL_EXT_shader_explicit_arithmetic_types) */
INTEGER_SUFFIX=[uU] | [lL] | [uU][lL] | [sS] | [uU][sS]
INTEGER_LITERAL=([1-9] {DIGIT}* | 0 [0-7]* | 0 [xX] {HEX_DIGIT}+) {INTEGER_SUFFIX}?

EXPONENT=[eE] [+-]? {DIGITS}
/* f: float, lf: double, hf: half (GL_EXT_shader_explicit_arithmetic_types) */
FLOAT_SUFFIX=[fF] | "lf" | "LF" | "hf" | "HF"
FLOAT_LITERAL=({DIGITS} "." {DIGIT}* {EXPONENT}? | "." {DIGITS} {EXPONENT}? | {DIGITS} {EXPONENT}) {FLOAT_SUFFIX}? | {DIGITS} {FLOAT_SUFFIX}

/* Godot uniform hint arguments: hint_enum("A", "B") */
STRING_LITERAL=\"([^\\\"\r\n]|\\[^\r\n])*\"?

%%

<YYINITIAL> {
  {WHITE_SPACE_CHAR}+ { return GlslTokens.WHITE_SPACE; }

  {BLOCK_COMMENT} { return GlslTokens.BLOCK_COMMENT; }
  {LINE_COMMENT}  { return GlslTokens.LINE_COMMENT; }

  {PREPROCESSOR_LINE} { return GlslTokens.PREPROCESSOR_LINE; }

  {FLOAT_LITERAL}   { return GlslTypes.FLOAT_LITERAL; }
  {INTEGER_LITERAL} { return GlslTypes.INTEGER_LITERAL; }
  {STRING_LITERAL}  { return GlslTypes.STRING_LITERAL; }

  {IDENTIFIER} { return GlslKeywords.tokenOf(yytext(), myGodot); }

  "<<=" { return GlslTypes.LEFT_ASSIGN; }
  ">>=" { return GlslTypes.RIGHT_ASSIGN; }
  "+="  { return GlslTypes.ADD_ASSIGN; }
  "-="  { return GlslTypes.SUB_ASSIGN; }
  "*="  { return GlslTypes.MUL_ASSIGN; }
  "/="  { return GlslTypes.DIV_ASSIGN; }
  "%="  { return GlslTypes.MOD_ASSIGN; }
  "&="  { return GlslTypes.AND_ASSIGN; }
  "^="  { return GlslTypes.XOR_ASSIGN; }
  "|="  { return GlslTypes.OR_ASSIGN; }
  "++"  { return GlslTypes.INC_OP; }
  "--"  { return GlslTypes.DEC_OP; }
  "&&"  { return GlslTypes.AND_OP; }
  "||"  { return GlslTypes.OR_OP; }
  "^^"  { return GlslTypes.XOR_OP; }
  "<<"  { return GlslTypes.LEFT_OP; }
  ">>"  { return GlslTypes.RIGHT_OP; }
  "<="  { return GlslTypes.LE_OP; }
  ">="  { return GlslTypes.GE_OP; }
  "=="  { return GlslTypes.EQ_OP; }
  "!="  { return GlslTypes.NE_OP; }

  "("   { return GlslTypes.LEFT_PAREN; }
  ")"   { return GlslTypes.RIGHT_PAREN; }
  "["   { return GlslTypes.LEFT_BRACKET; }
  "]"   { return GlslTypes.RIGHT_BRACKET; }
  "{"   { return GlslTypes.LEFT_BRACE; }
  "}"   { return GlslTypes.RIGHT_BRACE; }
  "."   { return GlslTypes.DOT; }
  ","   { return GlslTypes.COMMA; }
  ":"   { return GlslTypes.COLON; }
  ";"   { return GlslTypes.SEMICOLON; }
  "?"   { return GlslTypes.QUESTION; }
  "="   { return GlslTypes.EQUAL; }
  "!"   { return GlslTypes.BANG; }
  "-"   { return GlslTypes.DASH; }
  "~"   { return GlslTypes.TILDE; }
  "+"   { return GlslTypes.PLUS; }
  "*"   { return GlslTypes.STAR; }
  "/"   { return GlslTypes.SLASH; }
  "%"   { return GlslTypes.PERCENT; }
  "<"   { return GlslTypes.LEFT_ANGLE; }
  ">"   { return GlslTypes.RIGHT_ANGLE; }
  "|"   { return GlslTypes.VERTICAL_BAR; }
  "^"   { return GlslTypes.CARET; }
  "&"   { return GlslTypes.AMPERSAND; }

  [^] { return GlslTokens.BAD_CHARACTER; }
}
