package de.espend.idea.php.annotation.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.ResolveResult
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.documentation.phpdoc.lexer.PhpDocTokenTypes
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocToken
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag
import com.jetbrains.php.lang.psi.PhpPsiUtil
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.jetbrains.php.lang.psi.elements.PhpNamespace
import com.jetbrains.php.lang.psi.elements.PhpUse
import de.espend.idea.php.annotation.util.AnnotationUtil
import de.espend.idea.php.annotation.util.PhpDocUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class DocTagNameAnnotationReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        /*
         * Our main reference provider to attach DocBlocTag to their use declaration
         * This one resolve the "Optimize Usage" issues
         *
         * "@Template()", "@ORM\PostPersist()"
         */
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PhpDocTag::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    if (element !is PhpDocTag) {
                        return emptyArray()
                    }
                    return arrayOf(PhpDocTagReference(element))
                }
            },
        )

        /*
         * Collects static identifier elements on the first element and search them inside the use statements or global namespace
         *
         * - @Foo(F<caret>OO::BAR)
         * - @Foo(Fo<caret>o\FOO::BAR)
         */
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PhpDocToken::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    if (element.node.elementType !== PhpDocTokenTypes.DOC_IDENTIFIER) {
                        return emptyArray()
                    }

                    val prevSibling = element.prevSibling
                    if (
                        prevSibling == null ||
                        prevSibling.node.elementType === PhpDocTokenTypes.DOC_NAMESPACE ||
                        PhpDocUtil.isDocStaticElement(prevSibling)
                    ) {
                        return emptyArray()
                    }

                    // We must be at first namespace part: "F<caret>oo\Bar::class"
                    PhpDocUtil.getNamespaceForDocIdentifierAtStart(element) ?: return emptyArray()
                    val docTag: PhpDocTag? =
                        PhpPsiUtil.getParentByCondition(element, true, PhpDocTag.INSTANCEOF, null)
                    docTag ?: return emptyArray()
                    // Find any import which is related here: "use Foo" => "F<caret>oo\Bar::class"
                    val fqn = AnnotationUtil.getClassFromDocIdentifierAsString(element) ?: return emptyArray()

                    return arrayOf(PhpDocIdentifierReference(element, fqn))
                }
            },
        )
    }

    private class PhpDocTagReference(element: PhpDocTag) : PsiPolyVariantReferenceBase<PhpDocTag>(element) {
        override fun isReferenceTo(element: PsiElement): Boolean {
            // eg for "Optimize Imports"
            // attach reference to @Template()
            // reference can also point to a namespace e.g. @Annotation\Exclude()

            // use Doctrine\ORM\Mapping as "ORM";
            if (element is PhpUse) {
                if (element.name == getDocBlockName()) {
                    return true
                }
            }

            // class "Zend\Form\Annotation\Exclude" imported via namespace and has "subclass" annotation
            // use Zend\Form\Annotation;
            // @Annotation\Exclude
            if (element is PhpNamespace) {
                val phpClass = AnnotationUtil.getAnnotationReference(this.element)
                return phpClass != null && phpClass.fqn.startsWith(element.fqn)
            }

            // direct class match
            // Zend\Form\Annotation => @Annotation
            if (element is PhpClass) {
                val phpClass = AnnotationUtil.getAnnotationReference(this.element)
                return phpClass != null && phpClass.fqn == element.fqn
            }

            return false
        }

        /**
         * We need to strip @ char before DocTag @Test, @Test\Foo
         *
         * @return TextRange of DocTag without @ char
         */
        override fun getRangeInElement(): TextRange {
            var tagName = element.name
            var rangeStart = 0
            var rangeEnd = tagName.length

            // remove DocTag "@" char
            // it should always be true, check for security reason
            if (tagName.startsWith("@")) {
                rangeStart = 1
                tagName = tagName.substring(1)
            }

            // "@ORM\PostPersist()"
            // only on alias and namespace use main ns
            if ("\\" in tagName) {
                rangeEnd = tagName.indexOf("\\") + rangeStart
            }

            return TextRange(rangeStart, rangeEnd)
        }

        override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
            val phpClass = AnnotationUtil.getAnnotationReference(element) ?: return emptyArray()
            return arrayOf(PsiElementResolveResult(phpClass))
        }

        override fun getVariants(): Array<Any> = emptyArray()

        /**
         * Get the class alias
         *
         * "@Template()"
         * "@ORM\PostPersist()"
         */
        private fun getDocBlockName(): String {
            var name = element.name
            if (name.startsWith("@")) {
                name = name.substring(1)
            }
            name = name.substringBefore("\\")
            return name
        }
    }

    /**
     * Adds support for references of "@Foobar(name=Fo<caret>oBar::Const)"
     */
    private class PhpDocIdentifierReference(
        element: PsiElement,
        private val fqn: String,
    ) : PsiReferenceBase<PsiElement>(element) {
        override fun getRangeInElement(): TextRange = TextRange.create(0, element.textLength)

        override fun resolve(): PsiElement? = null

        /**
         * Attach element identify name to class of "use" usage
         *
         * @param psiElement PhpClass used in "use" statement
         */
        override fun isReferenceTo(psiElement: PsiElement): Boolean {
            if (psiElement !is PhpNamedElement) {
                return false
            }

            val text = element.text
            return StringUtils.isNotBlank(text) &&
                text in AnnotationUtil.getUseImportMap(element) &&
                StringUtils.stripStart(psiElement.fqn, "\\").equals(
                    StringUtils.stripStart(fqn, "\\"),
                    ignoreCase = true,
                )
        }

        override fun getVariants(): Array<Any> = emptyArray()
    }
}
