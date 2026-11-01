package consulo.shader.hlsl.impl.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;

/* Lexes the text of one preprocessor line (HlslTokens.PREPROCESSOR_LINE). */
@SuppressWarnings({"ALL"})
@org.jspecify.annotations.NullUnmarked
%%

%public
%unicode
%class _HlslDirectiveLexer
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
  "#" [\ \t]* "include" { yybegin(INCLUDE); return HlslTokens.PREPROCESSOR_DIRECTIVE; }
  "#" [\ \t]* {IDENTIFIER} { return HlslTokens.PREPROCESSOR_DIRECTIVE; }
  "#" { return HlslTokens.PREPROCESSOR_DIRECTIVE; }
}

<YYINITIAL, INCLUDE> {
  {WHITE_SPACE} { return HlslTokens.WHITE_SPACE; }
  {BLOCK_COMMENT} { return HlslTokens.BLOCK_COMMENT; }
  {LINE_COMMENT} { return HlslTokens.LINE_COMMENT; }
}

<INCLUDE> {
  \"[^\"\r\n]*\"? { yybegin(YYINITIAL); return HlslTokens.INCLUDE_PATH; }
  "<" [^>\r\n]* ">"? { yybegin(YYINITIAL); return HlslTokens.INCLUDE_PATH; }
  [^] { yybegin(YYINITIAL); yypushback(1); }
}

<YYINITIAL> {
  [^\ \t\f\r\n\\/]+ { return HlslTokens.PREPROCESSOR_TEXT; }
  [^] { return HlslTokens.PREPROCESSOR_TEXT; }
}
