// Generated from /home/siedler/Slackday/idea-php-annotation-plugin/src/main/java/de/espend/idea/php/annotation/grammar/Annotations.g4 by ANTLR 4.8
package de.espend.idea.php.annotation.grammar.recognizer;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link AnnotationsParser}.
 */
public interface AnnotationsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link AnnotationsParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(AnnotationsParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link AnnotationsParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(AnnotationsParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link AnnotationsParser#base}.
	 * @param ctx the parse tree
	 */
	void enterBase(AnnotationsParser.BaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link AnnotationsParser#base}.
	 * @param ctx the parse tree
	 */
	void exitBase(AnnotationsParser.BaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link AnnotationsParser#annotation_content}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation_content(AnnotationsParser.Annotation_contentContext ctx);
	/**
	 * Exit a parse tree produced by {@link AnnotationsParser#annotation_content}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation_content(AnnotationsParser.Annotation_contentContext ctx);
	/**
	 * Enter a parse tree produced by {@link AnnotationsParser#content_list}.
	 * @param ctx the parse tree
	 */
	void enterContent_list(AnnotationsParser.Content_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link AnnotationsParser#content_list}.
	 * @param ctx the parse tree
	 */
	void exitContent_list(AnnotationsParser.Content_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link AnnotationsParser#content}.
	 * @param ctx the parse tree
	 */
	void enterContent(AnnotationsParser.ContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link AnnotationsParser#content}.
	 * @param ctx the parse tree
	 */
	void exitContent(AnnotationsParser.ContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link AnnotationsParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(AnnotationsParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link AnnotationsParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(AnnotationsParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link AnnotationsParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(AnnotationsParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link AnnotationsParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(AnnotationsParser.ArrayContext ctx);
}