package de.espend.idea.php.annotation.inspection.visitor;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import de.espend.idea.php.annotation.inspection.AnnotationInspectionUtil;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class PhpDocTagWithUsePsiElementVisitor extends PsiElementVisitor {
    @NotNull
    private final ProblemsHolder holder;

    @NotNull
    private final PhpDocTagWithUsePsiElementVisitor.DocWithUsePsiPsiElementVisitor visitor;

    public PhpDocTagWithUsePsiElementVisitor(@NotNull ProblemsHolder holder, @NotNull PhpDocTagWithUsePsiElementVisitor.DocWithUsePsiPsiElementVisitor visitor) {
        this.holder = holder;
        this.visitor = visitor;
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if(element instanceof PhpDocComment) {
            Collection<PhpDocTag> phpDocTags = new HashSet<>();

            // there os no "getAllTags", we can only search by name; so use same internal logic but without name
            PhpDocUtil.consumeTagElementsByName((PhpDocComment) element, null, phpDocTag -> {
                String name = phpDocTag.getName();
                if (StringUtils.isNotBlank(name) && !AnnotationUtil.isBlockedAnnotationTag(name)) {
                    phpDocTags.add(phpDocTag);
                }
            });

            // our scope are the full DocComment; so collect the imports for them but lazy only if we need them
            AnnotationInspectionUtil.LazyNamespaceImportResolver lazy = null;
            for (PhpDocTag phpDocTag : phpDocTags) {
                if (lazy == null) {
                    lazy = new AnnotationInspectionUtil.LazyNamespaceImportResolver(element);
                }

                this.visitor.visitElement(phpDocTag, holder, lazy);
            }
        }

        super.visitElement(element);
    }

    public interface DocWithUsePsiPsiElementVisitor {
        void visitElement(@NotNull PhpDocTag phpDocTag, @NotNull ProblemsHolder holder, @NotNull AnnotationInspectionUtil.LazyNamespaceImportResolver lazyNamespaceImportResolver);
    }
}