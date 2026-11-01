/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.shader.shaderlab.lang.pragma;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts shader program information (shader target, surface shader flag, shader keyword features) from the
 * {@code #pragma} directives of a program block text (the body of CGPROGRAM/HLSLPROGRAM ... ENDCG/ENDHLSL).
 * Pure string logic, no IDE dependencies.
 * <p>
 * Ported from JetBrains resharper-unity (src/Unity.Shaders/ShaderLab/Psi/Caches/ShaderProgramCache.cs, region
 * "ShaderProgramInfo data"), Apache License 2.0. JetBrains walks the C++ lexer tokens; this port finds the directives
 * itself (skipping comments and string literals) and keeps exact source offsets for backslash-continued lines.
 */
public final class UnityShaderVariantParser
{
    /**
     * A keyword declared by a pragma, with its range in the parsed text
     */
    public record Keyword(String name, int startOffset, int endOffset)
    {
    }

    /**
     * One keyword set ({@code #pragma multi_compile A B C}); exactly one keyword of the set (or none, when
     * {@link #allowAllKeywordsDisabled()}) is enabled in a variant
     */
    public record ShaderFeature(List<Keyword> keywords, boolean allowAllKeywordsDisabled)
    {
    }

    /**
     * A {@code #pragma} directive
     *
     * @param name        pragma name, e.g. {@code multi_compile}
     * @param arguments   pragma arguments with comments and line continuations removed
     * @param startOffset offset of the {@code #} in the parsed text
     * @param endOffset   end offset of the directive (exclusive, before the line break)
     */
    public record PragmaDirective(String name, List<String> arguments, int startOffset, int endOffset)
    {
    }

    public record Result(boolean isSurface,
                         int shaderTarget,
                         List<ShaderFeature> shaderFeatures,
                         Map<String, String> definedMacros,
                         List<PragmaDirective> pragmas)
    {
    }

    private final boolean myCompute;

    private boolean myIsSurface;
    private int myShaderTarget = UnityShaderPragmas.SHADER_TARGET_25;
    private final List<ShaderFeature> myShaderFeatures = new ArrayList<>();
    private final Map<String, String> myDefinedMacros = new LinkedHashMap<>();
    private final List<PragmaDirective> myPragmas = new ArrayList<>();

    private UnityShaderVariantParser(boolean compute)
    {
        myCompute = compute;
    }

    /**
     * Parses a ShaderLab program block body
     */
    public static Result parse(CharSequence text)
    {
        return parse(text, false);
    }

    /**
     * @param compute {@code true} for compute shader files - uses {@link UnityShaderPragmas#COMPUTE_PRAGMAS}
     */
    public static Result parse(CharSequence text, boolean compute)
    {
        UnityShaderVariantParser parser = new UnityShaderVariantParser(compute);
        parser.read(text);
        parser.myDefinedMacros.put("SHADER_TARGET", String.valueOf(parser.myShaderTarget));
        return new Result(parser.myIsSurface,
            parser.myShaderTarget,
            Collections.unmodifiableList(parser.myShaderFeatures),
            Collections.unmodifiableMap(parser.myDefinedMacros),
            Collections.unmodifiableList(parser.myPragmas));
    }

    private void read(CharSequence text)
    {
        int length = text.length();
        int i = 0;
        boolean lineStart = true;
        while (i < length)
        {
            char ch = text.charAt(i);
            if (ch == '\n')
            {
                lineStart = true;
                i++;
            }
            else if (ch == '/' && i + 1 < length && text.charAt(i + 1) == '/')
            {
                i = skipToLineEnd(text, i);
            }
            else if (ch == '/' && i + 1 < length && text.charAt(i + 1) == '*')
            {
                int end = indexOf(text, "*/", i + 2);
                i = end < 0 ? length : end + 2;
            }
            else if (ch == '"' || ch == '\'')
            {
                i = skipQuoted(text, i, ch);
                lineStart = false;
            }
            else if (Character.isWhitespace(ch))
            {
                i++;
            }
            else if (ch == '#' && lineStart)
            {
                i = readDirective(text, i);
                lineStart = false;
            }
            else
            {
                lineStart = false;
                i++;
            }
        }
    }

    /**
     * @return offset after the directive (at the line break or the end of text)
     */
    private int readDirective(CharSequence text, int hashOffset)
    {
        // collect the logical line: merge backslash continued lines, remember the source offset of every merged char
        int length = text.length();
        StringBuilder content = new StringBuilder();
        List<Integer> offsets = new ArrayList<>();
        int i = hashOffset + 1;
        while (i < length)
        {
            char ch = text.charAt(i);
            if (ch == '\\')
            {
                int next = i + 1;
                while (next < length && (text.charAt(next) == ' ' || text.charAt(next) == '\t' || text.charAt(next) == '\r'))
                {
                    next++;
                }
                if (next < length && text.charAt(next) == '\n')
                {
                    i = next + 1;
                    continue;
                }
            }
            if (ch == '\n')
            {
                break;
            }
            content.append(ch);
            offsets.add(i);
            i++;
        }
        int end = i;
        int directiveEnd = end > hashOffset + 1 && text.charAt(end - 1) == '\r' ? end - 1 : end;

        List<Slice> slices = split(content);
        if (slices.size() < 2 || !slices.get(0).text().equals("pragma"))
        {
            return end;
        }

        String pragmaType = slices.get(1).text();
        List<String> arguments = new ArrayList<>();
        List<Keyword> keywords = new ArrayList<>();
        for (int k = 2; k < slices.size(); k++)
        {
            Slice slice = slices.get(k);
            arguments.add(slice.text());
            int start = offsets.get(slice.start());
            int last = offsets.get(slice.start() + slice.text().length() - 1);
            keywords.add(new Keyword(slice.text(), start, last + 1));
        }

        myPragmas.add(new PragmaDirective(pragmaType, List.copyOf(arguments), hashOffset, directiveEnd));
        readPragmaCommand(pragmaType, keywords);
        return end;
    }

    private void readPragmaCommand(String pragmaType, List<Keyword> arguments)
    {
        switch (pragmaType)
        {
            case "surface":
                myIsSurface = true;
                break;
            case "target":
                if (!arguments.isEmpty())
                {
                    int versionFromTarget;
                    try
                    {
                        versionFromTarget = Integer.parseInt(arguments.get(0).name().replace(".", ""));
                    }
                    catch (NumberFormatException e)
                    {
                        versionFromTarget = UnityShaderPragmas.SHADER_TARGET_35;
                    }
                    myShaderTarget = Math.max(myShaderTarget, versionFromTarget);
                }
                break;
            case "multi_compile_lightpass":
                // multi_compile_lightpass == multi_compile DIRECTIONAL, DIRECTIONAL_COOKIE, POINT, POINT_NOATT, POINT_COOKIE, SPOT
                myDefinedMacros.put("DIRECTIONAL", "1");
                break;
            default:
                UnityShaderPragmas.Pragma pragma = myCompute ? UnityShaderPragmas.findComputePragma(pragmaType) : UnityShaderPragmas.findShaderPragma(pragmaType);
                if (pragma != null)
                {
                    ShaderFeature shaderFeature = readShaderFeature(pragma, arguments);
                    if (shaderFeature != null)
                    {
                        myShaderFeatures.add(shaderFeature);
                    }
                    if (pragma.impliedShaderTarget() > myShaderTarget)
                    {
                        myShaderTarget = pragma.impliedShaderTarget();
                    }
                }
                break;
        }
    }

    @Nullable
    private static ShaderFeature readShaderFeature(UnityShaderPragmas.Pragma pragma, List<Keyword> arguments)
    {
        UnityShaderPragmas.FeatureType featureType = pragma.featureType();
        if (featureType != UnityShaderPragmas.FeatureType.KEYWORD_LIST
            && featureType != UnityShaderPragmas.FeatureType.KEYWORD_LIST_WITH_DISABLED_VARIANT_FOR_SINGLE_KEYWORD)
        {
            return null;
        }

        boolean allowDisableAllKeywords = false;
        List<Keyword> entries = new ArrayList<>();
        for (Keyword keyword : arguments)
        {
            if (isNoKeywordMarker(keyword.name()))
            {
                allowDisableAllKeywords = true;
            }
            else
            {
                entries.add(keyword);
            }
        }

        if (entries.isEmpty())
        {
            return null;
        }

        if (!allowDisableAllKeywords)
        {
            allowDisableAllKeywords = entries.size() == 1
                && featureType == UnityShaderPragmas.FeatureType.KEYWORD_LIST_WITH_DISABLED_VARIANT_FOR_SINGLE_KEYWORD;
        }
        return new ShaderFeature(List.copyOf(entries), allowDisableAllKeywords);
    }

    /**
     * {@code _}, {@code __}... declare the "no keyword" variant
     */
    static boolean isNoKeywordMarker(String keyword)
    {
        for (int i = 0; i < keyword.length(); i++)
        {
            if (keyword.charAt(i) != '_')
            {
                return false;
            }
        }
        return true;
    }

    private record Slice(String text, int start)
    {
    }

    /**
     * Splits on whitespace and comments; a {@code //} comment ends the content
     */
    private static List<Slice> split(CharSequence content)
    {
        List<Slice> result = new ArrayList<>();
        int length = content.length();
        int i = 0;
        while (i < length)
        {
            char ch = content.charAt(i);
            if (Character.isWhitespace(ch))
            {
                i++;
                continue;
            }
            if (ch == '/' && i + 1 < length)
            {
                char next = content.charAt(i + 1);
                if (next == '/')
                {
                    break;
                }
                if (next == '*')
                {
                    int end = indexOf(content, "*/", i + 2);
                    i = end < 0 ? length : end + 2;
                    continue;
                }
            }

            int start = i;
            while (i < length && !Character.isWhitespace(content.charAt(i)) && !isCommentStart(content, i))
            {
                i++;
            }
            result.add(new Slice(content.subSequence(start, i).toString(), start));
        }
        return result;
    }

    private static boolean isCommentStart(CharSequence text, int i)
    {
        return text.charAt(i) == '/' && i + 1 < text.length() && (text.charAt(i + 1) == '/' || text.charAt(i + 1) == '*');
    }

    private static int skipToLineEnd(CharSequence text, int i)
    {
        int length = text.length();
        while (i < length && text.charAt(i) != '\n')
        {
            i++;
        }
        return i;
    }

    private static int skipQuoted(CharSequence text, int i, char quote)
    {
        int length = text.length();
        i++;
        while (i < length)
        {
            char ch = text.charAt(i);
            if (ch == '\\')
            {
                i += 2;
                continue;
            }
            if (ch == quote || ch == '\n')
            {
                return ch == quote ? i + 1 : i;
            }
            i++;
        }
        return length;
    }

    private static int indexOf(CharSequence text, String what, int from)
    {
        int max = text.length() - what.length();
        for (int i = Math.max(from, 0); i <= max; i++)
        {
            boolean match = true;
            for (int j = 0; j < what.length(); j++)
            {
                if (text.charAt(i + j) != what.charAt(j))
                {
                    match = false;
                    break;
                }
            }
            if (match)
            {
                return i;
            }
        }
        return -1;
    }
}
