/*
 * Copyright 2007-2008 Dave Griffith
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
package org.jetbrains.plugins.groovy.codeInspection.confusing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspection;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspectionVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;

public class GroovyEmptyStatementBodyInspection extends BaseInspection {

  @NotNull
  public String getDisplayName() {
    return "Statement with empty body";
  }

  @NotNull
  public String getGroupDisplayName() {
    return CONFUSING_CODE_CONSTRUCTS;
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  public String buildErrorString(Object... args) {
    if (args[0] instanceof GrIfStatement) {
      return "'#ref' Statement has empty branch";
    } else {
      return "'#ref' Statement has empty body";
    }
  }

  public BaseInspectionVisitor buildVisitor() {
    return new Visitor();
  }

  private static class Visitor extends BaseInspectionVisitor {

    public void visitWhileStatement(@NotNull GrWhileStatement statement) {
      super.visitWhileStatement(statement);
      final GrStatement body = statement.getBody();
      if (body == null) {
        return;
      }
      if (!isEmpty(body)) {
        return;
      }
      registerStatementError(statement, statement);
    }

    public void visitForStatement(@NotNull GrForStatement statement) {
      super.visitForStatement(statement);
      final GrStatement body = statement.getBody();
      if (body == null) {
        return;
      }
      if (!isEmpty(body)) {
        return;
      }
      registerStatementError(statement, statement);
    }


    public void visitIfStatement(@NotNull GrIfStatement statement) {
      super.visitIfStatement(statement);
      final GrStatement thenBranch = statement.getThenBranch();
      if (thenBranch != null) {
        if (isEmpty(thenBranch)) {
          registerStatementError(statement, statement);
          return;
        }
      }
      final GrStatement elseBranch = statement.getElseBranch();

      if (elseBranch != null) {
        if (isEmpty(elseBranch)) {
          registerStatementError(statement, statement);
        }
      }
    }

    private static boolean isEmpty(GroovyPsiElement body) {
      if (!(body instanceof GrBlockStatement)) {
        return false;
      }
      final GrBlockStatement block = (GrBlockStatement) body;
      final GrStatement[] statements = block.getBlock().getStatements();
      return statements.length == 0;
    }
  }
}
