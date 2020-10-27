// Generated from at/ac/tuwien/kr/alpha/antlr/ASPCore2.g4 by ANTLR 4.7
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
public class ASPCore2Lexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, ANONYMOUS_VARIABLE=2, DOT=3, COMMA=4, QUERY_MARK=5, COLON=6, SEMICOLON=7, 
		OR=8, NAF=9, CONS=10, WCONS=11, PLUS=12, MINUS=13, TIMES=14, DIV=15, POWER=16, 
		MODULO=17, BITXOR=18, AT=19, SHARP=20, AMPERSAND=21, QUOTE=22, PAREN_OPEN=23, 
		PAREN_CLOSE=24, SQUARE_OPEN=25, SQUARE_CLOSE=26, CURLY_OPEN=27, CURLY_CLOSE=28, 
		EQUAL=29, UNEQUAL=30, LESS=31, GREATER=32, LESS_OR_EQ=33, GREATER_OR_EQ=34, 
		AGGREGATE_COUNT=35, AGGREGATE_MAX=36, AGGREGATE_MIN=37, AGGREGATE_SUM=38, 
		ID=39, VARIABLE=40, NUMBER=41, QUOTED_STRING=42, COMMENT=43, MULTI_LINE_COMMEN=44, 
		BLANK=45;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "ANONYMOUS_VARIABLE", "DOT", "COMMA", "QUERY_MARK", "COLON", "SEMICOLON", 
		"OR", "NAF", "CONS", "WCONS", "PLUS", "MINUS", "TIMES", "DIV", "POWER", 
		"MODULO", "BITXOR", "AT", "SHARP", "AMPERSAND", "QUOTE", "PAREN_OPEN", 
		"PAREN_CLOSE", "SQUARE_OPEN", "SQUARE_CLOSE", "CURLY_OPEN", "CURLY_CLOSE", 
		"EQUAL", "UNEQUAL", "LESS", "GREATER", "LESS_OR_EQ", "GREATER_OR_EQ", 
		"AGGREGATE_COUNT", "AGGREGATE_MAX", "AGGREGATE_MIN", "AGGREGATE_SUM", 
		"ID", "VARIABLE", "NUMBER", "QUOTED_STRING", "COMMENT", "MULTI_LINE_COMMEN", 
		"BLANK"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'enumeration_predicate_is'", "'_'", "'.'", "','", "'?'", "':'", 
		"';'", "'|'", "'not'", "':-'", "':~'", "'+'", "'-'", "'*'", "'/'", "'**'", 
		"'\\'", "'^'", "'@'", "'#'", "'&'", "'\"'", "'('", "')'", "'['", "']'", 
		"'{'", "'}'", "'='", null, "'<'", "'>'", "'<='", "'>='", "'#count'", "'#max'", 
		"'#min'", "'#sum'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "ANONYMOUS_VARIABLE", "DOT", "COMMA", "QUERY_MARK", "COLON", 
		"SEMICOLON", "OR", "NAF", "CONS", "WCONS", "PLUS", "MINUS", "TIMES", "DIV", 
		"POWER", "MODULO", "BITXOR", "AT", "SHARP", "AMPERSAND", "QUOTE", "PAREN_OPEN", 
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


	public ASPCore2Lexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ASPCore2.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2/\u011a\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3"+
		"\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5"+
		"\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3"+
		"\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\21\3\22\3\22"+
		"\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31"+
		"\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3\37"+
		"\5\37\u00b8\n\37\3 \3 \3!\3!\3\"\3\"\3\"\3#\3#\3#\3$\3$\3$\3$\3$\3$\3"+
		"$\3%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3\'\3\'\3\'\3\'\3\'\3(\3(\7(\u00dc\n("+
		"\f(\16(\u00df\13(\3)\3)\7)\u00e3\n)\f)\16)\u00e6\13)\3*\3*\3*\7*\u00eb"+
		"\n*\f*\16*\u00ee\13*\5*\u00f0\n*\3+\3+\3+\3+\7+\u00f6\n+\f+\16+\u00f9"+
		"\13+\3+\3+\3,\3,\7,\u00ff\n,\f,\16,\u0102\13,\3,\3,\3-\3-\3-\3-\7-\u010a"+
		"\n-\f-\16-\u010d\13-\3-\3-\3-\3-\3-\3.\6.\u0115\n.\r.\16.\u0116\3.\3."+
		"\4\u00f7\u010b\2/\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65"+
		"\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/\3\2\5\6\2\62;C\\a"+
		"ac|\4\2\f\f\17\17\5\2\13\f\16\17\"\"\2\u0123\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2"+
		"\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2"+
		"\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2"+
		"\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2"+
		"M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3"+
		"\2\2\2\2[\3\2\2\2\3]\3\2\2\2\5v\3\2\2\2\7x\3\2\2\2\tz\3\2\2\2\13|\3\2"+
		"\2\2\r~\3\2\2\2\17\u0080\3\2\2\2\21\u0082\3\2\2\2\23\u0084\3\2\2\2\25"+
		"\u0088\3\2\2\2\27\u008b\3\2\2\2\31\u008e\3\2\2\2\33\u0090\3\2\2\2\35\u0092"+
		"\3\2\2\2\37\u0094\3\2\2\2!\u0096\3\2\2\2#\u0099\3\2\2\2%\u009b\3\2\2\2"+
		"\'\u009d\3\2\2\2)\u009f\3\2\2\2+\u00a1\3\2\2\2-\u00a3\3\2\2\2/\u00a5\3"+
		"\2\2\2\61\u00a7\3\2\2\2\63\u00a9\3\2\2\2\65\u00ab\3\2\2\2\67\u00ad\3\2"+
		"\2\29\u00af\3\2\2\2;\u00b1\3\2\2\2=\u00b7\3\2\2\2?\u00b9\3\2\2\2A\u00bb"+
		"\3\2\2\2C\u00bd\3\2\2\2E\u00c0\3\2\2\2G\u00c3\3\2\2\2I\u00ca\3\2\2\2K"+
		"\u00cf\3\2\2\2M\u00d4\3\2\2\2O\u00d9\3\2\2\2Q\u00e0\3\2\2\2S\u00ef\3\2"+
		"\2\2U\u00f1\3\2\2\2W\u00fc\3\2\2\2Y\u0105\3\2\2\2[\u0114\3\2\2\2]^\7g"+
		"\2\2^_\7p\2\2_`\7w\2\2`a\7o\2\2ab\7g\2\2bc\7t\2\2cd\7c\2\2de\7v\2\2ef"+
		"\7k\2\2fg\7q\2\2gh\7p\2\2hi\7a\2\2ij\7r\2\2jk\7t\2\2kl\7g\2\2lm\7f\2\2"+
		"mn\7k\2\2no\7e\2\2op\7c\2\2pq\7v\2\2qr\7g\2\2rs\7a\2\2st\7k\2\2tu\7u\2"+
		"\2u\4\3\2\2\2vw\7a\2\2w\6\3\2\2\2xy\7\60\2\2y\b\3\2\2\2z{\7.\2\2{\n\3"+
		"\2\2\2|}\7A\2\2}\f\3\2\2\2~\177\7<\2\2\177\16\3\2\2\2\u0080\u0081\7=\2"+
		"\2\u0081\20\3\2\2\2\u0082\u0083\7~\2\2\u0083\22\3\2\2\2\u0084\u0085\7"+
		"p\2\2\u0085\u0086\7q\2\2\u0086\u0087\7v\2\2\u0087\24\3\2\2\2\u0088\u0089"+
		"\7<\2\2\u0089\u008a\7/\2\2\u008a\26\3\2\2\2\u008b\u008c\7<\2\2\u008c\u008d"+
		"\7\u0080\2\2\u008d\30\3\2\2\2\u008e\u008f\7-\2\2\u008f\32\3\2\2\2\u0090"+
		"\u0091\7/\2\2\u0091\34\3\2\2\2\u0092\u0093\7,\2\2\u0093\36\3\2\2\2\u0094"+
		"\u0095\7\61\2\2\u0095 \3\2\2\2\u0096\u0097\7,\2\2\u0097\u0098\7,\2\2\u0098"+
		"\"\3\2\2\2\u0099\u009a\7^\2\2\u009a$\3\2\2\2\u009b\u009c\7`\2\2\u009c"+
		"&\3\2\2\2\u009d\u009e\7B\2\2\u009e(\3\2\2\2\u009f\u00a0\7%\2\2\u00a0*"+
		"\3\2\2\2\u00a1\u00a2\7(\2\2\u00a2,\3\2\2\2\u00a3\u00a4\7$\2\2\u00a4.\3"+
		"\2\2\2\u00a5\u00a6\7*\2\2\u00a6\60\3\2\2\2\u00a7\u00a8\7+\2\2\u00a8\62"+
		"\3\2\2\2\u00a9\u00aa\7]\2\2\u00aa\64\3\2\2\2\u00ab\u00ac\7_\2\2\u00ac"+
		"\66\3\2\2\2\u00ad\u00ae\7}\2\2\u00ae8\3\2\2\2\u00af\u00b0\7\177\2\2\u00b0"+
		":\3\2\2\2\u00b1\u00b2\7?\2\2\u00b2<\3\2\2\2\u00b3\u00b4\7>\2\2\u00b4\u00b8"+
		"\7@\2\2\u00b5\u00b6\7#\2\2\u00b6\u00b8\7?\2\2\u00b7\u00b3\3\2\2\2\u00b7"+
		"\u00b5\3\2\2\2\u00b8>\3\2\2\2\u00b9\u00ba\7>\2\2\u00ba@\3\2\2\2\u00bb"+
		"\u00bc\7@\2\2\u00bcB\3\2\2\2\u00bd\u00be\7>\2\2\u00be\u00bf\7?\2\2\u00bf"+
		"D\3\2\2\2\u00c0\u00c1\7@\2\2\u00c1\u00c2\7?\2\2\u00c2F\3\2\2\2\u00c3\u00c4"+
		"\7%\2\2\u00c4\u00c5\7e\2\2\u00c5\u00c6\7q\2\2\u00c6\u00c7\7w\2\2\u00c7"+
		"\u00c8\7p\2\2\u00c8\u00c9\7v\2\2\u00c9H\3\2\2\2\u00ca\u00cb\7%\2\2\u00cb"+
		"\u00cc\7o\2\2\u00cc\u00cd\7c\2\2\u00cd\u00ce\7z\2\2\u00ceJ\3\2\2\2\u00cf"+
		"\u00d0\7%\2\2\u00d0\u00d1\7o\2\2\u00d1\u00d2\7k\2\2\u00d2\u00d3\7p\2\2"+
		"\u00d3L\3\2\2\2\u00d4\u00d5\7%\2\2\u00d5\u00d6\7u\2\2\u00d6\u00d7\7w\2"+
		"\2\u00d7\u00d8\7o\2\2\u00d8N\3\2\2\2\u00d9\u00dd\4c|\2\u00da\u00dc\t\2"+
		"\2\2\u00db\u00da\3\2\2\2\u00dc\u00df\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd"+
		"\u00de\3\2\2\2\u00deP\3\2\2\2\u00df\u00dd\3\2\2\2\u00e0\u00e4\4C\\\2\u00e1"+
		"\u00e3\t\2\2\2\u00e2\u00e1\3\2\2\2\u00e3\u00e6\3\2\2\2\u00e4\u00e2\3\2"+
		"\2\2\u00e4\u00e5\3\2\2\2\u00e5R\3\2\2\2\u00e6\u00e4\3\2\2\2\u00e7\u00f0"+
		"\7\62\2\2\u00e8\u00ec\4\63;\2\u00e9\u00eb\4\62;\2\u00ea\u00e9\3\2\2\2"+
		"\u00eb\u00ee\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\u00f0"+
		"\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ef\u00e7\3\2\2\2\u00ef\u00e8\3\2\2\2\u00f0"+
		"T\3\2\2\2\u00f1\u00f7\5-\27\2\u00f2\u00f3\7^\2\2\u00f3\u00f6\7$\2\2\u00f4"+
		"\u00f6\13\2\2\2\u00f5\u00f2\3\2\2\2\u00f5\u00f4\3\2\2\2\u00f6\u00f9\3"+
		"\2\2\2\u00f7\u00f8\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f8\u00fa\3\2\2\2\u00f9"+
		"\u00f7\3\2\2\2\u00fa\u00fb\5-\27\2\u00fbV\3\2\2\2\u00fc\u0100\7\'\2\2"+
		"\u00fd\u00ff\n\3\2\2\u00fe\u00fd\3\2\2\2\u00ff\u0102\3\2\2\2\u0100\u00fe"+
		"\3\2\2\2\u0100\u0101\3\2\2\2\u0101\u0103\3\2\2\2\u0102\u0100\3\2\2\2\u0103"+
		"\u0104\b,\2\2\u0104X\3\2\2\2\u0105\u0106\7\'\2\2\u0106\u0107\7,\2\2\u0107"+
		"\u010b\3\2\2\2\u0108\u010a\13\2\2\2\u0109\u0108\3\2\2\2\u010a\u010d\3"+
		"\2\2\2\u010b\u010c\3\2\2\2\u010b\u0109\3\2\2\2\u010c\u010e\3\2\2\2\u010d"+
		"\u010b\3\2\2\2\u010e\u010f\7,\2\2\u010f\u0110\7\'\2\2\u0110\u0111\3\2"+
		"\2\2\u0111\u0112\b-\2\2\u0112Z\3\2\2\2\u0113\u0115\t\4\2\2\u0114\u0113"+
		"\3\2\2\2\u0115\u0116\3\2\2\2\u0116\u0114\3\2\2\2\u0116\u0117\3\2\2\2\u0117"+
		"\u0118\3\2\2\2\u0118\u0119\b.\2\2\u0119\\\3\2\2\2\r\2\u00b7\u00dd\u00e4"+
		"\u00ec\u00ef\u00f5\u00f7\u0100\u010b\u0116\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}