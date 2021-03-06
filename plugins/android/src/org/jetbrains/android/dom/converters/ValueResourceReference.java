/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package org.jetbrains.android.dom.converters;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.android.dom.wrappers.ValueResourceElementWrapper;
import org.jetbrains.android.dom.resources.ResourceValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class ValueResourceReference extends BaseResourceReference {
  private final ArrayList<PsiElement> myTargets;

  public ValueResourceReference(GenericDomValue<ResourceValue> value, @Nullable Collection<PsiElement> targets) {
    super(value);
    myTargets = new ArrayList<PsiElement>(targets);
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    if (myTargets == null) return ResolveResult.EMPTY_ARRAY;
    List<ResolveResult> result = new ArrayList<ResolveResult>();
    for (PsiElement target : myTargets) {
      PsiElement e = target instanceof NavigationItem && target instanceof XmlAttributeValue
                     ? new ValueResourceElementWrapper((XmlAttributeValue)target)
                     : target;
      result.add(new PsiElementResolveResult(e));
    }
    return result.toArray(new ResolveResult[result.size()]);
  }
}
