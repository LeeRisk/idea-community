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

/*
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Apr 22, 2002
 * Time: 5:51:22 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.openapi.editor.impl;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.editor.ex.DocumentEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FoldRegionImpl extends RangeMarkerImpl implements FoldRegion {
  private boolean myIsExpanded;
  private final Editor myEditor;
  private final String myPlaceholderText;
  private final FoldingGroup myGroup;
  private final boolean myShouldNeverExpand;

  FoldRegionImpl(@NotNull Editor editor,
                 int startOffset,
                 int endOffset,
                 @NotNull String placeholder,
                 FoldingGroup group,
                 boolean shouldNeverExpand) {
    super((DocumentEx)editor.getDocument(), startOffset, endOffset);
    myGroup = group;
    myShouldNeverExpand = shouldNeverExpand;
    myIsExpanded = true;
    myEditor = editor;
    myPlaceholderText = placeholder;
  }

  public boolean isExpanded() {
    return myIsExpanded;
  }

  public void setExpanded(boolean expanded) {
    FoldingModelImpl foldingModel = (FoldingModelImpl)myEditor.getFoldingModel();
    if (myGroup == null) {
      doSetExpanded(expanded, foldingModel, this);
    } else {
      for (final FoldRegion region : foldingModel.getGroupedRegions(myGroup)) {
        doSetExpanded(expanded, foldingModel, region);
      }
    }
  }

  private static void doSetExpanded(boolean expanded, FoldingModelImpl foldingModel, FoldRegion region) {
    if (expanded) {
      foldingModel.expandFoldRegion(region);
    }
    else{
      foldingModel.collapseFoldRegion(region);
    }
  }

  public boolean isValid() {
    return super.isValid() && myStart + 1 < myEnd;
  }

  public void setExpandedInternal(boolean toExpand) {
    myIsExpanded = toExpand;
  }

  @NotNull
  public String getPlaceholderText() {
    return myPlaceholderText;
  }

  public Editor getEditor() {
    return myEditor;
  }

  @Nullable
  public FoldingGroup getGroup() {
    return myGroup;
  }

  @Override
  public boolean shouldNeverExpand() {
    return myShouldNeverExpand;
  }

  public String toString() {
    return "FoldRegion " + (isExpanded() ? "-" : "+") +
           "(" + getStartOffset() + ":" + getEndOffset() + ")" + (isValid() ? "" : "(invalid)");
  }
}
