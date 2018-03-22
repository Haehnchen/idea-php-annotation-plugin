package de.espend.idea.php.annotation.toolbox;

import de.espend.idea.php.toolbox.completion.dict.ToolboxJsonFileCompletionArguments;
import de.espend.idea.php.toolbox.extension.ToolboxJsonFileCompletion;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class AnnotationToolboxJsonFileCompletion implements ToolboxJsonFileCompletion {
    @Override
    public void addCompletions(@NotNull ToolboxJsonFileCompletionArguments arguments) {
        if(arguments.getType().equals(ToolboxJsonFileCompletionArguments.TYPE.SIGNATURE_TYPE)) {
            arguments.addLookupString("annotation");
            arguments.addLookupString("annotation_array");
        }
    }
}
