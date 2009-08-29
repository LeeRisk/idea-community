/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.psi.templateLanguages;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.analysis.ErrorQuickFixProvider;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.LangBundle;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class TemplateLanguageErrorQuickFixProvider implements ErrorQuickFixProvider{

  public void registerErrorQuickFix(final PsiErrorElement errorElement, final HighlightInfo highlightInfo) {
    final PsiFile psiFile = errorElement.getContainingFile();
    final FileViewProvider provider = psiFile.getViewProvider();
    if (!(provider instanceof TemplateLanguageFileViewProvider)) return;
    if (psiFile.getLanguage() != ((TemplateLanguageFileViewProvider) provider).getTemplateDataLanguage()) return;

    QuickFixAction.registerQuickFixAction(highlightInfo, createChangeTemplateDataLanguageFix(errorElement));

  }

  public static IntentionAction createChangeTemplateDataLanguageFix(final PsiElement errorElement) {
    final PsiFile containingFile = errorElement.getContainingFile();
    final VirtualFile virtualFile = containingFile.getVirtualFile();
    final Language language = ((TemplateLanguageFileViewProvider) containingFile.getViewProvider()).getTemplateDataLanguage();
    return new IntentionAction() {

      @NotNull
      public String getText() {
        return LangBundle.message("quickfix.change.template.data.language.text", language.getDisplayName());
      }

      @NotNull
      public String getFamilyName() {
        return getText();
      }

      public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
        return true;
      }

      public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        final TemplateDataLanguageConfigurable configurable = TemplateDataLanguageConfigurable.getInstance(project);
        ShowSettingsUtil.getInstance().editConfigurable(project, configurable, new Runnable() {
          public void run() {
            if (virtualFile != null) {
              configurable.selectFile(virtualFile);
            }
          }
        });
      }

      public boolean startInWriteAction() {
        return false;
      }
    };
  }

}