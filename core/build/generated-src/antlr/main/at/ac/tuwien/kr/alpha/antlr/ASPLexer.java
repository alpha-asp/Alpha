// Generated from at/ac/tuwien/kr/alpha/antlr/ASPLexer.g4 by ANTLR 4.7
package at.ac.tuwien.kr.alpha.antlr;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ASPLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ANONYMOUS_VARIABLE=1, DOT=2, COMMA=3, QUERY_MARK=4, COLON=5, SEMICOLON=6, 
		OR=7, NAF=8, CONS=9, WCONS=10, PLUS=11, MINUS=12, TIMES=13, DIV=14, POWER=15, 
		MODULO=16, BITXOR=17, AT=18, SHARP=19, AMPERSAND=20, QUOTE=21, PAREN_OPEN=22, 
		PAREN_CLOSE=23, SQUARE_OPEN=24, SQUARE_CLOSE=25, CURLY_OPEN=26, CURLY_CLOSE=27, 
		EQUAL=28, UNEQUAL=29, LESS=30, GREATER=31, LESS_OR_EQ=32, GREATER_OR_EQ=33, 
		AGGREGATE_COUNT=34, AGGREGATE_MAX=35, AGGREGATE_MIN=36, AGGREGATE_SUM=37, 
		ID=38, VARIABLE=39, NUMBER=40, QUOTED_STRING=41, COMMENT=42, MULTI_LINE_COMMEN=43, 
		BLANK=44;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"ANONYMOUS_VARIABLE", "DOT", "COMMA", "QUERY_MARK", "COLON", "SEMICOLON", 
		"OR", "NAF", "CONS", "WCONS", "PLUS", "MINUS", "TIMES", "DIV", "POWER", 
		"MODULO", "BITXOR", "AT", "SHARP", "AMPERSAND", "QUOTE", "PAREN_OPEN", 
		"PAREN_CLOSE", "SQUARE_OPEN", "SQUARE_CLOSE", "CURLY_OPEN", "CURLY_CLOSE", 
		"EQUAL", "UNEQUAL", "LESS", "GREATER", "LESS_OR_EQ", "GREATER_OR_EQ", 
		"AGGREGATE_COUNT", "AGGREGATE_MAX", "AGGREGATE_MIN", "AGGREGATE_SUM", 
		"ID", "VARIABLE", "NUMBER", "QUOTED_STRING", "COMMENT", "MULTI_LINE_COMMEN", 
		"BLANK"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'_'", "'.'", "','", "'?'", "':'", "';'", "'|'", "'not'", "':-'", 
		"':~'", "'+'", "'-'", "'*'", "'/'", "'**'", "'\\'", "'^'", "'@'", "'#'", 
		"'&'", "'\"'", "'('", "')'", "'['", "']'", "'{'", "'}'", "'='", null, 
		"'<'", "'>'", "'<='", "'>='", "'#count'", "'#max'", "'#min'", "'#sum'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "ANONYMOUS_VARIABLE", "DOT", "COMMA", "QUERY_MARK", "COLON", "SEMICOLON", 
		"OR", "NAF", "CONS", "WCONS", "PLUS", "MINUS", "TIMES", "DIV", "POWER", 
		"MODULO", "BITXOR", "AT", "SHARP", "AMPERSAND", "QUOTE", "PAREN_OPEN", 
		"PAREN_CLOSE", "SQUARE_OPEN", "SQUARE_CLOSE", "CURLY_OPEN", "CURLY_CLOSE", 
		"EQUAL", "UNEQUAL", "LESS", "GREATER", "LESS_OR_EQ", "GREATER_OR_EQ", 
		"AGGREGATE_COUNT", "AGGREGATE_MAX", "AGGREGATE_MIN", "AGGREGATE_SUM", 
		"ID", "VARIABLE", "NUMBER", "QUOTED_STRING", "COMMENT", "MULTI_LINE_COMMEN", 
		"BLANK"
	};
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


	public ASPLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ASPLexer.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2.\u00ff\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t"+
		"\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17"+
		"\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25"+
		"\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34"+
		"\3\35\3\35\3\36\3\36\3\36\3\36\5\36\u009d\n\36\3\37\3\37\3 \3 \3!\3!\3"+
		"!\3\"\3\"\3\"\3#\3#\3#\3#\3#\3#\3#\3$\3$\3$\3$\3$\3%\3%\3%\3%\3%\3&\3"+
		"&\3&\3&\3&\3\'\3\'\7\'\u00c1\n\'\f\'\16\'\u00c4\13\'\3(\3(\7(\u00c8\n"+
		"(\f(\16(\u00cb\13(\3)\3)\3)\7)\u00d0\n)\f)\16)\u00d3\13)\5)\u00d5\n)\3"+
		"*\3*\3*\3*\7*\u00db\n*\f*\16*\u00de\13*\3*\3*\3+\3+\7+\u00e4\n+\f+\16"+
		"+\u00e7\13+\3+\3+\3,\3,\3,\3,\7,\u00ef\n,\f,\16,\u00f2\13,\3,\3,\3,\3"+
		",\3,\3-\6-\u00fa\n-\r-\16-\u00fb\3-\3-\4\u00dc\u00f0\2.\3\3\5\4\7\5\t"+
		"\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23"+
		"%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G"+
		"%I&K\'M(O)Q*S+U,W-Y.\3\2\5\6\2\62;C\\aac|\4\2\f\f\17\17\5\2\13\f\16\17"+
		"\"\"\2\u0108\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2"+
		"\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2"+
		"\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2"+
		"\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3"+
		"\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2"+
		"\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2"+
		"S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\3[\3\2\2\2\5]\3\2\2\2\7_\3"+
		"\2\2\2\ta\3\2\2\2\13c\3\2\2\2\re\3\2\2\2\17g\3\2\2\2\21i\3\2\2\2\23m\3"+
		"\2\2\2\25p\3\2\2\2\27s\3\2\2\2\31u\3\2\2\2\33w\3\2\2\2\35y\3\2\2\2\37"+
		"{\3\2\2\2!~\3\2\2\2#\u0080\3\2\2\2%\u0082\3\2\2\2\'\u0084\3\2\2\2)\u0086"+
		"\3\2\2\2+\u0088\3\2\2\2-\u008a\3\2\2\2/\u008c\3\2\2\2\61\u008e\3\2\2\2"+
		"\63\u0090\3\2\2\2\65\u0092\3\2\2\2\67\u0094\3\2\2\29\u0096\3\2\2\2;\u009c"+
		"\3\2\2\2=\u009e\3\2\2\2?\u00a0\3\2\2\2A\u00a2\3\2\2\2C\u00a5\3\2\2\2E"+
		"\u00a8\3\2\2\2G\u00af\3\2\2\2I\u00b4\3\2\2\2K\u00b9\3\2\2\2M\u00be\3\2"+
		"\2\2O\u00c5\3\2\2\2Q\u00d4\3\2\2\2S\u00d6\3\2\2\2U\u00e1\3\2\2\2W\u00ea"+
		"\3\2\2\2Y\u00f9\3\2\2\2[\\\7a\2\2\\\4\3\2\2\2]^\7\60\2\2^\6\3\2\2\2_`"+
		"\7.\2\2`\b\3\2\2\2ab\7A\2\2b\n\3\2\2\2cd\7<\2\2d\f\3\2\2\2ef\7=\2\2f\16"+
		"\3\2\2\2gh\7~\2\2h\20\3\2\2\2ij\7p\2\2jk\7q\2\2kl\7v\2\2l\22\3\2\2\2m"+
		"n\7<\2\2no\7/\2\2o\24\3\2\2\2pq\7<\2\2qr\7\u0080\2\2r\26\3\2\2\2st\7-"+
		"\2\2t\30\3\2\2\2uv\7/\2\2v\32\3\2\2\2wx\7,\2\2x\34\3\2\2\2yz\7\61\2\2"+
		"z\36\3\2\2\2{|\7,\2\2|}\7,\2\2} \3\2\2\2~\177\7^\2\2\177\"\3\2\2\2\u0080"+
		"\u0081\7`\2\2\u0081$\3\2\2\2\u0082\u0083\7B\2\2\u0083&\3\2\2\2\u0084\u0085"+
		"\7%\2\2\u0085(\3\2\2\2\u0086\u0087\7(\2\2\u0087*\3\2\2\2\u0088\u0089\7"+
		"$\2\2\u0089,\3\2\2\2\u008a\u008b\7*\2\2\u008b.\3\2\2\2\u008c\u008d\7+"+
		"\2\2\u008d\60\3\2\2\2\u008e\u008f\7]\2\2\u008f\62\3\2\2\2\u0090\u0091"+
		"\7_\2\2\u0091\64\3\2\2\2\u0092\u0093\7}\2\2\u0093\66\3\2\2\2\u0094\u0095"+
		"\7\177\2\2\u00958\3\2\2\2\u0096\u0097\7?\2\2\u0097:\3\2\2\2\u0098\u0099"+
		"\7>\2\2\u0099\u009d\7@\2\2\u009a\u009b\7#\2\2\u009b\u009d\7?\2\2\u009c"+
		"\u0098\3\2\2\2\u009c\u009a\3\2\2\2\u009d<\3\2\2\2\u009e\u009f\7>\2\2\u009f"+
		">\3\2\2\2\u00a0\u00a1\7@\2\2\u00a1@\3\2\2\2\u00a2\u00a3\7>\2\2\u00a3\u00a4"+
		"\7?\2\2\u00a4B\3\2\2\2\u00a5\u00a6\7@\2\2\u00a6\u00a7\7?\2\2\u00a7D\3"+
		"\2\2\2\u00a8\u00a9\7%\2\2\u00a9\u00aa\7e\2\2\u00aa\u00ab\7q\2\2\u00ab"+
		"\u00ac\7w\2\2\u00ac\u00ad\7p\2\2\u00ad\u00ae\7v\2\2\u00aeF\3\2\2\2\u00af"+
		"\u00b0\7%\2\2\u00b0\u00b1\7o\2\2\u00b1\u00b2\7c\2\2\u00b2\u00b3\7z\2\2"+
		"\u00b3H\3\2\2\2\u00b4\u00b5\7%\2\2\u00b5\u00b6\7o\2\2\u00b6\u00b7\7k\2"+
		"\2\u00b7\u00b8\7p\2\2\u00b8J\3\2\2\2\u00b9\u00ba\7%\2\2\u00ba\u00bb\7"+
		"u\2\2\u00bb\u00bc\7w\2\2\u00bc\u00bd\7o\2\2\u00bdL\3\2\2\2\u00be\u00c2"+
		"\4c|\2\u00bf\u00c1\t\2\2\2\u00c0\u00bf\3\2\2\2\u00c1\u00c4\3\2\2\2\u00c2"+
		"\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3N\3\2\2\2\u00c4\u00c2\3\2\2\2"+
		"\u00c5\u00c9\4C\\\2\u00c6\u00c8\t\2\2\2\u00c7\u00c6\3\2\2\2\u00c8\u00cb"+
		"\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9\u00ca\3\2\2\2\u00caP\3\2\2\2\u00cb"+
		"\u00c9\3\2\2\2\u00cc\u00d5\7\62\2\2\u00cd\u00d1\4\63;\2\u00ce\u00d0\4"+
		"\62;\2\u00cf\u00ce\3\2\2\2\u00d0\u00d3\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d1"+
		"\u00d2\3\2\2\2\u00d2\u00d5\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d4\u00cc\3\2"+
		"\2\2\u00d4\u00cd\3\2\2\2\u00d5R\3\2\2\2\u00d6\u00dc\5+\26\2\u00d7\u00d8"+
		"\7^\2\2\u00d8\u00db\7$\2\2\u00d9\u00db\13\2\2\2\u00da\u00d7\3\2\2\2\u00da"+
		"\u00d9\3\2\2\2\u00db\u00de\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dc\u00da\3\2"+
		"\2\2\u00dd\u00df\3\2\2\2\u00de\u00dc\3\2\2\2\u00df\u00e0\5+\26\2\u00e0"+
		"T\3\2\2\2\u00e1\u00e5\7\'\2\2\u00e2\u00e4\n\3\2\2\u00e3\u00e2\3\2\2\2"+
		"\u00e4\u00e7\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e8"+
		"\3\2\2\2\u00e7\u00e5\3\2\2\2\u00e8\u00e9\b+\2\2\u00e9V\3\2\2\2\u00ea\u00eb"+
		"\7\'\2\2\u00eb\u00ec\7,\2\2\u00ec\u00f0\3\2\2\2\u00ed\u00ef\13\2\2\2\u00ee"+
		"\u00ed\3\2\2\2\u00ef\u00f2\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f0\u00ee\3\2"+
		"\2\2\u00f1\u00f3\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f3\u00f4\7,\2\2\u00f4"+
		"\u00f5\7\'\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f7\b,\2\2\u00f7X\3\2\2\2\u00f8"+
		"\u00fa\t\4\2\2\u00f9\u00f8\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00f9\3\2"+
		"\2\2\u00fb\u00fc\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00fe\b-\2\2\u00fe"+
		"Z\3\2\2\2\r\2\u009c\u00c2\u00c9\u00d1\u00d4\u00da\u00dc\u00e5\u00f0\u00fb"+
		"\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}