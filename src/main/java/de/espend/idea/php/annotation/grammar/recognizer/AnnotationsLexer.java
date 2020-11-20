// Generated from /home/siedler/Slackday/idea-php-annotation-plugin/src/main/java/de/espend/idea/php/annotation/grammar/Annotations.g4 by ANTLR 4.8
package de.espend.idea.php.annotation.grammar.recognizer;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class AnnotationsLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, ANNOTATION_TITLE=4, AT_CHARACTER=5, OPENING_BRACKET=6, 
		CLOSING_BRACKET=7, OPENING_CURLY_BRACKET=8, CLOSING_CURLY_BRACKET=9, STRING=10, 
		INTEGER=11, ANNOTATION_NAME=12, WS=13;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "ANNOTATION_TITLE", "AT_CHARACTER", "OPENING_BRACKET", 
			"CLOSING_BRACKET", "OPENING_CURLY_BRACKET", "CLOSING_CURLY_BRACKET", 
			"STRING", "INTEGER", "ANNOTATION_NAME", "WS"
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


	public AnnotationsLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Annotations.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\17T\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6"+
		"\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\7\13\63\n\13\f\13\16\13"+
		"\66\13\13\3\13\3\13\3\f\6\f;\n\f\r\f\16\f<\3\f\3\f\7\fA\n\f\f\f\16\fD"+
		"\13\f\3\f\5\fG\n\f\3\r\6\rJ\n\r\r\r\16\rK\3\16\6\16O\n\16\r\16\16\16P"+
		"\3\16\3\16\2\2\17\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\16\33\17\3\2\6\3\2$$\3\2\62;\n\2\f\f\"\"$$*+..??}}\177\177\6\2\13\f\17"+
		"\17\"\",,\2Y\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2"+
		"\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\3\35\3\2\2\2\5\37\3\2\2\2\7!\3\2"+
		"\2\2\t#\3\2\2\2\13&\3\2\2\2\r(\3\2\2\2\17*\3\2\2\2\21,\3\2\2\2\23.\3\2"+
		"\2\2\25\60\3\2\2\2\27:\3\2\2\2\31I\3\2\2\2\33N\3\2\2\2\35\36\7.\2\2\36"+
		"\4\3\2\2\2\37 \7?\2\2 \6\3\2\2\2!\"\7<\2\2\"\b\3\2\2\2#$\5\13\6\2$%\5"+
		"\31\r\2%\n\3\2\2\2&\'\7B\2\2\'\f\3\2\2\2()\7*\2\2)\16\3\2\2\2*+\7+\2\2"+
		"+\20\3\2\2\2,-\7}\2\2-\22\3\2\2\2./\7\177\2\2/\24\3\2\2\2\60\64\7$\2\2"+
		"\61\63\n\2\2\2\62\61\3\2\2\2\63\66\3\2\2\2\64\62\3\2\2\2\64\65\3\2\2\2"+
		"\65\67\3\2\2\2\66\64\3\2\2\2\678\7$\2\28\26\3\2\2\29;\t\3\2\2:9\3\2\2"+
		"\2;<\3\2\2\2<:\3\2\2\2<=\3\2\2\2=F\3\2\2\2>B\7\60\2\2?A\t\3\2\2@?\3\2"+
		"\2\2AD\3\2\2\2B@\3\2\2\2BC\3\2\2\2CG\3\2\2\2DB\3\2\2\2EG\3\2\2\2F>\3\2"+
		"\2\2FE\3\2\2\2G\30\3\2\2\2HJ\n\4\2\2IH\3\2\2\2JK\3\2\2\2KI\3\2\2\2KL\3"+
		"\2\2\2L\32\3\2\2\2MO\t\5\2\2NM\3\2\2\2OP\3\2\2\2PN\3\2\2\2PQ\3\2\2\2Q"+
		"R\3\2\2\2RS\b\16\2\2S\34\3\2\2\2\t\2\64<BFKP\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}