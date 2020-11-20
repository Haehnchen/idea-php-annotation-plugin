// Generated from /home/siedler/Slackday/idea-php-annotation-plugin/src/main/java/de/espend/idea/php/annotation/grammar/Annotations.g4 by ANTLR 4.8
package de.espend.idea.php.annotation.grammar.recognizer;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link AnnotationsParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface AnnotationsVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link AnnotationsParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(AnnotationsParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link AnnotationsParser#base}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBase(AnnotationsParser.BaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link AnnotationsParser#annotation_content}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation_content(AnnotationsParser.Annotation_contentContext ctx);
	/**
	 * Visit a parse tree produced by {@link AnnotationsParser#content_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContent_list(AnnotationsParser.Content_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link AnnotationsParser#content}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContent(AnnotationsParser.ContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link AnnotationsParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(AnnotationsParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link AnnotationsParser#array}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray(AnnotationsParser.ArrayContext ctx);
}