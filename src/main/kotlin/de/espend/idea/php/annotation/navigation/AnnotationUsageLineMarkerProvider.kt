package de.espend.idea.php.annotation.navigation

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.PhpIcons
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.espend.idea.php.annotation.AnnotationUsageIndex
import de.espend.idea.php.annotation.util.AnnotationUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
class AnnotationUsageLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(
        psiElements: MutableList<out PsiElement>,
        results: MutableCollection<in LineMarkerInfo<*>?>
    ) {
        for (psiElement in psiElements) {
            if (!CLASS_NAME_PATTERN.accepts(psiElement)) {
                continue
            }

            val phpClass = psiElement.context as? PhpClass ?: continue

            val isAnnotationOrAttribute = AnnotationUtil.isAnnotationClass(phpClass)
                    || phpClass.getAttributes("\\Attribute").isNotEmpty()

            if (!isAnnotationOrAttribute) {
                continue
            }

            val fqn = StringUtils.stripStart(phpClass.fqn, "\\")

            // find one index annotation class and stop processing on first match
            val processed = booleanArrayOf(false)
            FileBasedIndex.getInstance().getFilesWithKey(
                AnnotationUsageIndex.KEY,
                hashSetOf(fqn),
                Processor { _: VirtualFile ->
                    processed[0] = true

                    // stop on first match
                    false
                },
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                    GlobalSearchScope.allScope(psiElement.project),
                    PhpFileType.INSTANCE
                )
            )

            // we found at least one target to provide lazy target linemarker
            if (processed[0]) {
                val builder = NavigationGutterIconBuilder.create(PhpIcons.IMPLEMENTS)
                    .setTargets(NotNullLazyValue.lazy {
                        AnnotationUtil.getImplementationsForAnnotation(
                            psiElement.project,
                            fqn,
                        )
                    })
                    .setTooltipText("Navigate to implementations")

                results.add(builder.createLineMarkerInfo(psiElement))
            }
        }
    }

}

/**
 * class "Foo" extends
 */
private val CLASS_NAME_PATTERN = PlatformPatterns
    .psiElement(PhpTokenTypes.IDENTIFIER)
    .afterLeafSkipping(
        PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
        PlatformPatterns.psiElement(PhpTokenTypes.kwCLASS)
    )
    .withParent(PhpClass::class.java)
    .withLanguage(PhpLanguage.INSTANCE)
