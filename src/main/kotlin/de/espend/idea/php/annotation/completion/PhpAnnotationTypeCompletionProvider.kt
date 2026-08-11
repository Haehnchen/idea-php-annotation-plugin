package de.espend.idea.php.annotation.completion

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.jetbrains.php.lang.psi.elements.Field
import de.espend.idea.php.annotation.dict.AnnotationPropertyEnum
import de.espend.idea.php.annotation.extension.PhpAnnotationCompletionProvider
import de.espend.idea.php.annotation.extension.parameter.AnnotationCompletionProviderParameter
import de.espend.idea.php.annotation.extension.parameter.AnnotationPropertyParameter
import de.espend.idea.php.annotation.util.AnnotationUtil
import java.util.regex.Pattern

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class PhpAnnotationTypeCompletionProvider : PhpAnnotationCompletionProvider {
    override fun getPropertyValueCompletions(
        annotationPropertyParameter: AnnotationPropertyParameter,
        completionParameter: AnnotationCompletionProviderParameter
    ) {
        val propertyName = annotationPropertyParameter.propertyName
        if (annotationPropertyParameter.type != AnnotationPropertyParameter.Type.PROPERTY_VALUE || propertyName == null) {
            return
        }

        val values = HashSet<String>()
        AnnotationUtil.visitAttributes(
            annotationPropertyParameter.phpClass,
        ) { attributeName, type, target ->
            if (attributeName == propertyName) {
                if (AnnotationPropertyEnum.fromString(type) == AnnotationPropertyEnum.BOOLEAN) {
                    values.addAll(listOf("false", "true"))
                }

                // @Enum({"AUTO", "SEQUENCE"})
                if (target is Field) {
                    val docComment = target.docComment
                    if (docComment != null) {
                        val phpDocTags = docComment.getTagElementsByName("@Enum")
                        for (phpDocTag in phpDocTags) {
                            val phpDocAttrList = phpDocTag.firstPsiChild
                            if (phpDocAttrList != null) {
                                val enumArrayString = phpDocAttrList.text
                                val targetPattern = Pattern.compile("\"(\\w+)\"")

                                val matcher = targetPattern.matcher(enumArrayString)
                                while (matcher.find()) {
                                    values.add(matcher.group(1))
                                }
                            }
                        }
                    }
                }
            }
            null
        }

        for (s in values) {
            completionParameter.result.addElement(LookupElementBuilder.create(s))
        }
    }
}
