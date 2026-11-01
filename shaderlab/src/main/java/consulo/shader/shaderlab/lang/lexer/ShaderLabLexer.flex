package consulo.shader.shaderlab.lang.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import consulo.shader.shaderlab.lang.psi.ShaderLabTokens;

/*
 * Parts of this lexer (case-insensitive keywords, the BRACKETS/PARENS states for property attributes with unquoted
 * text, #warning/#error/#line lines, 2D/3D/2DArray texture dimensions, program block termination) are
 * ported from JetBrains resharper-unity (src/Unity.Shaders/ShaderLab/Psi/Parsing/ShaderLab.lex), Apache License 2.0.
 */
@SuppressWarnings({"ALL"})
@org.jspecify.annotations.NullUnmarked
%%

%public
%class _ShaderLabLexer
%extends LexerBase
%unicode
%ignorecase
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state SHADERSCRIPT
%state HLSLSCRIPT
%state GLSLSCRIPT
%state BRACKETS
%state PARENS

WHITE_SPACE=[ \n\r\t\f]+
INLINE_WHITE_SPACE=[ \t\f]+
NEW_LINE_WHITE_SPACE=[ \t\f]* [\r\n] [ \n\r\t\f]*

SINGLE_LINE_COMMENT="/""/"[^\r\n]*
COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
MULTI_LINE_STYLE_COMMENT=("/*"{COMMENT_TAIL})|"/*"

PP_LINE=("#warning" | "#error" | "#line") [^\r\n]*

STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]

DIGIT=[0-9]
DIGITS={DIGIT}+

INTEGER_PREFIX="+" | "-"
INTEGER_LITERAL={INTEGER_PREFIX}? ({DIGITS} | {DIGIT}* "." {DIGITS} | {DIGITS} ".")

IDENTIFIER=[:jletter:] [:jletterdigit:]* | {INTEGER_LITERAL} [:jletter:]

TEXTURE_DIMENSION="2D" | "3D" | "2DArray"

/* unquoted text inside property attribute parentheses, e.g. [Header(Some text)] or [Enum(UnityEngine.Rendering.CullMode)] */
UNQUOTED_START=("#" | "_" | "(" | [:letter:])
UNQUOTED_PART=({UNQUOTED_START} | {DIGIT} | [ \t\f] | "." | ")")
UNQUOTED_END=({UNQUOTED_START} | {DIGIT} | ".")
UNQUOTED_REST=({UNQUOTED_PART}* {UNQUOTED_END})
UNQUOTED_STRING_LITERAL=({UNQUOTED_START} {UNQUOTED_REST}*) | ({DIGIT} {UNQUOTED_REST}+)

%%

<YYINITIAL>
{
	{SINGLE_LINE_COMMENT}      { return ShaderLabTokens.LINE_COMMENT; }

	{PP_LINE}                  { return ShaderLabTokens.PREPROCESSOR_LINE; }

	{MULTI_LINE_STYLE_COMMENT} { return ShaderLabTokens.BLOCK_COMMENT; }

	"CGINCLUDE"                { yybegin(SHADERSCRIPT); return ShaderLabTokens.CGINCLUDE_KEYWORD; }

	"CGPROGRAM"                { yybegin(SHADERSCRIPT); return ShaderLabTokens.CGPROGRAM_KEYWORD; }

	"ENDCG"                    { return ShaderLabTokens.ENDCG_KEYWORD; }

	"HLSLINCLUDE"              { yybegin(HLSLSCRIPT); return ShaderLabTokens.HLSLINCLUDE_KEYWORD; }

	"HLSLPROGRAM"              { yybegin(HLSLSCRIPT); return ShaderLabTokens.HLSLPROGRAM_KEYWORD; }

	"ENDHLSL"                  { return ShaderLabTokens.ENDHLSL_KEYWORD; }

	"GLSLINCLUDE"              { yybegin(GLSLSCRIPT); return ShaderLabTokens.GLSLINCLUDE_KEYWORD; }

	"GLSLPROGRAM"              { yybegin(GLSLSCRIPT); return ShaderLabTokens.GLSLPROGRAM_KEYWORD; }

	"ENDGLSL"                  { return ShaderLabTokens.ENDGLSL_KEYWORD; }

	"{"                        { return ShaderLabTokens.LBRACE; }

	"}"                        { return ShaderLabTokens.RBRACE; }

	"("                        { return ShaderLabTokens.LPAR; }

	")"                        { return ShaderLabTokens.RPAR; }

	"["                        { yybegin(BRACKETS); return ShaderLabTokens.LBRACKET; }

	"]"                        { return ShaderLabTokens.RBRACKET; }

	","                        { return ShaderLabTokens.COMMA; }

	"="                        { return ShaderLabTokens.EQ; }

	{TEXTURE_DIMENSION}        { return ShaderLabTokens.IDENTIFIER; }

	{IDENTIFIER}               { return ShaderLabTokens.IDENTIFIER; }

	{INTEGER_LITERAL}          { return ShaderLabTokens.INTEGER_LITERAL; }

	{STRING_LITERAL}           { return ShaderLabTokens.STRING_LITERAL; }

	{WHITE_SPACE}              { return ShaderLabTokens.WHITE_SPACE; }

	[^]                        { return ShaderLabTokens.BAD_CHARACTER; }
}

