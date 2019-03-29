package com.italankin.lintchecks.detectors

import com.android.SdkConstants
import com.android.tools.lint.client.api.TYPE_INT
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.italankin.lintchecks.CATEGORY_LNCH
import org.jetbrains.uast.*

class ActivityResultDetector : Detector(), SourceCodeScanner {

    companion object {
        val ISSUE = Issue.create(
                "ActivityResultDetector",
                "Result code comparing with request code",
                "Request code in onActivityResult should not be compared with result codes",
                CATEGORY_LNCH,
                5,
                Severity.ERROR,
                Implementation(ActivityResultDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
        private val TARGET_CLASSES = listOf(
                SdkConstants.CLASS_ACTIVITY,
                "androidx.fragment.app.Fragment",
                SdkConstants.CLASS_FRAGMENT
        )
    }

    override fun getApplicableReferenceNames(): List<String>? {
        return listOf("RESULT_OK", "RESULT_CANCELED", "RESULT_FIRST_USER")
    }

    override fun visitReference(context: JavaContext, reference: UReferenceExpression, referenced: PsiElement) {
        if (referenced !is PsiField) {
            return
        }
        if (!context.evaluator.isMemberInClass(referenced, SdkConstants.CLASS_ACTIVITY)) {
            return
        }
        // check if 'referenced' is used in onActivityResult method
        val method: UMethod = reference.getParentOfType(UMethod::class.java) ?: return
        if (!isTarget(context, method)) {
            return
        }
        // find expression, e.g. (requestCode == Activity.RESULT_OK)
        val comparison: UBinaryExpression = reference.getParentOfType(UBinaryExpression::class.java)
                ?: return
        // get another argument of comparison expression
        val anotherOperand = if (comparison.leftOperand.getQualifiedParentOrThis() == reference.getQualifiedParentOrThis()) {
            comparison.rightOperand
        } else {
            comparison.leftOperand
        }
        // operand should be reference to a parameter
        if (anotherOperand !is UReferenceExpression) {
            return
        }
        val paramRequestCode = method.uastParameters[0]
        if (anotherOperand.resolveToUElement() == paramRequestCode) {
            val paramResultCode = method.uastParameters[1]
            val fix = fix().replace()
                    .text(paramRequestCode.name)
                    .with(paramResultCode.name)
                    .build()
            context.report(ISSUE, context.getLocation(comparison),
                    "`${paramRequestCode.name}` should not be compared with `${referenced.name}`",
                    fix)
        }
    }

    /**
     * Check if method is one of onActivityResult methods declared in [TARGET_CLASSES]
     */
    private fun isTarget(context: JavaContext, method: UMethod): Boolean {
        for (targetClass in TARGET_CLASSES) {
            if (context.evaluator.methodMatches(method, targetClass, true,
                            TYPE_INT, TYPE_INT, SdkConstants.CLASS_INTENT)) {
                return true
            }
        }
        return false
    }
}