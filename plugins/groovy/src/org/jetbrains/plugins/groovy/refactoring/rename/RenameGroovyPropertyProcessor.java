/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package org.jetbrains.plugins.groovy.refactoring.rename;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenameJavaVariableProcessor;
import com.intellij.refactoring.util.RefactoringMessageUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.GrModifier;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.lang.psi.util.GroovyPropertyUtils;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

import java.util.*;

/**
 * @author ilyas
 */
public class RenameGroovyPropertyProcessor extends RenameJavaVariableProcessor {

  @NotNull
  @Override
  public Collection<PsiReference> findReferences(final PsiElement element) {
    if (element instanceof GrField) {
      ArrayList<PsiReference> refs = new ArrayList<PsiReference>();

      GrField field = (GrField)element;
      PsiMethod setter = GroovyPropertyUtils.findSetterForField(field);
      GlobalSearchScope projectScope = GlobalSearchScope.projectScope(element.getProject());
      if (setter != null && setter instanceof GrAccessorMethod) {
        refs.addAll(RenameAliasedUsagesUtil.filterAliasedRefs(MethodReferencesSearch.search(setter, projectScope, true).findAll(), setter));
      }
      GrAccessorMethod[] getters = field.getGetters();
      for (GrAccessorMethod getter : getters) {
        refs.addAll(RenameAliasedUsagesUtil.filterAliasedRefs(MethodReferencesSearch.search(getter, projectScope, true).findAll(), getter));
      }
      refs.addAll(RenameAliasedUsagesUtil.filterAliasedRefs(ReferencesSearch.search(field, projectScope, true).findAll(), field));
      return refs;
    }
    return super.findReferences(element);
  }

  @Override
  public void renameElement(final PsiElement psiElement,
                            final String newName,
                            final UsageInfo[] usages,
                            final RefactoringElementListener listener) throws IncorrectOperationException {

    final GrField field = (GrField)psiElement;
    final PsiMethod getter = GroovyPropertyUtils.findGetterForField(field);
    final PsiMethod setter = GroovyPropertyUtils.findSetterForField(field);
    final String newGetterName = (getter != null && getter.getName().startsWith("is") ? "is" : "get") + StringUtil.capitalize(newName);
    final String newSetterName = "set" + StringUtil.capitalize(newName);

    final PsiManager manager = field.getManager();

    List<PsiReference> getterRefs = new ArrayList<PsiReference>();
    List<PsiReference> setterRefs = new ArrayList<PsiReference>();
    List<PsiReference> fieldRefs = new ArrayList<PsiReference>();

    for (UsageInfo usage : usages) {
      final PsiElement element = usage.getElement();
      if (element == null) continue;

      PsiReference ref = element.findReferenceAt(usage.startOffset);
      if (ref == null) continue;

      PsiElement resolved = ref.resolve();
      if (manager.areElementsEquivalent(resolved, getter)) {
        if (isPropertyAccess(element)) {
          fieldRefs.add(ref);
        }
        else {
          getterRefs.add(ref);
        }
      }
      else if (manager.areElementsEquivalent(resolved, setter)) {
        if (isPropertyAccess(element)) {
          fieldRefs.add(ref);
        }
        else {
          setterRefs.add(ref);
        }
      }
      else if (manager.areElementsEquivalent(resolved, field)) {
        fieldRefs.add(ref);
      }
      else {
        ref.handleElementRename(newName);
      }
    }

    field.setName(newName);

    final PsiMethod newGetter = GroovyPropertyUtils.findGetterForField(field);
    doRename(newGetterName, manager, getterRefs, newGetter);

    final PsiMethod newSetter = GroovyPropertyUtils.findSetterForField(field);
    doRename(newSetterName, manager, setterRefs, newSetter);

    doRename(newName, manager, fieldRefs, field);

    listener.elementRenamed(field);
  }

  private static void doRename(String newName, PsiManager manager, List<PsiReference> refs, PsiMember member) {
    for (PsiReference ref : refs) {
      rename(ref, newName, manager, member);
    }
  }

