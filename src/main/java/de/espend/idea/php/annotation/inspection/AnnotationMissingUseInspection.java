package de.espend.idea.php.annotation.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.parser.PhpDocElementTypes;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import de.espend.idea.php.annotation.inspection.visitor.PhpDocTagWithUsePsiElementVisitor;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Inspection DocTags and their imports
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationMissingUseInspection extends LocalInspectionTool {
    public static final String MESSAGE = "Missing import";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PhpDocTagWithUsePsiElementVisitor(holder, this::visitAnnotationDocTag);
    }

    private void visitAnnotationDocTag(@NotNull PhpDocTag phpDocTag, @NotNull ProblemsHolder holder, @NotNull Function<Void, Map<String, String>> lazyUseImporterCollector) {
        // Target for our inspection is DocTag name: @Foobar() => Foobar
        // This prevent highlighting the complete DocTag
        PsiElement firstChild = phpDocTag.getFirstChild();
        if (firstChild == null || firstChild.getNode().getElementType() != PhpDocElementTypes.DOC_TAG_NAME) {
            return;
        }

        String name = phpDocTag.getName();
        String tagName = StringUtils.stripStart(name, "@");

        // ignore "@\Foo" absolute FQN ones
        if (tagName.startsWith("\\")) {
            return;
        }

        String[] split = tagName.split("\\\\");

        Map<String, String> useImportMap = lazyUseImporterCollector.apply(null);
        if (useImportMap.containsKey(split[0])) {
            return;
        }

        PhpClass annotationReference = AnnotationUtil.getAnnotationReference(phpDocTag);
        if (annotationReference != null) {
            return;
        }

        Collection<PhpClass> phpClasses = AnnotationUtil.getPossibleImportClasses(phpDocTag);
        if (phpClasses.size() > 0) {
            Set<String> collect = phpClasses.stream()
                .map(PhpNamedElement::getFQN)
                .collect(Collectors.toSet());

            holder.registerProblem(
                firstChild,
                MESSAGE,
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                new ImportUseForAnnotationQuickFix(phpDocTag, collect)
            );

            return;
        }

        holder.registerProblem(
            firstChild,
            MESSAGE,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        );
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }

    public static class MyLazyUserImporterCollector implements Function<Void, Map<String, String>> {
        @NotNull
        private final PhpDocComment phpDocComment;

        private Map<String, String> imports = null;

        public MyLazyUserImporterCollector(@NotNull PhpDocComment phpDocComment) {
            this.phpDocComment = phpDocComment;
        }

        @Override
        public Map<String, String> apply(Void aVoid) {
            if (imports != null) {
                return this.imports;
            }

            return this.imports = AnnotationUtil.getUseImportMap(this.phpDocComment);
        }
    }
}
