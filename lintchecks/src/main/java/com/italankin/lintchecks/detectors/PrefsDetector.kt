package com.italankin.lintchecks.detectors

import com.android.SdkConstants
import com.android.resources.ResourceFolderType
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClassType
import com.italankin.lintchecks.util.getAndroidAttrNode
import com.italankin.lintchecks.util.isBoxedPrimitive
import com.italankin.lintchecks.util.isEnum
import org.jetbrains.uast.*
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
    }

    private val prefs: MutableMap<String, Pref> = ConcurrentHashMap()

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
                val defaultValue = node.valueArguments.getOrNull(1) ?: return
                val name = node.valueArguments.getOrNull(0)?.evaluateString() ?: return
                val location = getFieldNameLocation(context, node)
                val prefType = (node.returnType as PsiClassType).parameters.getOrNull(0) ?: return
                if (prefType.isBoxedPrimitive() || isString(prefType)) {
                    prefs[name] = Pref(defaultValue.evaluate(), location)
                    return
                }
                prefs[name] = Pref(Pref.Value.UNKNOWN, location)
                when {
                    prefType.isEnum() -> {
                        val literalValue = defaultValue.tryResolve()
                                .toUElement(UEnumConstant::class.java)
                                ?.resolveEnumKey()
                                ?: return
                        prefs[name] = Pref(literalValue, location)
                    }
                }
            }
        }
    }

    private fun UEnumConstant?.resolveEnumKey(): String? {
        if (this == null) {
            return null
        }
        val firstArgument = valueArguments.getOrNull(0)!!
        return firstArgument.evaluateString()
    }

    private fun getFieldNameLocation(context: JavaContext, node: UCallExpression): Location? {
        val parent = node.getOutermostQualified()?.uastParent ?: return null
        return context.getNameLocation(parent)
    }

    ///////////////////////////////////////////////////////////////////////////
    // XML
    ///////////////////////////////////////////////////////////////////////////

    override fun getApplicableElements(): Collection<String> {
        return listOf(
                "Preference",
                "ListPreference",
                "CheckBoxPreference",
                "MultiSelectListPreference",
                "SwitchPreference"
        )
    }

    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType == ResourceFolderType.XML
    }

    override fun visitElement(context: XmlContext, element: Element) {
        if (context.phase != SECOND_PHASE) {
            return
        }
        val nodeKey = element.getAndroidAttrNode(ATTR_KEY) ?: return
        val key = nodeKey.value
        if (key.startsWith(SdkConstants.STRING_PREFIX)) {
            return
        }
        if (!prefs.containsKey(key)) {
            context.report(ISSUE, context.getValueLocation(nodeKey), "Unknown preference key")
            return
        }
        val pref = prefs[key]!!
        if (pref.defaultValue == Pref.Value.UNKNOWN) {
            return
        }
        val nodeDefaultValue: Attr? = element.getAndroidAttrNode(ATTR_DEFAULT_VALUE)
        if (nodeDefaultValue == null) {
            val location = context.getNameLocation(nodeKey).addDefinition(pref)
            context.report(ISSUE, location, "Must have defaultValue `${pref.defaultValue}`")
            return
        }
        if (!checkValueEquals(nodeDefaultValue.value, pref.defaultValue)) {
            val location = context.getValueLocation(nodeDefaultValue).addDefinition(pref)
            context.report(ISSUE, location, "Must be `${pref.defaultValue}`")
        }
    }

    private fun checkValueEquals(value: String, expected: Any?): Boolean {
        val any: Any = when (expected) {
            is String -> value
            is Boolean -> value.toBoolean()
            is Float -> value.toFloat()
            is Long -> value.toLong()
            is Byte -> value.toByte()
            is Double -> value.toDouble()
            is Int -> value.toInt()
            is Char -> value.toInt()
            is Short -> value.toShort()
            null -> return value == SdkConstants.NULL_RESOURCE
            else -> return false
        }
        return expected == any
    }

    private fun Location.addDefinition(pref: Pref): Location {
        if (pref.declaration != null) {
            withSecondary(pref.declaration, "defined here", false)
        }
        return this
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data
    ///////////////////////////////////////////////////////////////////////////

    private data class Pref(
            val defaultValue: Any?,
            val declaration: Location? = null
    ) {
        enum class Value {
            NULL() {
                override fun toString() = "null"
            },
            UNKNOWN
        }
    }
}