  private static void rename(PsiReference ref,
                             String newName,
                             PsiManager manager,
                             PsiMember elementToResolve) {
    final PsiElement renamed = ref.handleElementRename(newName);
    PsiElement newly_resolved = ref.resolve();
    if (!manager.areElementsEquivalent(newly_resolved, elementToResolve)) {
      if (newly_resolved instanceof PsiMethod) {
        newly_resolved = GroovyPropertyUtils.findFieldForAccessor((PsiMethod)newly_resolved, false);
      }
      if (!manager.areElementsEquivalent(newly_resolved, elementToResolve)) {
        qualify(elementToResolve, renamed, newName);
      }
    }
  }

  private static boolean isPropertyAccess(PsiElement element) {
    return !(element.getParent() instanceof GrCall) && (PsiTreeUtil.getParentOfType(element, GrImportStatement.class, true) == null);
  }

  private static void qualify(PsiMember member, PsiElement renamed, String name) {
    if (!(renamed instanceof GrReferenceExpression)) return;

    final PsiClass clazz = member.getContainingClass();
    if (clazz == null) return;

    final GrReferenceExpression refExpr = (GrReferenceExpression)renamed;
    final PsiElement replaced;
    if (member.hasModifierProperty(GrModifier.STATIC)) {
      final GrReferenceExpression newRefExpr = GroovyPsiElementFactory.getInstance(member.getProject())
        .createReferenceExpressionFromText(clazz.getQualifiedName() + "." + name);
      replaced = refExpr.replace(newRefExpr);
    }
    else {
      final PsiClass containingClass = PsiTreeUtil.getParentOfType(renamed, PsiClass.class);
      if (member.getManager().areElementsEquivalent(containingClass, clazz)) {
        final GrReferenceExpression newRefExpr = GroovyPsiElementFactory.getInstance(member.getProject())
          .createReferenceExpressionFromText("this." + name);
        replaced = refExpr.replace(newRefExpr);
      }
      else {
        final GrReferenceExpression newRefExpr = GroovyPsiElementFactory.getInstance(member.getProject())
          .createReferenceExpressionFromText(clazz.getQualifiedName() + ".this." + name);
        replaced = refExpr.replace(newRefExpr);
      }
    }
    PsiUtil.shortenReferences((GroovyPsiElement)replaced);
  }

  @Override
  public boolean canProcessElement(@NotNull final PsiElement element) {
    return element instanceof GrField && ((GrField)element).isProperty();
  }

  @Override
  public void prepareRenaming(final PsiElement element, final String newName, final Map<PsiElement, String> allRenames) {
    GrField field = (GrField)element;
    final PsiMethod getter = GroovyPropertyUtils.findGetterForField(field);
    final PsiMethod setter = GroovyPropertyUtils.findSetterForField(field);
    if (getter != null && !(getter instanceof GrAccessorMethod) || setter != null && !(setter instanceof GrAccessorMethod)) {
      if (askToRenameAccesors(getter, setter, newName, element.getProject())) {
        if (getter != null && !(getter instanceof GrAccessorMethod)) {
          String name = getter.getName();
          allRenames.put(getter, name.startsWith("is") ? "is" + StringUtil.capitalize(newName) : "get" + StringUtil.capitalize(newName));
        }
        if (setter != null && !(setter instanceof GrAccessorMethod)) {
          allRenames.put(setter, "set" + StringUtil.capitalize(newName));
        }
      }
    }
  }

  private static boolean askToRenameAccesors(PsiMethod getter, PsiMethod setter, String newName, final Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode()) return false;
    String text = RefactoringMessageUtil.getGetterSetterMessage(newName, RefactoringBundle.message("rename.title"), getter, setter);
    return Messages.showYesNoDialog(project, text, RefactoringBundle.message("rename.title"), Messages.getQuestionIcon()) == 0;
  }

  @Override
  public void findCollisions(PsiElement element,
                             String newName,
                             Map<? extends PsiElement, String> allRenames,
                             List<UsageInfo> result) {
    super.findCollisions(element, newName, allRenames, result);
  }


}
