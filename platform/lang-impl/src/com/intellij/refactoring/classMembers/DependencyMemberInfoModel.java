/*
 * Created by IntelliJ IDEA.
 * User: dsl
 * Date: 09.07.2002
 * Time: 15:03:42
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.refactoring.classMembers;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class DependencyMemberInfoModel<T extends PsiElement, M extends MemberInfoBase<T>> implements MemberInfoModel<T, M> {
  protected MemberDependencyGraph<T, M> myMemberDependencyGraph;
  private final int myProblemLevel;
  private MemberInfoTooltipManager myTooltipManager;

  public DependencyMemberInfoModel(MemberDependencyGraph<T, M> memberDependencyGraph, int problemLevel) {
    myMemberDependencyGraph = memberDependencyGraph;
    myProblemLevel = problemLevel;
  }

  public void setTooltipProvider(MemberInfoTooltipManager.TooltipProvider <T, M> tooltipProvider) {
    myTooltipManager = new MemberInfoTooltipManager<T, M>(tooltipProvider);
  }

  public boolean isAbstractEnabled(M member) {
    return true;
  }

  public boolean isAbstractWhenDisabled(M member) {
    return false;
  }

  public boolean isMemberEnabled(M member) {
    return true;
  }

  public int checkForProblems(@NotNull M memberInfo) {
    if (memberInfo.isChecked()) return OK;
    final T member = memberInfo.getMember();

    if (myMemberDependencyGraph.getDependent().contains(member)) {
      return myProblemLevel;
    }
    return OK;
  }

  public void setMemberDependencyGraph(MemberDependencyGraph<T, M> memberDependencyGraph) {
    myMemberDependencyGraph = memberDependencyGraph;
  }

  public void memberInfoChanged(MemberInfoChange<T, M> event) {
    memberInfoChanged(event.getChangedMembers());
  }

  public void memberInfoChanged(final Collection<M> changedMembers) {
    if (myTooltipManager != null) myTooltipManager.invalidate();
    for (M changedMember : changedMembers) {
      myMemberDependencyGraph.memberChanged(changedMember);
    }
  }

  public String getTooltipText(M member) {
    if (myTooltipManager != null) {
      return myTooltipManager.getTooltip(member);
    } else {
      return null;
    }
  }
}