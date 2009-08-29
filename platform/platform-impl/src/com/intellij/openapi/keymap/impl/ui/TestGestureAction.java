package com.intellij.openapi.keymap.impl.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnActionEventVisitor;
import com.intellij.openapi.actionSystem.KeyboardGestureAction;
import com.intellij.openapi.project.DumbAware;

public class TestGestureAction extends AnAction implements KeyboardGestureAction, DumbAware {
  public void actionPerformed(final AnActionEvent e) {
    e.accept(new AnActionEventVisitor() {
      @Override
      public void visitGestureInitEvent(final AnActionEvent e) {
        System.out.println("TestGestureAction.visitGestureInitEvent");
      }

      @Override
      public void visitGesturePerformedEvent(final AnActionEvent e) {
        System.out.println("TestGestureAction.visitGesturePerformedEvent");
      }

      @Override
      public void visitGestureFinishEvent(final AnActionEvent e) {
        System.out.println("TestGestureAction.visitGestureFinishEvent");
      }

      @Override
      public void visitEvent(final AnActionEvent e) {
        System.out.println("TestGestureAction.visitEvent");
      }
    });
  }
}