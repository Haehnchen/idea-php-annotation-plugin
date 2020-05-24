package de.espend.idea.php.annotation.inspection.visitor;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import de.espend.idea.php.annotation.inspection.AnnotationMissingUseInspection;
import de.espend.idea.php.annotation.util.AnnotationUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

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
            PhpDocUtil.processTagElementsByName((PhpDocComment) element, null, phpDocTag -> {
                String name = phpDocTag.getName();
                if (StringUtils.isNotBlank(name) && !AnnotationUtil.NON_ANNOTATION_TAGS.contains(name)) {
                    phpDocTags.add(phpDocTag);
                }

                return true;
            });

            // our scope are the full DocComment; so collect the imports for them but lazy only if we need them
            AnnotationMissingUseInspection.MyLazyUserImporterCollector lazy = null;
            for (PhpDocTag phpDocTag : phpDocTags) {
                if (lazy == null) {
                    lazy = new AnnotationMissingUseInspection.MyLazyUserImporterCollector((PhpDocComment) element);
                }

                this.visitor.visitElement(phpDocTag, holder, lazy);
            }
        }

        super.visitElement(element);
    }

    public static interface DocWithUsePsiPsiElementVisitor {
        public void visitElement(@NotNull PhpDocTag phpDocTag, @NotNull ProblemsHolder holder, @NotNull Function<Void, Map<String, String>> lazyUseImporterCollector);
    }
}