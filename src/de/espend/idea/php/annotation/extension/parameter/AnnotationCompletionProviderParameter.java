package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationCompletionProviderParameter {
    @NotNull
    final private CompletionParameters parameters;

    @NotNull
    final private ProcessingContext context;

    @NotNull
    final private CompletionResultSet result;

    public AnnotationCompletionProviderParameter(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        this.parameters = parameters;
        this.context = context;
        this.result = result;
    }

    @NotNull
    public CompletionParameters getParameters() {
        return parameters;
    }

    @NotNull
    public ProcessingContext getContext() {
        return context;
    }

    @NotNull
    public CompletionResultSet getResult() {
        return result;
    }
}
