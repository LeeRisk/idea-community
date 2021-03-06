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
package com.intellij.openapi.roots.ui.util;

import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;

public abstract class BaseTextCommentCellAppearance implements CellAppearance {
  private SimpleTextAttributes myCommentAttributes = SimpleTextAttributes.GRAY_ATTRIBUTES;
  private SimpleTextAttributes myTextAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;

  protected abstract Icon getIcon();

  protected abstract String getSecondaryText();

  protected abstract String getPrimaryText();

  public void customize(SimpleColoredComponent component) {
    component.setIcon(getIcon());
    component.append(getPrimaryText(), myTextAttributes);
    String secondaryText = getSecondaryText();
    if (secondaryText != null && secondaryText.length() > 0)
      component.append(" (" + secondaryText + ")", myCommentAttributes);
  }

  public String getText() {
    String secondaryText = getSecondaryText();
    if (secondaryText != null && secondaryText.length() >0)
      return getPrimaryText() + " (" + secondaryText + ")";
    return getPrimaryText();
  }

  public void setCommentAttributes(SimpleTextAttributes commentAttributes) {
    myCommentAttributes = commentAttributes;
  }

  public void setTextAttributes(SimpleTextAttributes textAttributes) {
    myTextAttributes = textAttributes;
  }
}
