package de.espend.idea.php.annotation.extension.parameter;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationCompletionProviderParameter {

    private CompletionParameters parameters;
    private ProcessingContext context;
    private CompletionResultSet result;

    public AnnotationCompletionProviderParameter(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
        this.parameters = parameters;
        this.context = context;
        this.result = result;
    }

    public CompletionParameters getParameters() {
        return parameters;
    }

    public ProcessingContext getContext() {
        return context;
    }

    public CompletionResultSet getResult() {
        return result;
    }

}
