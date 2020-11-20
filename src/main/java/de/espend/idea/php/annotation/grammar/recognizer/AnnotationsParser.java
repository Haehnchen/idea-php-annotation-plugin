// Generated from /home/siedler/Slackday/idea-php-annotation-plugin/src/main/java/de/espend/idea/php/annotation/grammar/Annotations.g4 by ANTLR 4.8
package de.espend.idea.php.annotation.grammar.recognizer;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class AnnotationsParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, ANNOTATION_TITLE=4, AT_CHARACTER=5, OPENING_BRACKET=6, 
		CLOSING_BRACKET=7, OPENING_CURLY_BRACKET=8, CLOSING_CURLY_BRACKET=9, STRING=10, 
		INTEGER=11, ANNOTATION_NAME=12, WS=13;
	public static final int
		RULE_start = 0, RULE_base = 1, RULE_annotation_content = 2, RULE_content_list = 3, 
		RULE_content = 4, RULE_value = 5, RULE_array = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "base", "annotation_content", "content_list", "content", "value", 
			"array"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','", "'='", "':'", null, "'@'", "'('", "')'", "'{'", "'}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "ANNOTATION_TITLE", "AT_CHARACTER", "OPENING_BRACKET", 
			"CLOSING_BRACKET", "OPENING_CURLY_BRACKET", "CLOSING_CURLY_BRACKET", 
			"STRING", "INTEGER", "ANNOTATION_NAME", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Annotations.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public AnnotationsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StartContext extends ParserRuleContext {
		public BaseContext base() {
			return getRuleContext(BaseContext.class,0);
		}
		public TerminalNode EOF() { return getToken(AnnotationsParser.EOF, 0); }
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).enterStart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).exitStart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AnnotationsVisitor ) return ((AnnotationsVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			base();
			setState(15);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BaseContext extends ParserRuleContext {
		public TerminalNode ANNOTATION_TITLE() { return getToken(AnnotationsParser.ANNOTATION_TITLE, 0); }
		public Annotation_contentContext annotation_content() {
			return getRuleContext(Annotation_contentContext.class,0);
		}
		public BaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_base; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).enterBase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).exitBase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AnnotationsVisitor ) return ((AnnotationsVisitor<? extends T>)visitor).visitBase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BaseContext base() throws RecognitionException {
		BaseContext _localctx = new BaseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_base);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(17);
			match(ANNOTATION_TITLE);
			setState(20);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OPENING_BRACKET:
				{
				setState(18);
				annotation_content();
				}
				break;
			case EOF:
			case T__0:
			case CLOSING_BRACKET:
			case CLOSING_CURLY_BRACKET:
				{
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Annotation_contentContext extends ParserRuleContext {
		public TerminalNode OPENING_BRACKET() { return getToken(AnnotationsParser.OPENING_BRACKET, 0); }
		public TerminalNode CLOSING_BRACKET() { return getToken(AnnotationsParser.CLOSING_BRACKET, 0); }
		public Content_listContext content_list() {
			return getRuleContext(Content_listContext.class,0);
		}
		public Annotation_contentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation_content; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).enterAnnotation_content(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).exitAnnotation_content(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AnnotationsVisitor ) return ((AnnotationsVisitor<? extends T>)visitor).visitAnnotation_content(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Annotation_contentContext annotation_content() throws RecognitionException {
		Annotation_contentContext _localctx = new Annotation_contentContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_annotation_content);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22);
			match(OPENING_BRACKET);
			setState(25);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANNOTATION_TITLE:
			case OPENING_CURLY_BRACKET:
			case STRING:
			case INTEGER:
			case ANNOTATION_NAME:
				{
				setState(23);
				content_list();
				}
				break;
			case CLOSING_BRACKET:
				{
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(27);
			match(CLOSING_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Content_listContext extends ParserRuleContext {
		public ContentContext content() {
			return getRuleContext(ContentContext.class,0);
		}
		public Content_listContext content_list() {
			return getRuleContext(Content_listContext.class,0);
		}
		public Content_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_content_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).enterContent_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).exitContent_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AnnotationsVisitor ) return ((AnnotationsVisitor<? extends T>)visitor).visitContent_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Content_listContext content_list() throws RecognitionException {
		Content_listContext _localctx = new Content_listContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_content_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			content();
			setState(34);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(30);
				match(T__0);
				setState(31);
				content_list();
				}
				break;
			case 2:
				{
				setState(32);
				match(T__0);
				}
				break;
			case 3:
				{
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ContentContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public ArrayContext array() {
			return getRuleContext(ArrayContext.class,0);
		}
		public BaseContext base() {
			return getRuleContext(BaseContext.class,0);
		}
		public TerminalNode ANNOTATION_NAME() { return getToken(AnnotationsParser.ANNOTATION_NAME, 0); }
		public TerminalNode STRING() { return getToken(AnnotationsParser.STRING, 0); }
		public ContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_content; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).enterContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).exitContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AnnotationsVisitor ) return ((AnnotationsVisitor<? extends T>)visitor).visitContent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContentContext content() throws RecognitionException {
		ContentContext _localctx = new ContentContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_content);
		int _la;
		try {
			setState(47);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(39);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(36);
					_la = _input.LA(1);
					if ( !(_la==STRING || _la==ANNOTATION_NAME) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(37);
					_la = _input.LA(1);
					if ( !(_la==T__1 || _la==T__2) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					break;
				case 2:
					{
					}
					break;
				}
				setState(44);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case STRING:
				case INTEGER:
				case ANNOTATION_NAME:
					{
					setState(41);
					value();
					}
					break;
				case OPENING_CURLY_BRACKET:
					{
					setState(42);
					array();
					}
					break;
				case ANNOTATION_TITLE:
					{
					setState(43);
					base();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				base();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode ANNOTATION_NAME() { return getToken(AnnotationsParser.ANNOTATION_NAME, 0); }
		public TerminalNode STRING() { return getToken(AnnotationsParser.STRING, 0); }
		public TerminalNode INTEGER() { return getToken(AnnotationsParser.INTEGER, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AnnotationsVisitor ) return ((AnnotationsVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << INTEGER) | (1L << ANNOTATION_NAME))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayContext extends ParserRuleContext {
		public TerminalNode OPENING_CURLY_BRACKET() { return getToken(AnnotationsParser.OPENING_CURLY_BRACKET, 0); }
		public TerminalNode CLOSING_CURLY_BRACKET() { return getToken(AnnotationsParser.CLOSING_CURLY_BRACKET, 0); }
		public Content_listContext content_list() {
			return getRuleContext(Content_listContext.class,0);
		}
		public ArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).enterArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AnnotationsListener ) ((AnnotationsListener)listener).exitArray(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AnnotationsVisitor ) return ((AnnotationsVisitor<? extends T>)visitor).visitArray(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayContext array() throws RecognitionException {
		ArrayContext _localctx = new ArrayContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_array);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			match(OPENING_CURLY_BRACKET);
			setState(54);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANNOTATION_TITLE:
			case OPENING_CURLY_BRACKET:
			case STRING:
			case INTEGER:
			case ANNOTATION_NAME:
				{
				setState(52);
				content_list();
				}
				break;
			case CLOSING_CURLY_BRACKET:
				{
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(56);
			match(CLOSING_CURLY_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17=\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\2\3\3\3\3\3\3\5\3"+
		"\27\n\3\3\4\3\4\3\4\5\4\34\n\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\5\5%\n\5\3"+
		"\6\3\6\3\6\5\6*\n\6\3\6\3\6\3\6\5\6/\n\6\3\6\5\6\62\n\6\3\7\3\7\3\b\3"+
		"\b\3\b\5\b9\n\b\3\b\3\b\3\b\2\2\t\2\4\6\b\n\f\16\2\5\4\2\f\f\16\16\3\2"+
		"\4\5\3\2\f\16\2>\2\20\3\2\2\2\4\23\3\2\2\2\6\30\3\2\2\2\b\37\3\2\2\2\n"+
		"\61\3\2\2\2\f\63\3\2\2\2\16\65\3\2\2\2\20\21\5\4\3\2\21\22\7\2\2\3\22"+
		"\3\3\2\2\2\23\26\7\6\2\2\24\27\5\6\4\2\25\27\3\2\2\2\26\24\3\2\2\2\26"+
		"\25\3\2\2\2\27\5\3\2\2\2\30\33\7\b\2\2\31\34\5\b\5\2\32\34\3\2\2\2\33"+
		"\31\3\2\2\2\33\32\3\2\2\2\34\35\3\2\2\2\35\36\7\t\2\2\36\7\3\2\2\2\37"+
		"$\5\n\6\2 !\7\3\2\2!%\5\b\5\2\"%\7\3\2\2#%\3\2\2\2$ \3\2\2\2$\"\3\2\2"+
		"\2$#\3\2\2\2%\t\3\2\2\2&\'\t\2\2\2\'*\t\3\2\2(*\3\2\2\2)&\3\2\2\2)(\3"+
		"\2\2\2*.\3\2\2\2+/\5\f\7\2,/\5\16\b\2-/\5\4\3\2.+\3\2\2\2.,\3\2\2\2.-"+
		"\3\2\2\2/\62\3\2\2\2\60\62\5\4\3\2\61)\3\2\2\2\61\60\3\2\2\2\62\13\3\2"+
		"\2\2\63\64\t\4\2\2\64\r\3\2\2\2\658\7\n\2\2\669\5\b\5\2\679\3\2\2\28\66"+
		"\3\2\2\28\67\3\2\2\29:\3\2\2\2:;\7\13\2\2;\17\3\2\2\2\t\26\33$).\618";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}