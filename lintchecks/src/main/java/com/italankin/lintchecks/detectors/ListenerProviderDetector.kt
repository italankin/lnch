package com.italankin.lintchecks.detectors

import com.android.tools.lint.detector.api.*
import com.italankin.lintchecks.CATEGORY_LNCH
import org.jetbrains.uast.UClass
import org.jetbrains.uast.ULambdaExpression

class ListenerProviderDetector : Detector(), SourceCodeScanner {

    companion object {
        val ISSUE = Issue.create(
                "ListenerProviderDetector",
                "ListenerProvider must be static classes",
                "ListenerProvider must be static classes",
                CATEGORY_LNCH,
                10,
                Severity.FATAL,
                Implementation(ListenerProviderDetector::class.java, Scope.JAVA_FILE_SCOPE))
    }

    override fun applicableSuperClasses(): List<String>? {
        return listOf(
                "com.italankin.lnch.util.dialogfragment.ListenerFragment",
                "com.italankin.lnch.util.dialogfragment.ListenerActivity"
        )
    }

    override fun visitClass(context: JavaContext, declaration: UClass) {
        if (!declaration.isInterface && !context.evaluator.isStatic(declaration)) {
            context.report(ISSUE, context.getNameLocation(declaration), "Must be static")
        }
    }

    override fun visitClass(context: JavaContext, lambda: ULambdaExpression) {
        context.report(ISSUE, context.getNameLocation(lambda), "Must not be a lambda")
    }
}