package de.espend.idea.php.annotation.completion.parameter;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;


public class CompletionParameter {

    private CompletionParameters parameters;
    private ProcessingContext context;
    private CompletionResultSet result;

    public CompletionParameter(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
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
