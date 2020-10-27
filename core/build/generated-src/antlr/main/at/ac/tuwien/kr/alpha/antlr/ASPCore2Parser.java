// Generated from at/ac/tuwien/kr/alpha/antlr/ASPCore2.g4 by ANTLR 4.7
package at.ac.tuwien.kr.alpha.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ASPCore2Parser extends Parser {
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
	public static final int
		RULE_program = 0, RULE_statements = 1, RULE_query = 2, RULE_statement = 3, 
		RULE_head = 4, RULE_body = 5, RULE_disjunction = 6, RULE_choice = 7, RULE_choice_elements = 8, 
		RULE_choice_element = 9, RULE_aggregate = 10, RULE_aggregate_elements = 11, 
		RULE_aggregate_element = 12, RULE_aggregate_function = 13, RULE_weight_at_level = 14, 
		RULE_naf_literals = 15, RULE_naf_literal = 16, RULE_classical_literal = 17, 
		RULE_builtin_atom = 18, RULE_binop = 19, RULE_terms = 20, RULE_term = 21, 
		RULE_interval = 22, RULE_external_atom = 23, RULE_directive = 24, RULE_directive_enumeration = 25, 
		RULE_basic_terms = 26, RULE_basic_term = 27, RULE_ground_term = 28, RULE_variable_term = 29, 
		RULE_answer_set = 30, RULE_answer_sets = 31;
	public static final String[] ruleNames = {
		"program", "statements", "query", "statement", "head", "body", "disjunction", 
		"choice", "choice_elements", "choice_element", "aggregate", "aggregate_elements", 
		"aggregate_element", "aggregate_function", "weight_at_level", "naf_literals", 
		"naf_literal", "classical_literal", "builtin_atom", "binop", "terms", 
		"term", "interval", "external_atom", "directive", "directive_enumeration", 
		"basic_terms", "basic_term", "ground_term", "variable_term", "answer_set", 
		"answer_sets"
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

	@Override
	public String getGrammarFileName() { return "ASPCore2.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ASPCore2Parser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(ASPCore2Parser.EOF, 0); }
		public StatementsContext statements() {
			return getRuleContext(StatementsContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(64);
				statements();
				}
				break;
			}
			setState(68);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS || _la==ID) {
				{
				setState(67);
				query();
				}
			}

			setState(70);
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

	public static class StatementsContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statements; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statements);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(73); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(72);
					statement();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(75); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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

	public static class QueryContext extends ParserRuleContext {
		public Classical_literalContext classical_literal() {
			return getRuleContext(Classical_literalContext.class,0);
		}
		public TerminalNode QUERY_MARK() { return getToken(ASPCore2Parser.QUERY_MARK, 0); }
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_query);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			classical_literal();
			setState(78);
			match(QUERY_MARK);
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

	public static class StatementContext extends ParserRuleContext {
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	 
		public StatementContext() { }
		public void copyFrom(StatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Statement_factContext extends StatementContext {
		public HeadContext head() {
			return getRuleContext(HeadContext.class,0);
		}
		public TerminalNode DOT() { return getToken(ASPCore2Parser.DOT, 0); }
		public Statement_factContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitStatement_fact(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Statement_ruleContext extends StatementContext {
		public HeadContext head() {
			return getRuleContext(HeadContext.class,0);
		}
		public TerminalNode CONS() { return getToken(ASPCore2Parser.CONS, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public TerminalNode DOT() { return getToken(ASPCore2Parser.DOT, 0); }
		public Statement_ruleContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitStatement_rule(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Statement_weightConstraintContext extends StatementContext {
		public TerminalNode WCONS() { return getToken(ASPCore2Parser.WCONS, 0); }
		public TerminalNode DOT() { return getToken(ASPCore2Parser.DOT, 0); }
		public TerminalNode SQUARE_OPEN() { return getToken(ASPCore2Parser.SQUARE_OPEN, 0); }
		public Weight_at_levelContext weight_at_level() {
			return getRuleContext(Weight_at_levelContext.class,0);
		}
		public TerminalNode SQUARE_CLOSE() { return getToken(ASPCore2Parser.SQUARE_CLOSE, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public Statement_weightConstraintContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitStatement_weightConstraint(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Statement_constraintContext extends StatementContext {
		public TerminalNode CONS() { return getToken(ASPCore2Parser.CONS, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public TerminalNode DOT() { return getToken(ASPCore2Parser.DOT, 0); }
		public Statement_constraintContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitStatement_constraint(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Statement_directiveContext extends StatementContext {
		public DirectiveContext directive() {
			return getRuleContext(DirectiveContext.class,0);
		}
		public Statement_directiveContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitStatement_directive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_statement);
		int _la;
		try {
			setState(102);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new Statement_factContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(80);
				head();
				setState(81);
				match(DOT);
				}
				break;
			case 2:
				_localctx = new Statement_constraintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(83);
				match(CONS);
				setState(84);
				body();
				setState(85);
				match(DOT);
				}
				break;
			case 3:
				_localctx = new Statement_ruleContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(87);
				head();
				setState(88);
				match(CONS);
				setState(89);
				body();
				setState(90);
				match(DOT);
				}
				break;
			case 4:
				_localctx = new Statement_weightConstraintContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(92);
				match(WCONS);
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANONYMOUS_VARIABLE) | (1L << NAF) | (1L << MINUS) | (1L << AMPERSAND) | (1L << PAREN_OPEN) | (1L << AGGREGATE_COUNT) | (1L << AGGREGATE_MAX) | (1L << AGGREGATE_MIN) | (1L << AGGREGATE_SUM) | (1L << ID) | (1L << VARIABLE) | (1L << NUMBER) | (1L << QUOTED_STRING))) != 0)) {
					{
					setState(93);
					body();
					}
				}

				setState(96);
				match(DOT);
				setState(97);
				match(SQUARE_OPEN);
				setState(98);
				weight_at_level();
				setState(99);
				match(SQUARE_CLOSE);
				}
				break;
			case 5:
				_localctx = new Statement_directiveContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(101);
				directive();
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

	public static class HeadContext extends ParserRuleContext {
		public DisjunctionContext disjunction() {
			return getRuleContext(DisjunctionContext.class,0);
		}
		public ChoiceContext choice() {
			return getRuleContext(ChoiceContext.class,0);
		}
		public HeadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_head; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitHead(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HeadContext head() throws RecognitionException {
		HeadContext _localctx = new HeadContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_head);
		try {
			setState(106);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(104);
				disjunction();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(105);
				choice();
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

	public static class BodyContext extends ParserRuleContext {
		public Naf_literalContext naf_literal() {
			return getRuleContext(Naf_literalContext.class,0);
		}
		public AggregateContext aggregate() {
			return getRuleContext(AggregateContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(ASPCore2Parser.COMMA, 0); }
		public BodyContext body() {
			return getRuleContext(BodyContext.class,0);
		}
		public BodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_body; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BodyContext body() throws RecognitionException {
		BodyContext _localctx = new BodyContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_body);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(108);
				naf_literal();
				}
				break;
			case 2:
				{
				setState(109);
				aggregate();
				}
				break;
			}
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(112);
				match(COMMA);
				setState(113);
				body();
				}
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

	public static class DisjunctionContext extends ParserRuleContext {
		public Classical_literalContext classical_literal() {
			return getRuleContext(Classical_literalContext.class,0);
		}
		public TerminalNode OR() { return getToken(ASPCore2Parser.OR, 0); }
		public DisjunctionContext disjunction() {
			return getRuleContext(DisjunctionContext.class,0);
		}
		public DisjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunction; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitDisjunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DisjunctionContext disjunction() throws RecognitionException {
		DisjunctionContext _localctx = new DisjunctionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_disjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			classical_literal();
			setState(119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OR) {
				{
				setState(117);
				match(OR);
				setState(118);
				disjunction();
				}
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

	public static class ChoiceContext extends ParserRuleContext {
		public TermContext lt;
		public BinopContext lop;
		public BinopContext uop;
		public TermContext ut;
		public TerminalNode CURLY_OPEN() { return getToken(ASPCore2Parser.CURLY_OPEN, 0); }
		public TerminalNode CURLY_CLOSE() { return getToken(ASPCore2Parser.CURLY_CLOSE, 0); }
		public Choice_elementsContext choice_elements() {
			return getRuleContext(Choice_elementsContext.class,0);
		}
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<BinopContext> binop() {
			return getRuleContexts(BinopContext.class);
		}
		public BinopContext binop(int i) {
			return getRuleContext(BinopContext.class,i);
		}
		public ChoiceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_choice; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitChoice(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ChoiceContext choice() throws RecognitionException {
		ChoiceContext _localctx = new ChoiceContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_choice);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANONYMOUS_VARIABLE) | (1L << MINUS) | (1L << PAREN_OPEN) | (1L << ID) | (1L << VARIABLE) | (1L << NUMBER) | (1L << QUOTED_STRING))) != 0)) {
				{
				setState(121);
				((ChoiceContext)_localctx).lt = term(0);
				setState(122);
				((ChoiceContext)_localctx).lop = binop();
				}
			}

			setState(126);
			match(CURLY_OPEN);
			setState(128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS || _la==ID) {
				{
				setState(127);
				choice_elements();
				}
			}

			setState(130);
			match(CURLY_CLOSE);
			setState(134);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQUAL) | (1L << UNEQUAL) | (1L << LESS) | (1L << GREATER) | (1L << LESS_OR_EQ) | (1L << GREATER_OR_EQ))) != 0)) {
				{
				setState(131);
				((ChoiceContext)_localctx).uop = binop();
				setState(132);
				((ChoiceContext)_localctx).ut = term(0);
				}
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

	public static class Choice_elementsContext extends ParserRuleContext {
		public Choice_elementContext choice_element() {
			return getRuleContext(Choice_elementContext.class,0);
		}
		public TerminalNode SEMICOLON() { return getToken(ASPCore2Parser.SEMICOLON, 0); }
		public Choice_elementsContext choice_elements() {
			return getRuleContext(Choice_elementsContext.class,0);
		}
		public Choice_elementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_choice_elements; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitChoice_elements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Choice_elementsContext choice_elements() throws RecognitionException {
		Choice_elementsContext _localctx = new Choice_elementsContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_choice_elements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			choice_element();
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMICOLON) {
				{
				setState(137);
				match(SEMICOLON);
				setState(138);
				choice_elements();
				}
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

	public static class Choice_elementContext extends ParserRuleContext {
		public Classical_literalContext classical_literal() {
			return getRuleContext(Classical_literalContext.class,0);
		}
		public TerminalNode COLON() { return getToken(ASPCore2Parser.COLON, 0); }
		public Naf_literalsContext naf_literals() {
			return getRuleContext(Naf_literalsContext.class,0);
		}
		public Choice_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_choice_element; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitChoice_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Choice_elementContext choice_element() throws RecognitionException {
		Choice_elementContext _localctx = new Choice_elementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_choice_element);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			classical_literal();
			setState(146);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(142);
				match(COLON);
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANONYMOUS_VARIABLE) | (1L << NAF) | (1L << MINUS) | (1L << AMPERSAND) | (1L << PAREN_OPEN) | (1L << ID) | (1L << VARIABLE) | (1L << NUMBER) | (1L << QUOTED_STRING))) != 0)) {
					{
					setState(143);
					naf_literals();
					}
				}

				}
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

	public static class AggregateContext extends ParserRuleContext {
		public TermContext lt;
		public BinopContext lop;
		public BinopContext uop;
		public TermContext ut;
		public Aggregate_functionContext aggregate_function() {
			return getRuleContext(Aggregate_functionContext.class,0);
		}
		public TerminalNode CURLY_OPEN() { return getToken(ASPCore2Parser.CURLY_OPEN, 0); }
		public Aggregate_elementsContext aggregate_elements() {
			return getRuleContext(Aggregate_elementsContext.class,0);
		}
		public TerminalNode CURLY_CLOSE() { return getToken(ASPCore2Parser.CURLY_CLOSE, 0); }
		public TerminalNode NAF() { return getToken(ASPCore2Parser.NAF, 0); }
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<BinopContext> binop() {
			return getRuleContexts(BinopContext.class);
		}
		public BinopContext binop(int i) {
			return getRuleContext(BinopContext.class,i);
		}
		public AggregateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregate; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitAggregate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregateContext aggregate() throws RecognitionException {
		AggregateContext _localctx = new AggregateContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_aggregate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NAF) {
				{
				setState(148);
				match(NAF);
				}
			}

			setState(154);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANONYMOUS_VARIABLE) | (1L << MINUS) | (1L << PAREN_OPEN) | (1L << ID) | (1L << VARIABLE) | (1L << NUMBER) | (1L << QUOTED_STRING))) != 0)) {
				{
				setState(151);
				((AggregateContext)_localctx).lt = term(0);
				setState(152);
				((AggregateContext)_localctx).lop = binop();
				}
			}

			setState(156);
			aggregate_function();
			setState(157);
			match(CURLY_OPEN);
			setState(158);
			aggregate_elements();
			setState(159);
			match(CURLY_CLOSE);
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQUAL) | (1L << UNEQUAL) | (1L << LESS) | (1L << GREATER) | (1L << LESS_OR_EQ) | (1L << GREATER_OR_EQ))) != 0)) {
				{
				setState(160);
				((AggregateContext)_localctx).uop = binop();
				setState(161);
				((AggregateContext)_localctx).ut = term(0);
				}
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

	public static class Aggregate_elementsContext extends ParserRuleContext {
		public Aggregate_elementContext aggregate_element() {
			return getRuleContext(Aggregate_elementContext.class,0);
		}
		public TerminalNode SEMICOLON() { return getToken(ASPCore2Parser.SEMICOLON, 0); }
		public Aggregate_elementsContext aggregate_elements() {
			return getRuleContext(Aggregate_elementsContext.class,0);
		}
		public Aggregate_elementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregate_elements; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitAggregate_elements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Aggregate_elementsContext aggregate_elements() throws RecognitionException {
		Aggregate_elementsContext _localctx = new Aggregate_elementsContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_aggregate_elements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			aggregate_element();
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMICOLON) {
				{
				setState(166);
				match(SEMICOLON);
				setState(167);
				aggregate_elements();
				}
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

	public static class Aggregate_elementContext extends ParserRuleContext {
		public Basic_termsContext basic_terms() {
			return getRuleContext(Basic_termsContext.class,0);
		}
		public TerminalNode COLON() { return getToken(ASPCore2Parser.COLON, 0); }
		public Naf_literalsContext naf_literals() {
			return getRuleContext(Naf_literalsContext.class,0);
		}
		public Aggregate_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregate_element; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitAggregate_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Aggregate_elementContext aggregate_element() throws RecognitionException {
		Aggregate_elementContext _localctx = new Aggregate_elementContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_aggregate_element);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANONYMOUS_VARIABLE) | (1L << MINUS) | (1L << ID) | (1L << VARIABLE) | (1L << NUMBER) | (1L << QUOTED_STRING))) != 0)) {
				{
				setState(170);
				basic_terms();
				}
			}

			setState(177);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(173);
				match(COLON);
				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANONYMOUS_VARIABLE) | (1L << NAF) | (1L << MINUS) | (1L << AMPERSAND) | (1L << PAREN_OPEN) | (1L << ID) | (1L << VARIABLE) | (1L << NUMBER) | (1L << QUOTED_STRING))) != 0)) {
					{
					setState(174);
					naf_literals();
					}
				}

				}
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

	public static class Aggregate_functionContext extends ParserRuleContext {
		public TerminalNode AGGREGATE_COUNT() { return getToken(ASPCore2Parser.AGGREGATE_COUNT, 0); }
		public TerminalNode AGGREGATE_MAX() { return getToken(ASPCore2Parser.AGGREGATE_MAX, 0); }
		public TerminalNode AGGREGATE_MIN() { return getToken(ASPCore2Parser.AGGREGATE_MIN, 0); }
		public TerminalNode AGGREGATE_SUM() { return getToken(ASPCore2Parser.AGGREGATE_SUM, 0); }
		public Aggregate_functionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregate_function; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitAggregate_function(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Aggregate_functionContext aggregate_function() throws RecognitionException {
		Aggregate_functionContext _localctx = new Aggregate_functionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_aggregate_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AGGREGATE_COUNT) | (1L << AGGREGATE_MAX) | (1L << AGGREGATE_MIN) | (1L << AGGREGATE_SUM))) != 0)) ) {
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

	public static class Weight_at_levelContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode AT() { return getToken(ASPCore2Parser.AT, 0); }
		public TerminalNode COMMA() { return getToken(ASPCore2Parser.COMMA, 0); }
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public Weight_at_levelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_weight_at_level; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitWeight_at_level(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Weight_at_levelContext weight_at_level() throws RecognitionException {
		Weight_at_levelContext _localctx = new Weight_at_levelContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_weight_at_level);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			term(0);
			setState(184);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT) {
				{
				setState(182);
				match(AT);
				setState(183);
				term(0);
				}
			}

			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(186);
				match(COMMA);
				setState(187);
				terms();
				}
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

	public static class Naf_literalsContext extends ParserRuleContext {
		public Naf_literalContext naf_literal() {
			return getRuleContext(Naf_literalContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(ASPCore2Parser.COMMA, 0); }
		public Naf_literalsContext naf_literals() {
			return getRuleContext(Naf_literalsContext.class,0);
		}
		public Naf_literalsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_naf_literals; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitNaf_literals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Naf_literalsContext naf_literals() throws RecognitionException {
		Naf_literalsContext _localctx = new Naf_literalsContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_naf_literals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			naf_literal();
			setState(193);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(191);
				match(COMMA);
				setState(192);
				naf_literals();
				}
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

	public static class Naf_literalContext extends ParserRuleContext {
		public External_atomContext external_atom() {
			return getRuleContext(External_atomContext.class,0);
		}
		public Classical_literalContext classical_literal() {
			return getRuleContext(Classical_literalContext.class,0);
		}
		public Builtin_atomContext builtin_atom() {
			return getRuleContext(Builtin_atomContext.class,0);
		}
		public TerminalNode NAF() { return getToken(ASPCore2Parser.NAF, 0); }
		public Naf_literalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_naf_literal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitNaf_literal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Naf_literalContext naf_literal() throws RecognitionException {
		Naf_literalContext _localctx = new Naf_literalContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_naf_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NAF) {
				{
				setState(195);
				match(NAF);
				}
			}

			setState(201);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(198);
				external_atom();
				}
				break;
			case 2:
				{
				setState(199);
				classical_literal();
				}
				break;
			case 3:
				{
				setState(200);
				builtin_atom();
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

	public static class Classical_literalContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ASPCore2Parser.ID, 0); }
		public TerminalNode MINUS() { return getToken(ASPCore2Parser.MINUS, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(ASPCore2Parser.PAREN_OPEN, 0); }
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TerminalNode PAREN_CLOSE() { return getToken(ASPCore2Parser.PAREN_CLOSE, 0); }
		public Classical_literalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classical_literal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitClassical_literal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Classical_literalContext classical_literal() throws RecognitionException {
		Classical_literalContext _localctx = new Classical_literalContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_classical_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS) {
				{
				setState(203);
				match(MINUS);
				}
			}

			setState(206);
			match(ID);
			setState(211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PAREN_OPEN) {
				{
				setState(207);
				match(PAREN_OPEN);
				setState(208);
				terms();
				setState(209);
				match(PAREN_CLOSE);
				}
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

	public static class Builtin_atomContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public BinopContext binop() {
			return getRuleContext(BinopContext.class,0);
		}
		public Builtin_atomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_builtin_atom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitBuiltin_atom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Builtin_atomContext builtin_atom() throws RecognitionException {
		Builtin_atomContext _localctx = new Builtin_atomContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_builtin_atom);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213);
			term(0);
			setState(214);
			binop();
			setState(215);
			term(0);
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

	public static class BinopContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(ASPCore2Parser.EQUAL, 0); }
		public TerminalNode UNEQUAL() { return getToken(ASPCore2Parser.UNEQUAL, 0); }
		public TerminalNode LESS() { return getToken(ASPCore2Parser.LESS, 0); }
		public TerminalNode GREATER() { return getToken(ASPCore2Parser.GREATER, 0); }
		public TerminalNode LESS_OR_EQ() { return getToken(ASPCore2Parser.LESS_OR_EQ, 0); }
		public TerminalNode GREATER_OR_EQ() { return getToken(ASPCore2Parser.GREATER_OR_EQ, 0); }
		public BinopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binop; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitBinop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BinopContext binop() throws RecognitionException {
		BinopContext _localctx = new BinopContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_binop);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQUAL) | (1L << UNEQUAL) | (1L << LESS) | (1L << GREATER) | (1L << LESS_OR_EQ) | (1L << GREATER_OR_EQ))) != 0)) ) {
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

	public static class TermsContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(ASPCore2Parser.COMMA, 0); }
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TermsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_terms; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerms(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermsContext terms() throws RecognitionException {
		TermsContext _localctx = new TermsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_terms);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			term(0);
			setState(222);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(220);
				match(COMMA);
				setState(221);
				terms();
				}
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

	public static class TermContext extends ParserRuleContext {
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
	 
		public TermContext() { }
		public void copyFrom(TermContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Term_numberContext extends TermContext {
		public TerminalNode NUMBER() { return getToken(ASPCore2Parser.NUMBER, 0); }
		public Term_numberContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_number(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_anonymousVariableContext extends TermContext {
		public TerminalNode ANONYMOUS_VARIABLE() { return getToken(ASPCore2Parser.ANONYMOUS_VARIABLE, 0); }
		public Term_anonymousVariableContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_anonymousVariable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_constContext extends TermContext {
		public TerminalNode ID() { return getToken(ASPCore2Parser.ID, 0); }
		public Term_constContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_const(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_minusArithTermContext extends TermContext {
		public TerminalNode MINUS() { return getToken(ASPCore2Parser.MINUS, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public Term_minusArithTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_minusArithTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_bitxorArithTermContext extends TermContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode BITXOR() { return getToken(ASPCore2Parser.BITXOR, 0); }
		public Term_bitxorArithTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_bitxorArithTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_intervalContext extends TermContext {
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public Term_intervalContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_interval(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_variableContext extends TermContext {
		public TerminalNode VARIABLE() { return getToken(ASPCore2Parser.VARIABLE, 0); }
		public Term_variableContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_variable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_timesdivmodArithTermContext extends TermContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode TIMES() { return getToken(ASPCore2Parser.TIMES, 0); }
		public TerminalNode DIV() { return getToken(ASPCore2Parser.DIV, 0); }
		public TerminalNode MODULO() { return getToken(ASPCore2Parser.MODULO, 0); }
		public Term_timesdivmodArithTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_timesdivmodArithTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_plusminusArithTermContext extends TermContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(ASPCore2Parser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ASPCore2Parser.MINUS, 0); }
		public Term_plusminusArithTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_plusminusArithTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_powerArithTermContext extends TermContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public TerminalNode POWER() { return getToken(ASPCore2Parser.POWER, 0); }
		public Term_powerArithTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_powerArithTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_funcContext extends TermContext {
		public TerminalNode ID() { return getToken(ASPCore2Parser.ID, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(ASPCore2Parser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(ASPCore2Parser.PAREN_CLOSE, 0); }
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public Term_funcContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_func(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_stringContext extends TermContext {
		public TerminalNode QUOTED_STRING() { return getToken(ASPCore2Parser.QUOTED_STRING, 0); }
		public Term_stringContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_string(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Term_parenthesisedTermContext extends TermContext {
		public TerminalNode PAREN_OPEN() { return getToken(ASPCore2Parser.PAREN_OPEN, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode PAREN_CLOSE() { return getToken(ASPCore2Parser.PAREN_CLOSE, 0); }
		public Term_parenthesisedTermContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitTerm_parenthesisedTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		return term(0);
	}

	private TermContext term(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermContext _localctx = new TermContext(_ctx, _parentState);
		TermContext _prevctx = _localctx;
		int _startState = 42;
		enterRecursionRule(_localctx, 42, RULE_term, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(243);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				{
				_localctx = new Term_constContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(225);
				match(ID);
				}
				break;
			case 2:
				{
				_localctx = new Term_funcContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(226);
				match(ID);
				{
				setState(227);
				match(PAREN_OPEN);
				setState(229);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ANONYMOUS_VARIABLE) | (1L << MINUS) | (1L << PAREN_OPEN) | (1L << ID) | (1L << VARIABLE) | (1L << NUMBER) | (1L << QUOTED_STRING))) != 0)) {
					{
					setState(228);
					terms();
					}
				}

				setState(231);
				match(PAREN_CLOSE);
				}
				}
				break;
			case 3:
				{
				_localctx = new Term_numberContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(232);
				match(NUMBER);
				}
				break;
			case 4:
				{
				_localctx = new Term_stringContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(233);
				match(QUOTED_STRING);
				}
				break;
			case 5:
				{
				_localctx = new Term_variableContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(234);
				match(VARIABLE);
				}
				break;
			case 6:
				{
				_localctx = new Term_anonymousVariableContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(235);
				match(ANONYMOUS_VARIABLE);
				}
				break;
			case 7:
				{
				_localctx = new Term_parenthesisedTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(236);
				match(PAREN_OPEN);
				setState(237);
				term(0);
				setState(238);
				match(PAREN_CLOSE);
				}
				break;
			case 8:
				{
				_localctx = new Term_intervalContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(240);
				interval();
				}
				break;
			case 9:
				{
				_localctx = new Term_minusArithTermContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(241);
				match(MINUS);
				setState(242);
				term(5);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(259);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(257);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
					case 1:
						{
						_localctx = new Term_powerArithTermContext(new TermContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_term);
						setState(245);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(246);
						match(POWER);
						setState(247);
						term(4);
						}
						break;
					case 2:
						{
						_localctx = new Term_timesdivmodArithTermContext(new TermContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_term);
						setState(248);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(249);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TIMES) | (1L << DIV) | (1L << MODULO))) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(250);
						term(4);
						}
						break;
					case 3:
						{
						_localctx = new Term_plusminusArithTermContext(new TermContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_term);
						setState(251);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(252);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(253);
						term(3);
						}
						break;
					case 4:
						{
						_localctx = new Term_bitxorArithTermContext(new TermContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_term);
						setState(254);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(255);
						match(BITXOR);
						setState(256);
						term(2);
						}
						break;
					}
					} 
				}
				setState(261);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class IntervalContext extends ParserRuleContext {
		public Token lower;
		public Token upper;
		public List<TerminalNode> DOT() { return getTokens(ASPCore2Parser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ASPCore2Parser.DOT, i);
		}
		public List<TerminalNode> NUMBER() { return getTokens(ASPCore2Parser.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(ASPCore2Parser.NUMBER, i);
		}
		public List<TerminalNode> VARIABLE() { return getTokens(ASPCore2Parser.VARIABLE); }
		public TerminalNode VARIABLE(int i) {
			return getToken(ASPCore2Parser.VARIABLE, i);
		}
		public IntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interval; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_interval);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(262);
			((IntervalContext)_localctx).lower = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==VARIABLE || _la==NUMBER) ) {
				((IntervalContext)_localctx).lower = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(263);
			match(DOT);
			setState(264);
			match(DOT);
			setState(265);
			((IntervalContext)_localctx).upper = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==VARIABLE || _la==NUMBER) ) {
				((IntervalContext)_localctx).upper = (Token)_errHandler.recoverInline(this);
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

	public static class External_atomContext extends ParserRuleContext {
		public TermsContext input;
		public TermsContext output;
		public TerminalNode AMPERSAND() { return getToken(ASPCore2Parser.AMPERSAND, 0); }
		public TerminalNode ID() { return getToken(ASPCore2Parser.ID, 0); }
		public TerminalNode MINUS() { return getToken(ASPCore2Parser.MINUS, 0); }
		public TerminalNode SQUARE_OPEN() { return getToken(ASPCore2Parser.SQUARE_OPEN, 0); }
		public TerminalNode SQUARE_CLOSE() { return getToken(ASPCore2Parser.SQUARE_CLOSE, 0); }
		public TerminalNode PAREN_OPEN() { return getToken(ASPCore2Parser.PAREN_OPEN, 0); }
		public TerminalNode PAREN_CLOSE() { return getToken(ASPCore2Parser.PAREN_CLOSE, 0); }
		public List<TermsContext> terms() {
			return getRuleContexts(TermsContext.class);
		}
		public TermsContext terms(int i) {
			return getRuleContext(TermsContext.class,i);
		}
		public External_atomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_external_atom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitExternal_atom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final External_atomContext external_atom() throws RecognitionException {
		External_atomContext _localctx = new External_atomContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_external_atom);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS) {
				{
				setState(267);
				match(MINUS);
				}
			}

			setState(270);
			match(AMPERSAND);
			setState(271);
			match(ID);
			setState(276);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SQUARE_OPEN) {
				{
				setState(272);
				match(SQUARE_OPEN);
				setState(273);
				((External_atomContext)_localctx).input = terms();
				setState(274);
				match(SQUARE_CLOSE);
				}
			}

			setState(282);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PAREN_OPEN) {
				{
				setState(278);
				match(PAREN_OPEN);
				setState(279);
				((External_atomContext)_localctx).output = terms();
				setState(280);
				match(PAREN_CLOSE);
				}
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

	public static class DirectiveContext extends ParserRuleContext {
		public Directive_enumerationContext directive_enumeration() {
			return getRuleContext(Directive_enumerationContext.class,0);
		}
		public DirectiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directive; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitDirective(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectiveContext directive() throws RecognitionException {
		DirectiveContext _localctx = new DirectiveContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_directive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
			directive_enumeration();
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

	public static class Directive_enumerationContext extends ParserRuleContext {
		public TerminalNode SHARP() { return getToken(ASPCore2Parser.SHARP, 0); }
		public TerminalNode ID() { return getToken(ASPCore2Parser.ID, 0); }
		public TerminalNode DOT() { return getToken(ASPCore2Parser.DOT, 0); }
		public Directive_enumerationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directive_enumeration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitDirective_enumeration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Directive_enumerationContext directive_enumeration() throws RecognitionException {
		Directive_enumerationContext _localctx = new Directive_enumerationContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_directive_enumeration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			match(SHARP);
			setState(287);
			match(T__0);
			setState(288);
			match(ID);
			setState(289);
			match(DOT);
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

	public static class Basic_termsContext extends ParserRuleContext {
		public Basic_termContext basic_term() {
			return getRuleContext(Basic_termContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(ASPCore2Parser.COMMA, 0); }
		public Basic_termsContext basic_terms() {
			return getRuleContext(Basic_termsContext.class,0);
		}
		public Basic_termsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basic_terms; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitBasic_terms(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Basic_termsContext basic_terms() throws RecognitionException {
		Basic_termsContext _localctx = new Basic_termsContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_basic_terms);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			basic_term();
			setState(294);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(292);
				match(COMMA);
				setState(293);
				basic_terms();
				}
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

	public static class Basic_termContext extends ParserRuleContext {
		public Ground_termContext ground_term() {
			return getRuleContext(Ground_termContext.class,0);
		}
		public Variable_termContext variable_term() {
			return getRuleContext(Variable_termContext.class,0);
		}
		public Basic_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basic_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitBasic_term(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Basic_termContext basic_term() throws RecognitionException {
		Basic_termContext _localctx = new Basic_termContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_basic_term);
		try {
			setState(298);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS:
			case ID:
			case NUMBER:
			case QUOTED_STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(296);
				ground_term();
				}
				break;
			case ANONYMOUS_VARIABLE:
			case VARIABLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(297);
				variable_term();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class Ground_termContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ASPCore2Parser.ID, 0); }
		public TerminalNode QUOTED_STRING() { return getToken(ASPCore2Parser.QUOTED_STRING, 0); }
		public TerminalNode NUMBER() { return getToken(ASPCore2Parser.NUMBER, 0); }
		public TerminalNode MINUS() { return getToken(ASPCore2Parser.MINUS, 0); }
		public Ground_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ground_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitGround_term(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ground_termContext ground_term() throws RecognitionException {
		Ground_termContext _localctx = new Ground_termContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_ground_term);
		int _la;
		try {
			setState(306);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(300);
				match(ID);
				}
				break;
			case QUOTED_STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(301);
				match(QUOTED_STRING);
				}
				break;
			case MINUS:
			case NUMBER:
				enterOuterAlt(_localctx, 3);
				{
				setState(303);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(302);
					match(MINUS);
					}
				}

				setState(305);
				match(NUMBER);
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class Variable_termContext extends ParserRuleContext {
		public TerminalNode VARIABLE() { return getToken(ASPCore2Parser.VARIABLE, 0); }
		public TerminalNode ANONYMOUS_VARIABLE() { return getToken(ASPCore2Parser.ANONYMOUS_VARIABLE, 0); }
		public Variable_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable_term; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitVariable_term(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Variable_termContext variable_term() throws RecognitionException {
		Variable_termContext _localctx = new Variable_termContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_variable_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			_la = _input.LA(1);
			if ( !(_la==ANONYMOUS_VARIABLE || _la==VARIABLE) ) {
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

	public static class Answer_setContext extends ParserRuleContext {
		public TerminalNode CURLY_OPEN() { return getToken(ASPCore2Parser.CURLY_OPEN, 0); }
		public TerminalNode CURLY_CLOSE() { return getToken(ASPCore2Parser.CURLY_CLOSE, 0); }
		public List<Classical_literalContext> classical_literal() {
			return getRuleContexts(Classical_literalContext.class);
		}
		public Classical_literalContext classical_literal(int i) {
			return getRuleContext(Classical_literalContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ASPCore2Parser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ASPCore2Parser.COMMA, i);
		}
		public Answer_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_answer_set; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitAnswer_set(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Answer_setContext answer_set() throws RecognitionException {
		Answer_setContext _localctx = new Answer_setContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_answer_set);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			match(CURLY_OPEN);
			setState(312);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS || _la==ID) {
				{
				setState(311);
				classical_literal();
				}
			}

			setState(318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(314);
				match(COMMA);
				setState(315);
				classical_literal();
				}
				}
				setState(320);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(321);
			match(CURLY_CLOSE);
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

	public static class Answer_setsContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(ASPCore2Parser.EOF, 0); }
		public List<Answer_setContext> answer_set() {
			return getRuleContexts(Answer_setContext.class);
		}
		public Answer_setContext answer_set(int i) {
			return getRuleContext(Answer_setContext.class,i);
		}
		public Answer_setsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_answer_sets; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ASPCore2Visitor ) return ((ASPCore2Visitor<? extends T>)visitor).visitAnswer_sets(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Answer_setsContext answer_sets() throws RecognitionException {
		Answer_setsContext _localctx = new Answer_setsContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_answer_sets);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CURLY_OPEN) {
				{
				{
				setState(323);
				answer_set();
				}
				}
				setState(328);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(329);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 21:
			return term_sempred((TermContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean term_sempred(TermContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		case 2:
			return precpred(_ctx, 2);
		case 3:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3/\u014e\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\3\2\5\2D\n\2\3\2\5\2G\n\2\3\2\3\2\3\3\6\3L\n\3\r\3\16\3M\3\4\3\4\3"+
		"\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5a\n\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\5\5i\n\5\3\6\3\6\5\6m\n\6\3\7\3\7\5\7q\n\7\3\7"+
		"\3\7\5\7u\n\7\3\b\3\b\3\b\5\bz\n\b\3\t\3\t\3\t\5\t\177\n\t\3\t\3\t\5\t"+
		"\u0083\n\t\3\t\3\t\3\t\3\t\5\t\u0089\n\t\3\n\3\n\3\n\5\n\u008e\n\n\3\13"+
		"\3\13\3\13\5\13\u0093\n\13\5\13\u0095\n\13\3\f\5\f\u0098\n\f\3\f\3\f\3"+
		"\f\5\f\u009d\n\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u00a6\n\f\3\r\3\r\3\r"+
		"\5\r\u00ab\n\r\3\16\5\16\u00ae\n\16\3\16\3\16\5\16\u00b2\n\16\5\16\u00b4"+
		"\n\16\3\17\3\17\3\20\3\20\3\20\5\20\u00bb\n\20\3\20\3\20\5\20\u00bf\n"+
		"\20\3\21\3\21\3\21\5\21\u00c4\n\21\3\22\5\22\u00c7\n\22\3\22\3\22\3\22"+
		"\5\22\u00cc\n\22\3\23\5\23\u00cf\n\23\3\23\3\23\3\23\3\23\3\23\5\23\u00d6"+
		"\n\23\3\24\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\26\5\26\u00e1\n\26\3\27"+
		"\3\27\3\27\3\27\3\27\5\27\u00e8\n\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\5\27\u00f6\n\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\7\27\u0104\n\27\f\27\16\27\u0107\13\27"+
		"\3\30\3\30\3\30\3\30\3\30\3\31\5\31\u010f\n\31\3\31\3\31\3\31\3\31\3\31"+
		"\3\31\5\31\u0117\n\31\3\31\3\31\3\31\3\31\5\31\u011d\n\31\3\32\3\32\3"+
		"\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\5\34\u0129\n\34\3\35\3\35\5\35"+
		"\u012d\n\35\3\36\3\36\3\36\5\36\u0132\n\36\3\36\5\36\u0135\n\36\3\37\3"+
		"\37\3 \3 \5 \u013b\n \3 \3 \7 \u013f\n \f \16 \u0142\13 \3 \3 \3!\7!\u0147"+
		"\n!\f!\16!\u014a\13!\3!\3!\3!\2\3,\"\2\4\6\b\n\f\16\20\22\24\26\30\32"+
		"\34\36 \"$&(*,.\60\62\64\668:<>@\2\b\3\2%(\3\2\37$\4\2\20\21\23\23\3\2"+
		"\16\17\3\2*+\4\2\4\4**\2\u0167\2C\3\2\2\2\4K\3\2\2\2\6O\3\2\2\2\bh\3\2"+
		"\2\2\nl\3\2\2\2\fp\3\2\2\2\16v\3\2\2\2\20~\3\2\2\2\22\u008a\3\2\2\2\24"+
		"\u008f\3\2\2\2\26\u0097\3\2\2\2\30\u00a7\3\2\2\2\32\u00ad\3\2\2\2\34\u00b5"+
		"\3\2\2\2\36\u00b7\3\2\2\2 \u00c0\3\2\2\2\"\u00c6\3\2\2\2$\u00ce\3\2\2"+
		"\2&\u00d7\3\2\2\2(\u00db\3\2\2\2*\u00dd\3\2\2\2,\u00f5\3\2\2\2.\u0108"+
		"\3\2\2\2\60\u010e\3\2\2\2\62\u011e\3\2\2\2\64\u0120\3\2\2\2\66\u0125\3"+
		"\2\2\28\u012c\3\2\2\2:\u0134\3\2\2\2<\u0136\3\2\2\2>\u0138\3\2\2\2@\u0148"+
		"\3\2\2\2BD\5\4\3\2CB\3\2\2\2CD\3\2\2\2DF\3\2\2\2EG\5\6\4\2FE\3\2\2\2F"+
		"G\3\2\2\2GH\3\2\2\2HI\7\2\2\3I\3\3\2\2\2JL\5\b\5\2KJ\3\2\2\2LM\3\2\2\2"+
		"MK\3\2\2\2MN\3\2\2\2N\5\3\2\2\2OP\5$\23\2PQ\7\7\2\2Q\7\3\2\2\2RS\5\n\6"+
		"\2ST\7\5\2\2Ti\3\2\2\2UV\7\f\2\2VW\5\f\7\2WX\7\5\2\2Xi\3\2\2\2YZ\5\n\6"+
		"\2Z[\7\f\2\2[\\\5\f\7\2\\]\7\5\2\2]i\3\2\2\2^`\7\r\2\2_a\5\f\7\2`_\3\2"+
		"\2\2`a\3\2\2\2ab\3\2\2\2bc\7\5\2\2cd\7\33\2\2de\5\36\20\2ef\7\34\2\2f"+
		"i\3\2\2\2gi\5\62\32\2hR\3\2\2\2hU\3\2\2\2hY\3\2\2\2h^\3\2\2\2hg\3\2\2"+
		"\2i\t\3\2\2\2jm\5\16\b\2km\5\20\t\2lj\3\2\2\2lk\3\2\2\2m\13\3\2\2\2nq"+
		"\5\"\22\2oq\5\26\f\2pn\3\2\2\2po\3\2\2\2qt\3\2\2\2rs\7\6\2\2su\5\f\7\2"+
		"tr\3\2\2\2tu\3\2\2\2u\r\3\2\2\2vy\5$\23\2wx\7\n\2\2xz\5\16\b\2yw\3\2\2"+
		"\2yz\3\2\2\2z\17\3\2\2\2{|\5,\27\2|}\5(\25\2}\177\3\2\2\2~{\3\2\2\2~\177"+
		"\3\2\2\2\177\u0080\3\2\2\2\u0080\u0082\7\35\2\2\u0081\u0083\5\22\n\2\u0082"+
		"\u0081\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0088\7\36"+
		"\2\2\u0085\u0086\5(\25\2\u0086\u0087\5,\27\2\u0087\u0089\3\2\2\2\u0088"+
		"\u0085\3\2\2\2\u0088\u0089\3\2\2\2\u0089\21\3\2\2\2\u008a\u008d\5\24\13"+
		"\2\u008b\u008c\7\t\2\2\u008c\u008e\5\22\n\2\u008d\u008b\3\2\2\2\u008d"+
		"\u008e\3\2\2\2\u008e\23\3\2\2\2\u008f\u0094\5$\23\2\u0090\u0092\7\b\2"+
		"\2\u0091\u0093\5 \21\2\u0092\u0091\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0095"+
		"\3\2\2\2\u0094\u0090\3\2\2\2\u0094\u0095\3\2\2\2\u0095\25\3\2\2\2\u0096"+
		"\u0098\7\13\2\2\u0097\u0096\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u009c\3"+
		"\2\2\2\u0099\u009a\5,\27\2\u009a\u009b\5(\25\2\u009b\u009d\3\2\2\2\u009c"+
		"\u0099\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009f\5\34"+
		"\17\2\u009f\u00a0\7\35\2\2\u00a0\u00a1\5\30\r\2\u00a1\u00a5\7\36\2\2\u00a2"+
		"\u00a3\5(\25\2\u00a3\u00a4\5,\27\2\u00a4\u00a6\3\2\2\2\u00a5\u00a2\3\2"+
		"\2\2\u00a5\u00a6\3\2\2\2\u00a6\27\3\2\2\2\u00a7\u00aa\5\32\16\2\u00a8"+
		"\u00a9\7\t\2\2\u00a9\u00ab\5\30\r\2\u00aa\u00a8\3\2\2\2\u00aa\u00ab\3"+
		"\2\2\2\u00ab\31\3\2\2\2\u00ac\u00ae\5\66\34\2\u00ad\u00ac\3\2\2\2\u00ad"+
		"\u00ae\3\2\2\2\u00ae\u00b3\3\2\2\2\u00af\u00b1\7\b\2\2\u00b0\u00b2\5 "+
		"\21\2\u00b1\u00b0\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b4\3\2\2\2\u00b3"+
		"\u00af\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4\33\3\2\2\2\u00b5\u00b6\t\2\2"+
		"\2\u00b6\35\3\2\2\2\u00b7\u00ba\5,\27\2\u00b8\u00b9\7\25\2\2\u00b9\u00bb"+
		"\5,\27\2\u00ba\u00b8\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00be\3\2\2\2\u00bc"+
		"\u00bd\7\6\2\2\u00bd\u00bf\5*\26\2\u00be\u00bc\3\2\2\2\u00be\u00bf\3\2"+
		"\2\2\u00bf\37\3\2\2\2\u00c0\u00c3\5\"\22\2\u00c1\u00c2\7\6\2\2\u00c2\u00c4"+
		"\5 \21\2\u00c3\u00c1\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4!\3\2\2\2\u00c5"+
		"\u00c7\7\13\2\2\u00c6\u00c5\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00cb\3"+
		"\2\2\2\u00c8\u00cc\5\60\31\2\u00c9\u00cc\5$\23\2\u00ca\u00cc\5&\24\2\u00cb"+
		"\u00c8\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00ca\3\2\2\2\u00cc#\3\2\2\2"+
		"\u00cd\u00cf\7\17\2\2\u00ce\u00cd\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00d0"+
		"\3\2\2\2\u00d0\u00d5\7)\2\2\u00d1\u00d2\7\31\2\2\u00d2\u00d3\5*\26\2\u00d3"+
		"\u00d4\7\32\2\2\u00d4\u00d6\3\2\2\2\u00d5\u00d1\3\2\2\2\u00d5\u00d6\3"+
		"\2\2\2\u00d6%\3\2\2\2\u00d7\u00d8\5,\27\2\u00d8\u00d9\5(\25\2\u00d9\u00da"+
		"\5,\27\2\u00da\'\3\2\2\2\u00db\u00dc\t\3\2\2\u00dc)\3\2\2\2\u00dd\u00e0"+
		"\5,\27\2\u00de\u00df\7\6\2\2\u00df\u00e1\5*\26\2\u00e0\u00de\3\2\2\2\u00e0"+
		"\u00e1\3\2\2\2\u00e1+\3\2\2\2\u00e2\u00e3\b\27\1\2\u00e3\u00f6\7)\2\2"+
		"\u00e4\u00e5\7)\2\2\u00e5\u00e7\7\31\2\2\u00e6\u00e8\5*\26\2\u00e7\u00e6"+
		"\3\2\2\2\u00e7\u00e8\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00f6\7\32\2\2"+
		"\u00ea\u00f6\7+\2\2\u00eb\u00f6\7,\2\2\u00ec\u00f6\7*\2\2\u00ed\u00f6"+
		"\7\4\2\2\u00ee\u00ef\7\31\2\2\u00ef\u00f0\5,\27\2\u00f0\u00f1\7\32\2\2"+
		"\u00f1\u00f6\3\2\2\2\u00f2\u00f6\5.\30\2\u00f3\u00f4\7\17\2\2\u00f4\u00f6"+
		"\5,\27\7\u00f5\u00e2\3\2\2\2\u00f5\u00e4\3\2\2\2\u00f5\u00ea\3\2\2\2\u00f5"+
		"\u00eb\3\2\2\2\u00f5\u00ec\3\2\2\2\u00f5\u00ed\3\2\2\2\u00f5\u00ee\3\2"+
		"\2\2\u00f5\u00f2\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f6\u0105\3\2\2\2\u00f7"+
		"\u00f8\f\6\2\2\u00f8\u00f9\7\22\2\2\u00f9\u0104\5,\27\6\u00fa\u00fb\f"+
		"\5\2\2\u00fb\u00fc\t\4\2\2\u00fc\u0104\5,\27\6\u00fd\u00fe\f\4\2\2\u00fe"+
		"\u00ff\t\5\2\2\u00ff\u0104\5,\27\5\u0100\u0101\f\3\2\2\u0101\u0102\7\24"+
		"\2\2\u0102\u0104\5,\27\4\u0103\u00f7\3\2\2\2\u0103\u00fa\3\2\2\2\u0103"+
		"\u00fd\3\2\2\2\u0103\u0100\3\2\2\2\u0104\u0107\3\2\2\2\u0105\u0103\3\2"+
		"\2\2\u0105\u0106\3\2\2\2\u0106-\3\2\2\2\u0107\u0105\3\2\2\2\u0108\u0109"+
		"\t\6\2\2\u0109\u010a\7\5\2\2\u010a\u010b\7\5\2\2\u010b\u010c\t\6\2\2\u010c"+
		"/\3\2\2\2\u010d\u010f\7\17\2\2\u010e\u010d\3\2\2\2\u010e\u010f\3\2\2\2"+
		"\u010f\u0110\3\2\2\2\u0110\u0111\7\27\2\2\u0111\u0116\7)\2\2\u0112\u0113"+
		"\7\33\2\2\u0113\u0114\5*\26\2\u0114\u0115\7\34\2\2\u0115\u0117\3\2\2\2"+
		"\u0116\u0112\3\2\2\2\u0116\u0117\3\2\2\2\u0117\u011c\3\2\2\2\u0118\u0119"+
		"\7\31\2\2\u0119\u011a\5*\26\2\u011a\u011b\7\32\2\2\u011b\u011d\3\2\2\2"+
		"\u011c\u0118\3\2\2\2\u011c\u011d\3\2\2\2\u011d\61\3\2\2\2\u011e\u011f"+
		"\5\64\33\2\u011f\63\3\2\2\2\u0120\u0121\7\26\2\2\u0121\u0122\7\3\2\2\u0122"+
		"\u0123\7)\2\2\u0123\u0124\7\5\2\2\u0124\65\3\2\2\2\u0125\u0128\58\35\2"+
		"\u0126\u0127\7\6\2\2\u0127\u0129\5\66\34\2\u0128\u0126\3\2\2\2\u0128\u0129"+
		"\3\2\2\2\u0129\67\3\2\2\2\u012a\u012d\5:\36\2\u012b\u012d\5<\37\2\u012c"+
		"\u012a\3\2\2\2\u012c\u012b\3\2\2\2\u012d9\3\2\2\2\u012e\u0135\7)\2\2\u012f"+
		"\u0135\7,\2\2\u0130\u0132\7\17\2\2\u0131\u0130\3\2\2\2\u0131\u0132\3\2"+
		"\2\2\u0132\u0133\3\2\2\2\u0133\u0135\7+\2\2\u0134\u012e\3\2\2\2\u0134"+
		"\u012f\3\2\2\2\u0134\u0131\3\2\2\2\u0135;\3\2\2\2\u0136\u0137\t\7\2\2"+
		"\u0137=\3\2\2\2\u0138\u013a\7\35\2\2\u0139\u013b\5$\23\2\u013a\u0139\3"+
		"\2\2\2\u013a\u013b\3\2\2\2\u013b\u0140\3\2\2\2\u013c\u013d\7\6\2\2\u013d"+
		"\u013f\5$\23\2\u013e\u013c\3\2\2\2\u013f\u0142\3\2\2\2\u0140\u013e\3\2"+
		"\2\2\u0140\u0141\3\2\2\2\u0141\u0143\3\2\2\2\u0142\u0140\3\2\2\2\u0143"+
		"\u0144\7\36\2\2\u0144?\3\2\2\2\u0145\u0147\5> \2\u0146\u0145\3\2\2\2\u0147"+
		"\u014a\3\2\2\2\u0148\u0146\3\2\2\2\u0148\u0149\3\2\2\2\u0149\u014b\3\2"+
		"\2\2\u014a\u0148\3\2\2\2\u014b\u014c\7\2\2\3\u014cA\3\2\2\2.CFM`hlpty"+
		"~\u0082\u0088\u008d\u0092\u0094\u0097\u009c\u00a5\u00aa\u00ad\u00b1\u00b3"+
		"\u00ba\u00be\u00c3\u00c6\u00cb\u00ce\u00d5\u00e0\u00e7\u00f5\u0103\u0105"+
		"\u010e\u0116\u011c\u0128\u012c\u0131\u0134\u013a\u0140\u0148";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}