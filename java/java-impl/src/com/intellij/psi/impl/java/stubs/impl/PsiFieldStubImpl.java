/*
 * @author max
 */
package com.intellij.psi.impl.java.stubs.impl;

import com.intellij.psi.PsiField;
import com.intellij.psi.impl.cache.InitializerTooLongException;
import com.intellij.psi.impl.cache.TypeInfo;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.java.stubs.PsiAnnotationStub;
import com.intellij.psi.impl.java.stubs.PsiFieldStub;
import com.intellij.psi.impl.java.stubs.PsiModifierListStub;
import com.intellij.psi.impl.source.tree.java.PsiAnnotationImpl;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import com.intellij.codeInsight.daemon.impl.analysis.AnnotationsHighlightUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PsiFieldStubImpl extends StubBase<PsiField> implements PsiFieldStub {
  private static final int INITIALIZER_LENGTH_LIMIT = 1000;
  @NonNls public static final StringRef INITIALIZER_TOO_LONG = StringRef.fromString(";INITIALIZER_TOO_LONG;");

  private final StringRef myName;
  private final TypeInfo myType;
  private final StringRef myInitializer;
  private final byte myFlags;

  private static final int ENUM_CONST = 0x01;
  private static final int DEPRECATED = 0x02;
  private static final int DEPRECATED_ANNOTATION = 0x04;

  public PsiFieldStubImpl(final StubElement parent, final String name, @NotNull TypeInfo type, final String initializer, final byte flags) {
    this(parent, StringRef.fromString(name), type, StringRef.fromString(initializer), flags);
  }

  public PsiFieldStubImpl(final StubElement parent, final StringRef name, @NotNull TypeInfo type, final StringRef initializer, final byte flags) {
    super(parent, isEnumConst(flags) ? JavaStubElementTypes.ENUM_CONSTANT : JavaStubElementTypes.FIELD);

    if (initializer != null && initializer.length() > INITIALIZER_LENGTH_LIMIT) {
      myInitializer = INITIALIZER_TOO_LONG;
    }
    else {
      myInitializer = initializer;
    }

    myName = name;
    myType = type;
    myFlags = flags;
  }

  @NotNull
  public TypeInfo getType(boolean doResolve) {
    if (!doResolve) return myType;

    return addApplicableTypeAnnotationsFromChildModifierList(this, myType);
  }

  public static TypeInfo addApplicableTypeAnnotationsFromChildModifierList(StubBase<?> aThis, TypeInfo type) {
    PsiModifierListStub modifierList = (PsiModifierListStub)aThis.findChildStubByType(JavaStubElementTypes.MODIFIER_LIST);
    if (modifierList == null) return type;
    TypeInfo typeInfo = new TypeInfo(type);
    for (StubElement child: modifierList.getChildrenStubs()){
      if (!(child instanceof PsiAnnotationStub)) continue;
      PsiAnnotationStub annotationStub = (PsiAnnotationStub)child;
      PsiAnnotationImpl annotation = (PsiAnnotationImpl)annotationStub.getTreeElement().getPsi();
      if (AnnotationsHighlightUtil.isAnnotationApplicableTo(annotation, true, "TYPE_USE")) {
        typeInfo.addAnnotation(annotationStub);
      }
    }
    return typeInfo;
  }

  public String getInitializerText() throws InitializerTooLongException {
    if (INITIALIZER_TOO_LONG.equals(myInitializer)) throw new InitializerTooLongException();
    return StringRef.toString(myInitializer);
  }

  public byte getFlags() {
    return myFlags;
  }

  public boolean isEnumConstant() {
    return isEnumConst(myFlags);
  }

  private static boolean isEnumConst(final byte flags) {
    return (flags & ENUM_CONST) != 0;
  }

  public boolean isDeprecated() {
    return (myFlags & DEPRECATED) != 0;
  }

  public boolean hasDeprecatedAnnotation() {
    return (myFlags & DEPRECATED_ANNOTATION) != 0;
  }

  public String getName() {
    return StringRef.toString(myName);
  }

  public static byte packFlags(boolean isEnumConst, boolean isDeprecated, boolean hasDeprecatedAnnotation) {
    byte flags = 0;
    if (isEnumConst) flags |= ENUM_CONST;
    if (isDeprecated) flags |= DEPRECATED;
    if (hasDeprecatedAnnotation) flags |= DEPRECATED_ANNOTATION;
    return flags;
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PsiFieldStub[");

    if (isDeprecated() || hasDeprecatedAnnotation()) {
      builder.append("deprecated ");
    }

    if (isEnumConstant()) {
      builder.append("enumconst ");
    }

    builder.append(getName()).append(':').append(TypeInfo.createTypeText(getType(true)));

    if (myInitializer != null) {
      builder.append('=').append(myInitializer);
    }

    builder.append("]");
    return builder.toString();
  }
}