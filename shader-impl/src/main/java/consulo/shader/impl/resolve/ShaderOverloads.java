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
package consulo.shader.impl.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.ResolveResult;
import consulo.shader.psi.ShaderCallExpression;
import consulo.shader.psi.ShaderFunction;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderParameter;
import consulo.shader.psi.ShaderReferenceElement;
import consulo.shader.psi.ShaderTemplateParameter;
import consulo.shader.impl.type.ShaderType;
import consulo.shader.impl.type.ShaderTypeEvaluator;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the declarations a name may mean into resolve results: a function prototype is dropped when a definition with
 * as many parameters exists, and for a call the overloads that take its arguments and whose parameter types fit the
 * argument types best are valid; the others stay as invalid results, so that navigation still offers them and
 * highlighting can explain them. Overloads that fit equally well are all valid: the call is ambiguous.
 */
public final class ShaderOverloads {
    private ShaderOverloads() {
    }

    @RequiredReadAction
    public static ResolveResult[] toResults(ShaderReferenceElement reference, List<? extends ShaderNamedElement> declarations) {
        PsiElement parent = reference.getParent();
        List<PsiElement> arguments = parent instanceof ShaderCallExpression call && call.getCallee() == reference ? call.getArguments() : null;
        List<ShaderNamedElement> candidates = new ArrayList<>();
        List<ShaderNamedElement> rejected = new ArrayList<>();
        for (ShaderNamedElement declaration : declarations) {
            if (declaration instanceof ShaderFunction function && !function.isDefinition() && hasDefinition(function, declarations)) {
                continue;
            }
            boolean accepts = arguments == null || !(declaration instanceof ShaderFunction function) || accepts(function, arguments.size());
            (accepts ? candidates : rejected).add(declaration);
        }
        if (arguments != null && candidates.size() > 1) {
            // of the overloads that take the arguments, those whose parameter types match them best
            List<@Nullable ShaderType> argumentTypes = new ArrayList<>();
            for (PsiElement argument : arguments) {
                argumentTypes.add(ShaderTypeEvaluator.typeOf(argument));
            }
            int best = Integer.MIN_VALUE;
            List<ShaderNamedElement> bestCandidates = new ArrayList<>();
            for (ShaderNamedElement candidate : candidates) {
                int score = candidate instanceof ShaderFunction function ? score(function, argumentTypes) : 0;
                if (score > best) {
                    best = score;
                    rejected.addAll(bestCandidates);
                    bestCandidates.clear();
                }
                (score == best ? bestCandidates : rejected).add(candidate);
            }
            candidates = bestCandidates;
        }
        List<ResolveResult> results = new ArrayList<>();
        for (ShaderNamedElement candidate : candidates) {
            results.add(new PsiElementResolveResult(candidate, true));
        }
        for (ShaderNamedElement declaration : rejected) {
            results.add(new PsiElementResolveResult(declaration, false));
        }
        return results.toArray(ResolveResult.EMPTY_ARRAY);
    }

    @RequiredReadAction
    public static boolean isCallee(ShaderReferenceElement reference) {
        return reference.getParent() instanceof ShaderCallExpression call && call.getCallee() == reference;
    }

    /**
     * How well the parameters fit the argument types: 2 for the same type, 1 for a template parameter, 0 when either
     * type is unknown, -1 for a different type.
     */
    @RequiredReadAction
    private static int score(ShaderFunction function, List<@Nullable ShaderType> argumentTypes) {
        List<? extends ShaderParameter> parameters = function.getShaderParameters();
        int score = 0;
        for (int i = 0; i < parameters.size() && i < argumentTypes.size(); i++) {
            ShaderType argument = argumentTypes.get(i);
            ShaderType parameter = ShaderTypeEvaluator.declaredType(parameters.get(i));
            if (argument == null || parameter == null || argument.name() == null) {
                continue;
            }
            if (parameter.declaration() instanceof ShaderTemplateParameter) {
                score += 1;
            }
            else if (argument.name().equals(parameter.name()) && argument.arrayDepth() == parameter.arrayDepth()) {
                score += 2;
            }
            else {
                score -= 1;
            }
        }
        return score;
    }

    @RequiredReadAction
    private static boolean accepts(ShaderFunction function, int arguments) {
        List<? extends ShaderParameter> parameters = function.getShaderParameters();
        int required = 0;
        for (ShaderParameter parameter : parameters) {
            if (!hasDefault(parameter)) {
                required++;
            }
        }
        // f(void) has a parameter without a name
        int total = parameters.size() == 1 && parameters.get(0).getNameIdentifier() == null ? 0 : parameters.size();
        return arguments >= Math.min(required, total) && arguments <= total;
    }

    @RequiredReadAction
    private static boolean hasDefault(ShaderParameter parameter) {
        for (PsiElement child = parameter.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getFirstChild() == null && child.getText().equals("=")) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean hasDefinition(ShaderFunction prototype, List<? extends ShaderNamedElement> declarations) {
        for (ShaderNamedElement declaration : declarations) {
            if (declaration instanceof ShaderFunction function && function.isDefinition()
                && function.getShaderParameters().size() == prototype.getShaderParameters().size()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The target to show or evaluate when a caller needs one element: the first valid result, else the first result.
     */
    public static @Nullable PsiElement bestTarget(ResolveResult[] results) {
        for (ResolveResult result : results) {
            if (result.isValidResult()) {
                return result.getElement();
            }
        }
        return results.length == 0 ? null : results[0].getElement();
    }
}
