package com.italankin.lintchecks.detectors

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.JavaPsiFacade
import com.italankin.lintchecks.util.psiClass
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

class JsonModelDetector : Detector(), SourceCodeScanner {

    companion object {
        val ISSUE = Issue.create(
                "JsonModelTypeProperty",
                "JsonModel.type is missing",
                "JsonModel implementors must have a 'type' field",
                Category.LINT,
                10,
                Severity.FATAL,
                Implementation(JsonModelDetector::class.java, Scope.JAVA_FILE_SCOPE))

        private const val JSON_MODEL = "com.italankin.lnch.model.repository.store.json.model.JsonModel"
        private const val FIELD_TYPE = "type"
        private const val FIELD_PROPERTY_TYPE = "PROPERTY_TYPE"
        private const val SERIALIZED_NAME = "com.google.gson.annotations.SerializedName"
        private const val KEEP = "androidx.annotation.Keep"
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UClass::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            private val psiFacade: JavaPsiFacade = JavaPsiFacade.getInstance(context.psiFile?.project)

            override fun visitClass(node: UClass) {
                val resolveScope = context.psiFile?.resolveScope ?: return
                val jsonModelClass = psiFacade.findClass(JSON_MODEL, resolveScope) ?: return

                // check if class implements JsonModel
                if (!node.isInheritor(jsonModelClass, true)) {
                    return
                }

                // find 'type' field
                val typeField = node.allFields.find { it.name == FIELD_TYPE }
                if (typeField == null) {
                    context.report(ISSUE, node, context.getNameLocation(node),
                            "${node.name} must declare `$FIELD_TYPE` field")
                    return
                }

                // find SerializedName annotation
                val serializedName = typeField.annotations.find { it.qualifiedName == SERIALIZED_NAME }
                if (serializedName == null) {
                    context.report(ISSUE, node, context.getNameLocation(typeField),
                            "`$FIELD_TYPE` must be annotated with `@SerializedName`")
                    return
                }

                // find SerializedName value node
                val serializedNameValue = serializedName.parameterList.attributes.find {
                    it.name == "value" || it.name == null
                }
                // check if value is PROPERTY_TYPE
                if (serializedNameValue?.value?.text != FIELD_PROPERTY_TYPE) {
                    val fix = fix()
                            .replace()
                            .text(serializedNameValue?.value?.text)
                            .with(FIELD_PROPERTY_TYPE)
                            .build()
                    context.report(ISSUE, node, context.getLocation(serializedName),
                            "`@SerializedName` must have a value of `$FIELD_PROPERTY_TYPE`",
                            fix)
                }

                // check field type
                val stringClass = psiFacade.findClass("java.lang.String", resolveScope) ?: return
                if (typeField.type.psiClass() != stringClass) {
                    val fix = fix()
                            .replace()
                            .text(typeField.type.presentableText)
                            .with("String")
                            .build()
                    context.report(ISSUE, node, context.getLocation(typeField.typeElement!!),
                            "`$FIELD_TYPE` must be a `String`", fix)
                }
                if (!typeField.hasAnnotation(KEEP)) {
                    context.report(ISSUE, node, context.getLocation(typeField),
                            "Must be annotated with `@Keep`")
                }

                // find constructor with no arguments
                val constructor = node.constructors.find { !it.hasParameters() }
                if (constructor == null) {
                    context.report(ISSUE, node, context.getNameLocation(node),
                            "${node.name} must have an empty constructor")
                    return
                }

                // check @Keep presence
                if (!constructor.hasAnnotation(KEEP)) {
                    context.report(ISSUE, node, context.getNameLocation(constructor),
                            "Must be annotated with `@Keep`")
                }

                // constructor must be public
                if (!constructor.hasModifier(JvmModifier.PUBLIC)) {
                    context.report(ISSUE, node, context.getNameLocation(constructor),
                            "Must be public")
                }
            }
        }
    }
}
