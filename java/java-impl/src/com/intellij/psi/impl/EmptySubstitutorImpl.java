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
package com.intellij.psi.impl;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 *  @author dsl
 */
public final class EmptySubstitutorImpl extends EmptySubstitutor {
  public PsiType substitute(@NotNull PsiTypeParameter typeParameter){
    return JavaPsiFacade.getInstance(typeParameter.getProject()).getElementFactory().createType(typeParameter);
  }

  public PsiType substitute(PsiType type){
    return type;
  }

  public PsiType substituteWithBoundsPromotion(PsiTypeParameter typeParameter) {
    return JavaPsiFacade.getInstance(typeParameter.getProject()).getElementFactory().createType(typeParameter);
  }

  public PsiSubstitutor put(PsiTypeParameter classParameter, PsiType mapping){
    final PsiSubstitutor substitutor = new PsiSubstitutorImpl();
    return substitutor.put(classParameter, mapping);
  }
  public PsiSubstitutor putAll(PsiClass parentClass, PsiType[] mappings){
    if(!parentClass.hasTypeParameters()) return this;
    final PsiSubstitutor substitutor = new PsiSubstitutorImpl();
    return substitutor.putAll(parentClass, mappings);
  }

  public PsiSubstitutor putAll(PsiSubstitutor another) {
    return another;
  }

  @NotNull
  public Map<PsiTypeParameter, PsiType> getSubstitutionMap() {
    return Collections.emptyMap();
  }

  public boolean isValid() {
    return true;
  }

}
