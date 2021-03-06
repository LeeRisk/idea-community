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
package com.intellij.ide.structureView.impl.java;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.smartTree.*;
import com.intellij.psi.PsiElement;
import com.intellij.util.Icons;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class PropertiesGrouper implements Grouper{
  @NonNls public static final String ID = "SHOW_PROPERTIES";

  @NotNull
  public Collection<Group> group(final AbstractTreeNode parent, Collection<TreeElement> children) {
    if (parent.getValue() instanceof PropertyGroup) return Collections.emptyList();
    Map<Group,Group> result = new THashMap<Group, Group>();
    for (TreeElement o : children) {
      if (o instanceof JavaClassTreeElementBase) {
        PsiElement element = ((JavaClassTreeElementBase)o).getElement();
        PropertyGroup group = PropertyGroup.createOn(element, o);
        if (group != null) {
          PropertyGroup existing = (PropertyGroup)result.get(group);
          if (existing != null) {
            existing.copyAccessorsFrom(group);
          }
          else {
            result.put(group, group);
          }
        }
      }
    }
    for (Iterator<Group> iterator = result.keySet().iterator(); iterator.hasNext();) {
      PropertyGroup group = (PropertyGroup)iterator.next();
      if (!group.isComplete()) {
        iterator.remove();
      }
    }
    return result.values();
  }

  @NotNull
  public ActionPresentation getPresentation() {
    return new ActionPresentationData(IdeBundle.message("action.structureview.show.properties"), null, Icons.PROPERTY_ICON);
  }

  @NotNull
  public String getName() {
    return ID;
  }
}
