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
package consulo.shader.impl.builtins;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.Language;
import consulo.language.file.light.ReadOnlyLightVirtualFile;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.shader.psi.ShaderDeclarationContainer;
import consulo.shader.psi.ShaderDeclarationProvider;
import consulo.shader.psi.ShaderNameKind;
import consulo.shader.psi.ShaderNamedElement;
import consulo.shader.psi.ShaderTemplateParameter;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Built-in declarations generated as a read-only source file, so that references to built-ins have a declaration to
 * resolve, show and navigate to. The language version of the file follows its name.
 */
public final class GeneratedDeclarationsFile {
    private static final Key<Boolean> GENERATED = Key.create("GeneratedDeclarationsFile");

    private final PsiFile myFile;
    private final Map<ShaderNameKind, Map<String, List<ShaderNamedElement>>> myDeclarations = new EnumMap<>(ShaderNameKind.class);

    @RequiredReadAction
    public GeneratedDeclarationsFile(Project project, Language language, String fileName, String text) {
        // backed by a read-only light file, so that navigation can open it in an editor
        ReadOnlyLightVirtualFile virtualFile = new ReadOnlyLightVirtualFile(fileName, language, text);
        myFile = Objects.requireNonNull(PsiManager.getInstance(project).findFile(virtualFile));
        myFile.putUserData(GENERATED, Boolean.TRUE);
        for (ShaderNameKind kind : ShaderNameKind.values()) {
            Map<String, List<ShaderNamedElement>> declarations = new HashMap<>();
            ShaderDeclarationContainer.processContents(myFile, kind, declaration -> {
                String name = declaration.getReferencedName();
                // the type parameters of the built-in templates are no types of their own
                if (name != null && !(declaration instanceof ShaderTemplateParameter)) {
                    declarations.computeIfAbsent(name, key -> new ArrayList<>()).add(declaration);
                }
                return true;
            });
            myDeclarations.put(kind, declarations);
        }
    }

    /**
     * The built-in variable or function named {@code name}; for an overloaded function its first overload.
     */
    public @Nullable ShaderNamedElement find(String name) {
        return find(name, ShaderNameKind.VALUE);
    }

    /**
     * The built-in of {@code kind} named {@code name}.
     */
    public @Nullable ShaderNamedElement find(String name, ShaderNameKind kind) {
        List<ShaderNamedElement> declarations = findAll(name, kind);
        return declarations.isEmpty() ? null : declarations.get(0);
    }

    /**
     * The built-ins of {@code kind} named {@code name}: every overload of a function.
     */
    public List<ShaderNamedElement> findAll(String name, ShaderNameKind kind) {
        Map<String, List<ShaderNamedElement>> declarations = myDeclarations.get(kind);
        List<ShaderNamedElement> found = declarations == null ? null : declarations.get(name);
        return found == null ? List.of() : found;
    }

    /**
     * The names of the built-ins of {@code kind}.
     */
    public Set<String> getNames(ShaderNameKind kind) {
        Map<String, List<ShaderNamedElement>> declarations = myDeclarations.get(kind);
        return declarations == null ? Set.of() : declarations.keySet();
    }

    public PsiFile getFile() {
        return myFile;
    }

    /**
     * Whether {@code element} is a declaration of a generated built-ins file.
     */
    @RequiredReadAction
    public static boolean isBuiltin(PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null && Boolean.TRUE.equals(file.getUserData(GENERATED));
    }
}
