package com.italankin.lintchecks.util

import com.android.SdkConstants
import com.android.tools.lint.detector.api.getPrimitiveType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import org.w3c.dom.Attr
import org.w3c.dom.Element

fun PsiType.psiClass(): PsiClass? {
    return PsiTypesUtil.getPsiClass(this)
}

fun PsiType.isBoxedPrimitive(): Boolean {
    val qn = psiClass()?.qualifiedName ?: return false
    return getPrimitiveType(qn) != null
}

fun Element.getAndroidAttrNode(attr: String): Attr? {
    return getAttributeNodeNS(SdkConstants.ANDROID_URI, attr)
}

fun PsiType.isEnum() : Boolean {
    return psiClass()?.isEnum == true
}