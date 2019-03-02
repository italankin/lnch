package com.italankin.lintchecks.detectors

import com.android.SdkConstants
import com.android.resources.ResourceFolderType
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.findContainingUClass
import org.jetbrains.uast.getOutermostQualified
import org.w3c.dom.Attr
import org.w3c.dom.Element
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PrefsDetector : Detector(), SourceCodeScanner, XmlScanner {

    companion object {
        val ISSUE = Issue.create(
                "PrefsDetector",
                "",
                "",
                Category.LINT,
                10,
                Severity.FATAL,
                Implementation(PrefsDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE, Scope.RESOURCE_FILE),
                        Scope.RESOURCE_FILE_SCOPE))

        private const val FIRST_PHASE = 1
        private const val SECOND_PHASE = 2
        private const val PREFS = "com.italankin.lnch.model.repository.prefs.Prefs"
        private const val METHOD_CREATE = "create"
        private const val ATTR_DEFAULT_VALUE = "defaultValue"
        private const val ATTR_KEY = "key"
        private val BOXED_PRIMITIVE_TYPES = setOf(
                String::class.java.name,
                Byte::class.java.name,
                Double::class.java.name,
                Integer::class.java.name,
                Float::class.java.name,
                Boolean::class.java.name,
                Character::class.java.name,
                Short::class.java.name,
                Long::class.java.name
        )
    }

    override fun beforeCheckEachProject(context: Context) {
        if (context.phase == FIRST_PHASE) {
            prefs.clear()
        }
    }

    override fun afterCheckEachProject(context: Context) {
        if (context.phase == FIRST_PHASE) {
            context.driver.requestRepeat(this, Scope.RESOURCE_FILE_SCOPE)
        }
    }

    private val prefs: MutableMap<String, Pref> = ConcurrentHashMap()

    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType == ResourceFolderType.XML
    }

    override fun getApplicableElements(): Collection<String> {
        return listOf("Preference", "ListPreference", "CheckBoxPreference", "MultiSelectListPreference")
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        if (context.phase != FIRST_PHASE) {
            return null
        }
        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                if (node.methodName != METHOD_CREATE) {
                    return
                }
                val containingClass = node.resolve()?.findContainingUClass() ?: return
                if (containingClass.qualifiedName != PREFS) {
                    return
                }
                val argName = node.valueArguments.getOrNull(0) ?: return
                val argDefaultValue = node.valueArguments.getOrNull(1) ?: return
                val name = ConstantEvaluator.evaluateString(context, argName, false) ?: return
                val defaultValue = ConstantEvaluator.evaluate(context, argDefaultValue)
                val prefType = (node.returnType as PsiClassType).parameters.getOrNull(0) ?: return
                val location = getFieldNameLocation(context, node)
                if (prefType.isBoxedPrimitive()) {
                    prefs[name] = Pref(defaultValue ?: Pref.Value.NULL, location)
                } else {
                    // TODO analyze enums
                    prefs[name] = Pref(Pref.Value.UNKNOWN, location)
                }
            }
        }
    }

    private fun getFieldNameLocation(context: JavaContext, node: UCallExpression): Location? {
        val parent = node.getOutermostQualified()?.uastParent ?: return null
        return context.getNameLocation(parent)
    }

    override fun visitElement(context: XmlContext, element: Element) {
        if (context.phase != SECOND_PHASE) {
            return
        }
        val nodeKey = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, ATTR_KEY) ?: return
        val key = nodeKey.value
        if (key.startsWith(SdkConstants.STRING_PREFIX)) {
            return
        }
        if (!prefs.containsKey(key)) {
            context.report(ISSUE, context.getValueLocation(element.getAttributeNode(ATTR_KEY)),
                    "Unknown key")
            return
        }
        val pref = prefs[key] as Pref
        if (pref.defaultValue == Pref.Value.UNKNOWN) {
            return
        }
        val nodeDefaultValue: Attr? = element.getAttributeNodeNS(SdkConstants.ANDROID_URI, ATTR_DEFAULT_VALUE)
        if (nodeDefaultValue == null) {
            val location = context.getNameLocation(nodeKey)
            if (pref.location != null) {
                location.withSecondary(pref.location, "Defined here", true)
            }
            context.report(ISSUE, location, "Must have value `${pref.defaultValue}`")
            return
        }
        if (nodeDefaultValue.value != pref.defaultValue) {
            val location = context.getValueLocation(nodeDefaultValue)
            if (pref.location != null) {
                location.withSecondary(pref.location, "Defined here", true)
            }
            val fix = fix()
                    .set(SdkConstants.ANDROID_URI, ATTR_DEFAULT_VALUE, "${pref.defaultValue}")
                    .build()
            context.report(ISSUE, location, "defaultValue should be `${pref.defaultValue}`", fix)
        }
        if(pref is EnumPref) {
            // TODO
        }
    }

    private fun PsiType.isBoxedPrimitive(): Boolean {
        return PsiTypesUtil.getPsiClass(this)?.qualifiedName in BOXED_PRIMITIVE_TYPES
    }

    private open class Pref(
            val defaultValue: Any?,
            val location: Location? = null
    ) {
        enum class Value {
            NULL() {
                override fun toString() = "null"
            },
            UNKNOWN
        }
    }

    private class EnumPref(
            defaultValue: Any?,
            location: Location?,
            val values: Set<Any>
    ) : Pref(defaultValue, location)
}