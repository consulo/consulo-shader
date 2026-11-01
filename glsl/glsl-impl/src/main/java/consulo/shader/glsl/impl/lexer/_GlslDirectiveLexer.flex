package consulo.shader.glsl.impl.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;

/* Lexes the text of one preprocessor line (GlslTokens.PREPROCESSOR_LINE). */
@SuppressWarnings({"ALL"})
@org.jspecify.annotations.NullUnmarked
%%

%public
%unicode
%class _GlslDirectiveLexer
%extends LexerBase
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state INCLUDE

WHITE_SPACE=([\ \t\f\r\n] | \\ (\r\n|\r|\n))+
IDENTIFIER=[a-zA-Z_] [a-zA-Z_0-9]*
BLOCK_COMMENT="/*" ([^*] | "*"+ [^*/])* ("*"+ "/")?
LINE_COMMENT="/""/"[^\r\n]*

%%

<YYINITIAL> {
  "#" [\ \t]* "include" { yybegin(INCLUDE); return GlslTokens.PREPROCESSOR_DIRECTIVE; }
  "#" [\ \t]* {IDENTIFIER} { return GlslTokens.PREPROCESSOR_DIRECTIVE; }
  "#" { return GlslTokens.PREPROCESSOR_DIRECTIVE; }
}

<YYINITIAL, INCLUDE> {
  {WHITE_SPACE} { return GlslTokens.WHITE_SPACE; }
  {BLOCK_COMMENT} { return GlslTokens.BLOCK_COMMENT; }
  {LINE_COMMENT} { return GlslTokens.LINE_COMMENT; }
}

<INCLUDE> {
  \"[^\"\r\n]*\"? { yybegin(YYINITIAL); return GlslTokens.INCLUDE_PATH; }
  "<" [^>\r\n]* ">"? { yybegin(YYINITIAL); return GlslTokens.INCLUDE_PATH; }
  [^] { yybegin(YYINITIAL); yypushback(1); }
}

<YYINITIAL> {
  [^\ \t\f\r\n\\/]+ { return GlslTokens.PREPROCESSOR_TEXT; }
  [^] { return GlslTokens.PREPROCESSOR_TEXT; }
}
