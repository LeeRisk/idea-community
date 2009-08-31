/*
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: Jul 23, 2002
 * Time: 3:01:22 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.codeInsight.completion;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;

public class JavaCharFilter extends CharFilter {

  private static boolean isWithinLiteral(final Lookup lookup) {
    PsiElement psiElement = lookup.getPsiElement();
    return psiElement != null && psiElement.getParent() instanceof PsiLiteralExpression;
  }

  public Result acceptChar(char c, final int prefixLength, final Lookup lookup) {
    if (!lookup.isCompletion()) return null;

    if (c == '!') {
      if (lookup.getPsiFile() instanceof PsiJavaFile) {
        final LookupElement item = lookup.getCurrentItem();
        if (item == null) return null;

        final Object o = item.getObject();
        if (o instanceof PsiVariable) {
          if (PsiType.BOOLEAN.isAssignableFrom(((PsiVariable)o).getType())) return Result.SELECT_ITEM_AND_FINISH_LOOKUP;
        }
        if (o instanceof PsiMethod) {
          final PsiType type = ((PsiMethod)o).getReturnType();
          if (type != null && PsiType.BOOLEAN.isAssignableFrom(type)) return Result.SELECT_ITEM_AND_FINISH_LOOKUP;
        }

        return null;
      }
    }
    if (c == '.' && isWithinLiteral(lookup)) return Result.ADD_TO_PREFIX;

    if (c == '#' && PsiTreeUtil.getParentOfType(lookup.getPsiElement(), PsiDocComment.class) != null) {
      final LookupElement item = lookup.getCurrentItem();
      if (item != null && item.getObject() instanceof PsiClass) {
        return Result.SELECT_ITEM_AND_FINISH_LOOKUP;
      }
    }
    return null;
  }

}