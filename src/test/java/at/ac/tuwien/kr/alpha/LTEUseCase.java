package at.ac.tuwien.kr.alpha;

/**
 * Copyright (c) 2019, the Alpha Team.
 */
public class LTEUseCase {

	/*private static final String TON_DELIMITER = "/";
	private static String[] decodeTrackOrderNo(String ton) {
		String[] ret = new String[3];
		int firstDelimIdx = ton.indexOf(TON_DELIMITER);
		int secondDelimIdx = ton.indexOf(TON_DELIMITER, firstDelimIdx + 1);
		if (firstDelimIdx == -1 || secondDelimIdx == -1) {
			throw new IllegalArgumentException("String " + ton + " does not seem to be a valid TrackOrderNo!");
		}
		String countryCode = ton.substring(0, firstDelimIdx);
		String trackId = ton.substring(firstDelimIdx + 1, secondDelimIdx);
		String orderId = ton.substring(secondDelimIdx + 1, ton.length());
		ret[0] = countryCode;
		ret[1] = trackId;
		ret[2] = orderId;
		return ret;
	}


	//@at.ac.tuwien.kr.alpha.api.externals.Predicate
	public static Set<List<ConstantTerm<String>>> ext_lte_decode_ton(String ton) {
		String[] decoded;
		try {
			decoded = decodeTrackOrderNo(ton);
		} catch (IllegalArgumentException ex) {
			//LOGGER.warn("Not a valid TrackOrderNo: {}", ton);
			return Collections.emptySet();
		}
		Set<List<ConstantTerm<String>>> retVal = new HashSet<>();
		List<ConstantTerm<String>> terms = new ArrayList<>();
		terms.add(ConstantTerm.getInstance(decoded[0]));
		terms.add(ConstantTerm.getInstance(decoded[1]));
		terms.add(ConstantTerm.getInstance(decoded[2]));
		retVal.add(terms);
		return retVal;
	}


	@Ignore
	@Test
	public void main() throws IOException, InterruptedException {
		//Thread.sleep(5000);
		String rules = "locomotive_ton(L,T) :- locomotive_for_ton(L,T,F)." +
			"locomotive(L) :- locomotive_ton(L,T)." +
			"loco_in_service(L,From,To,Dstart,Dend) :- locomotive_ton(L,T), segment_fields(T, origin(From,Dstart),destination(To,Dend),_,_,_,_)." +
			//"multi_locos(T, L1, L2) :- locomotive_ton(L1,T), locomotive_ton(L2,T), L1 != L2, L1 < L2." +
			//"triple_locos(T, L1, L2, L3) :- locomotive_ton(L1,T), locomotive_ton(L2,T), L1 < L2, locomotive_ton(L3,T), L2 < L3."
			"";

		//String fileDir = "/home/as/projects/dynacon/lte/scripts/prepro/workdir/";
		String inputScheduleFile = "/home/as/projects/dynacon/lte/asp/lte-kw49.sched.out.asp";
		InputConfig inputConfig = InputConfig.forString(rules);
		inputConfig.setFiles(singletonList(inputScheduleFile));
		HashSet<String> filter = new HashSet<>(Arrays.asList("segment_fields", "loco_in_service", "transport_order", "locomotive_ton", "multi_locos", "locomotive", "triple_locos"));
		inputConfig.setDesiredPredicates(filter);

		SystemConfig systemConfig = new SystemConfig();

		Alpha alpha = new Alpha(systemConfig);

		Program program = alpha.readProgram(inputConfig);
		Stream<AnswerSet> solve = alpha.solve(program, inputConfig.getFilter());

		AtomicInteger counter = new AtomicInteger(0);
		solve.forEach(as -> System.out.println("Answer set " + counter.incrementAndGet() + ":" + System.lineSeparator() + prettyPrint(as)));
	}

	private String prettyPrint(AnswerSet answerSet) {
		StringBuilder buf = new StringBuilder();
		for (Predicate predicate : answerSet.getPredicates()) {
			for (Atom atom : answerSet.getPredicateInstances(predicate)) {
				buf.append(atom);
				buf.append(".");
				buf.append(System.lineSeparator());
			}
		}
		return buf.toString();
	}


	@Ignore
	@Test
	public void computeReplacement() throws IOException, InterruptedException {
	//	Map<String, PredicateInterpretation> externals = Externals.scan(LTEUseCase.class);

		// schedule_item(TON, ORIGIN, ORIGIN_CODE, DESTINATION, DESTINATION_CODE, DEPTIME, ARRTIME, TRANSPORT_TYPE, INFRA_MANAGER, LOCO_ID)

		String[] inputFiles = {
			"benchmarks/lte/stdformat/kw49_schedule.as",
			"benchmarks/lte/country_inframanager.asp",
			"benchmarks/lte/train_classes.asp",
			"benchmarks/lte/train_replacement.asp",
			"benchmarks/lte/connections.asp"};
		InputConfig inputConfig = new InputConfig();
	//	inputConfig.addPredicateMethods(externals);
		inputConfig.setFiles(Arrays.asList(inputFiles));
		HashSet<String> filter = new HashSet<>(Arrays.asList(
			"replacement_candidate",
			"replacement_not_allowed_in_country",
			"replacement_quality_red",
			"replacement_quality_yellow",
			"replacement_quality_green"));
		inputConfig.setDesiredPredicates(filter);

		SystemConfig systemConfig = new SystemConfig();

		Alpha alpha = new Alpha(systemConfig);

		Program program = alpha.readProgram(inputConfig);
		Stream<AnswerSet> solve = alpha.solve(program, inputConfig.getFilter());

		AtomicInteger counter = new AtomicInteger(0);
		solve.forEach(as -> System.out.println("Answer set " + counter.incrementAndGet() + ":" + System.lineSeparator() + prettyPrint(as)));
	}


	@Ignore
	@Test
	public void transformRelations() throws IOException, InterruptedException {
	//	Map<String, PredicateInterpretation> externals = Externals.scan(LTEUseCase.class);

		// schedule_item(TON, ORIGIN, ORIGIN_CODE, DESTINATION, DESTINATION_CODE, DEPTIME, ARRTIME, TRANSPORT_TYPE, INFRA_MANAGER, LOCO_ID)

		String rules = "tu22eu_decoded(ton(TON_country,TON_track,TON_order),From,To,Deptime,Arrtime,Loco) :-" +
			"tu22eu(TONstr, From,To,NumLocos,Loco,Deptime_str,Arrtime_str,InfraManager)," +
			"&ext_lte_decode_ton[TONstr](TON_country, TON_track, TON_order)," +
			"&stdlib_datetime_parse[Deptime_str, \"dd,MM,yyyy HH:mm:ss\"](DEPTIME_YEAR, DEPTIME_MONTH, DEPTIME_DAY, DEPTIME_HOUR, DEPTIME_MIN, DEPTIME_SEC), Deptime=date(DEPTIME_YEAR, DEPTIME_MONTH, DEPTIME_DAY, DEPTIME_HOUR, DEPTIME_MIN, DEPTIME_SEC)," +
			"&stdlib_datetime_parse[Arrtime_str, \"dd,MM,yyyy HH:mm:ss\"](ARRTIME_YEAR, ARRTIME_MONTH, ARRTIME_DAY, ARRTIME_HOUR, ARRTIME_MIN, ARRTIME_SEC), Arrtime=date(ARRTIME_YEAR, ARRTIME_MONTH, ARRTIME_DAY, ARRTIME_HOUR, ARRTIME_MIN, ARRTIME_SEC)." +
			"loco_with_duty(Loco) :- tu22eu_decoded(TON,From,To,Start,End,Loco)." +

			"broken_from(\"91806193735-8\", date(2019,12,4,10,0,0))." +
			//"broken_from(\"91806193847-1\", date(2019,12,4,10,0,0))." +

			"needs_replacement(TON,From,To,Deptime,Arrtime,Loco) :- broken_from(Loco, date(Bt_y,Bt_m,Bt_d,Bt_h,Bt_min,Bt_s)), tu22eu_decoded(TON,From,To,Deptime,date(At_y,At_m,At_d,At_h,At_min,At_s),Loco)," +
			"Brokentime=date(Bt_y,Bt_m,Bt_d,Bt_h,Bt_min,Bt_s), Arrtime=date(At_y,At_m,At_d,At_h,At_min,At_s), &stdlib_datetime_is_before[Bt_y,Bt_m,Bt_d,Bt_h,Bt_min,Bt_s,At_y,At_m,At_d,At_h,At_min,At_s]." +

			"infra_manager(InfraManager) :- tu22eu(TONstr, From,To,NumLocos,Loco,Deptime_str,Arrtime_str,InfraManager)." +
			"country_codes(B) :- tu25(A,B,C,D)." +
			"dutylocoCC(Loco,CC) :- loco_with_duty(Loco), tu25(Loco,CC,_,_)." +
			"";

		//String fileDir = "/home/as/projects/dynacon/lte/scripts/prepro/workdir/";
		String inputScheduleFile = "benchmarks/lte/lte-kw49-2019.simple.asp";
		InputConfig inputConfig = InputConfig.forString(rules);
	//	inputConfig.addPredicateMethods(externals);
		inputConfig.setFiles(singletonList(inputScheduleFile));
		HashSet<String> filter = new HashSet<>(Arrays.asList("loco_with_duty", "broken_from", "needs_replacement",
			"tu25", "country_codes", "infra_manager", "tu22eu_decoded", "tu22eu", "dutylocoCC"));
		inputConfig.setDesiredPredicates(filter);

		SystemConfig systemConfig = new SystemConfig();

		Alpha alpha = new Alpha(systemConfig);

		Program program = alpha.readProgram(inputConfig);
		Stream<AnswerSet> solve = alpha.solve(program, inputConfig.getFilter());

		AtomicInteger counter = new AtomicInteger(0);
		solve.forEach(as -> System.out.println("Answer set " + counter.incrementAndGet() + ":" + System.lineSeparator() + prettyPrint(as)));
	}

	@Ignore
	@Test
	public void transformRawXslxToRelations() throws IOException {
		String rules = "hasColumn(SheetName,ColNum) :- sheet_column_name(sheet(File, SheetName), ColNum, ColName)." +
			"maxColumn(SheetName,Max) :- hasColumn(SheetName,Max), Mp1 = Max +1, not hasColumn(SheetName,Mp1)." +
			"columnName(SheetName,ColNum,ColName) :- sheet_column_name(sheet(F,SheetName),ColNum,ColName)." +
			"sheet_column_content(S,ColName,Row,DT) :- sheet_column_content(S,ColName,Row, TimeDay, TimeHour), &stdlib_string_concat[TimeDay,\" \"](TimeDayPlus), &stdlib_string_concat[TimeDayPlus,TimeHour](DT)." +
			"tu21eu(Content0,Content1,Content2,Content3,Content4,Content5,Content6,Content7,Content8,Content9,Content10,Content11,Content12,Content13,Content14,Content15) :-" +
			"SheetName=\"TU21(EU)\"," +
			"columnName(SheetName,0,ColName0), sheet_column_content(sheet(F,SheetName), ColName0, Row, Content0)," +
			"columnName(SheetName,1,ColName1), sheet_column_content(sheet(F,SheetName), ColName1, Row, Content1)," +
			"columnName(SheetName,2,ColName2), sheet_column_content(sheet(F,SheetName), ColName2, Row, Content2)," +
			"columnName(SheetName,3,ColName3), sheet_column_content(sheet(F,SheetName), ColName3, Row, Content3)," +
			"columnName(SheetName,4,ColName4), sheet_column_content(sheet(F,SheetName), ColName4, Row, Content4)," +
			"columnName(SheetName,5,ColName5), sheet_column_content(sheet(F,SheetName), ColName5, Row, Content5)," +
			"columnName(SheetName,6,ColName6), sheet_column_content(sheet(F,SheetName), ColName6, Row, Content6)," +
			"columnName(SheetName,7,ColName7), sheet_column_content(sheet(F,SheetName), ColName7, Row, Content7)," +
			"columnName(SheetName,8,ColName8), sheet_column_content(sheet(F,SheetName), ColName8, Row, Content8)," +
			"columnName(SheetName,9,ColName9), sheet_column_content(sheet(F,SheetName), ColName9, Row, Content9)," +
			"columnName(SheetName,10,ColName10), sheet_column_content(sheet(F,SheetName), ColName10, Row, Content10)," +
			"columnName(SheetName,11,ColName11), sheet_column_content(sheet(F,SheetName), ColName11, Row, Content11)," +
			"columnName(SheetName,12,ColName12), sheet_column_content(sheet(F,SheetName), ColName12, Row, Content12)," +
			"columnName(SheetName,13,ColName13), sheet_column_content(sheet(F,SheetName), ColName13, Row, Content13)," +
			"columnName(SheetName,14,ColName14), sheet_column_content(sheet(F,SheetName), ColName14, Row, Content14)," +
			"columnName(SheetName,15,ColName15), sheet_column_content(sheet(F,SheetName), ColName15, Row, Content15)." +

			"tu22eu(Content0,Content1,Content2,Content3,Content4,Content5,Content6,Content7) :-" +
			"SheetName=\"TU22(EU)\"," +
			"columnName(SheetName,0,ColName0), sheet_column_content(sheet(F,SheetName), ColName0, Row, Content0)," +
			"columnName(SheetName,1,ColName1), sheet_column_content(sheet(F,SheetName), ColName1, Row, Content1)," +
			"columnName(SheetName,2,ColName2), sheet_column_content(sheet(F,SheetName), ColName2, Row, Content2)," +
			"columnName(SheetName,3,ColName3), sheet_column_content(sheet(F,SheetName), ColName3, Row, Content3)," +
			"columnName(SheetName,4,ColName4), sheet_column_content(sheet(F,SheetName), ColName4, Row, Content4)," +
			"columnName(SheetName,5,ColName5), sheet_column_content(sheet(F,SheetName), ColName5, Row, Content5)," +
			"columnName(SheetName,6,ColName6), sheet_column_content(sheet(F,SheetName), ColName6, Row, Content6)," +
			"columnName(SheetName,7,ColName7), sheet_column_content(sheet(F,SheetName), ColName7, Row, Content7)." +

			"tu25(Content0,Content1,Content2,Content3) :-" +
			"SheetName=\"TU25\"," +
			"columnName(SheetName,0,ColName0), sheet_column_content(sheet(F,SheetName), ColName0, Row, Content0)," +
			"columnName(SheetName,1,ColName1), sheet_column_content(sheet(F,SheetName), ColName1, Row, Content1)," +
			"columnName(SheetName,2,ColName2), sheet_column_content(sheet(F,SheetName), ColName2, Row, Content2)," +
			"columnName(SheetName,3,ColName3), sheet_column_content(sheet(F,SheetName), ColName3, Row, Content3)." +


			"";

		//String fileDir = "/home/as/projects/dynacon/lte/scripts/prepro/workdir/";
		String inputScheduleFile = "/home/as/projects/dynacon/lte/asp/lte-kw49.xlsx.out.as";
		InputConfig inputConfig = InputConfig.forString(rules);
		inputConfig.setFiles(singletonList(inputScheduleFile));
		HashSet<String> filter = new HashSet<>(Arrays.asList(//"hasColumn", "maxColumn", "sheet_column_name", "columnName",
			"tu21eu", "tu22eu", "tu25"));
		inputConfig.setDesiredPredicates(filter);

		SystemConfig systemConfig = new SystemConfig();

		Alpha alpha = new Alpha(systemConfig);

		Program program = alpha.readProgram(inputConfig);
		Stream<AnswerSet> solve = alpha.solve(program, inputConfig.getFilter());

		AtomicInteger counter = new AtomicInteger(0);
		solve.forEach(as -> System.out.println("Answer set " + counter.incrementAndGet() + ":" + System.lineSeparator() + prettyPrint(as)));
	}
*/
}