/* inside '[' ... ']': property attributes and property references like [_MainTex]. A new line always leaves the state */
<BRACKETS>
{
	{INLINE_WHITE_SPACE}       { return ShaderLabTokens.WHITE_SPACE; }

	{NEW_LINE_WHITE_SPACE}     { yybegin(YYINITIAL); return ShaderLabTokens.WHITE_SPACE; }

	"("                        { yybegin(PARENS); return ShaderLabTokens.LPAR; }

	")"                        { return ShaderLabTokens.RPAR; }

	"]"                        { yybegin(YYINITIAL); return ShaderLabTokens.RBRACKET; }

	{TEXTURE_DIMENSION}        { return ShaderLabTokens.IDENTIFIER; }

	{IDENTIFIER}               { return ShaderLabTokens.IDENTIFIER; }

	{INTEGER_LITERAL}          { return ShaderLabTokens.INTEGER_LITERAL; }

	{STRING_LITERAL}           { return ShaderLabTokens.STRING_LITERAL; }

	[^]                        { yypushback(1); yybegin(YYINITIAL); }
}

/* inside '[Attribute(' ... ')': arguments may be unquoted text */
<PARENS>
{
	{INLINE_WHITE_SPACE}       { return ShaderLabTokens.WHITE_SPACE; }

	{NEW_LINE_WHITE_SPACE}     { yybegin(YYINITIAL); return ShaderLabTokens.WHITE_SPACE; }

	","                        { return ShaderLabTokens.COMMA; }

	")"                        { yybegin(BRACKETS); return ShaderLabTokens.RPAR; }

	"]"                        { yybegin(YYINITIAL); return ShaderLabTokens.RBRACKET; }

	{INTEGER_LITERAL}          { return ShaderLabTokens.INTEGER_LITERAL; }

	{STRING_LITERAL}           { return ShaderLabTokens.STRING_LITERAL; }

	{UNQUOTED_STRING_LITERAL}  { return ShaderLabTokens.UNQUOTED_STRING_LITERAL; }

	[^]                        { yypushback(1); yybegin(YYINITIAL); }
}

/* program bodies run until the matching END keyword, or up to the end of file for an unterminated block */
<SHADERSCRIPT>
{
	"ENDCG"                    { yybegin(YYINITIAL); return ShaderLabTokens.ENDCG_KEYWORD; }

	[^Ee]+                     { return ShaderLabTokens.SHADERSCRIPT; }

	[^]                        { return ShaderLabTokens.SHADERSCRIPT; }
}

<HLSLSCRIPT>
{
	"ENDHLSL"                  { yybegin(YYINITIAL); return ShaderLabTokens.ENDHLSL_KEYWORD; }

	[^Ee]+                     { return ShaderLabTokens.SHADERSCRIPT; }

	[^]                        { return ShaderLabTokens.SHADERSCRIPT; }
}

<GLSLSCRIPT>
{
	"ENDGLSL"                  { yybegin(YYINITIAL); return ShaderLabTokens.ENDGLSL_KEYWORD; }

	[^Ee]+                     { return ShaderLabTokens.SHADERSCRIPT; }

	[^]                        { return ShaderLabTokens.SHADERSCRIPT; }
}
