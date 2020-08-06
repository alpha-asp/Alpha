package at.ac.tuwien.kr.alpha;

//import at.ac.tuwien.kr.alpha.api.Alpha;
/**
 * Copyright (c) 2019, the Alpha Team.
 */
public class TermsAnonymizerLTE {
/*
	private final HashMap<String, String> anonymizedOrderIds = new HashMap<>();
	private final HashMap<String, String> anonymizedTrackIds = new HashMap<>();
	private final HashMap<String, String> anonymizedStationCodes = new HashMap<>();
	private final HashMap<String, String> anonymizedLocomotiveId = new HashMap<>();

	public TermsAnonymizerLTE() {
	}

	private String getAnonymized(HashMap<String, String> anonMap, String toAnonymize, String anonPrefix) {
		String anonymized = anonMap.get(toAnonymize);
		if (anonymized == null) {
			anonymized = anonPrefix + anonMap.size();
		}
		anonMap.put(toAnonymize, anonymized);
		return anonymized;
	}

	private String getAnonymizedOrderId(String orderId) {
		return getAnonymized(anonymizedOrderIds, orderId, "order");
	}

	private String getAnonymizedTrackId(String trackId) {
		return getAnonymized(anonymizedTrackIds, trackId, "track");
	}

	private String getAnonymizedStationCode(String stationCode) {
		return getAnonymized(anonymizedStationCodes, stationCode, "station");
	}

	private String getAnonymizedLocoId(String locoId) {
		return getAnonymized(anonymizedLocomotiveId, locoId, "loco");
	}

	@Ignore
	@Test
	public void rewriteSpecific() throws IOException {
		Alpha alpha = new Alpha();
		String fileDir = "/home/as/projects/dynacon/lte/scripts/prepro/workdir/";

		initAnonymizer(fileDir + "kw25_19.asp");

		InputConfig inputConfig = new InputConfig();
		inputConfig.setFiles(Arrays.asList(fileDir + "kw21_19.asp", fileDir + "kw23_19.asp",
			fileDir + "kw24_19.asp", fileDir + "kw25_19.asp", fileDir + "kw26_19.asp",
			fileDir + "kw27_19.asp", fileDir + "kw28_19.asp", fileDir + "kw29_19.asp"));
		Program program = alpha.readProgram(inputConfig);
		FileWriter fw = new FileWriter("/home/as/projects/dynacon/lte/scripts/loco-replace/anon_plans_kw21-29_19.asp");
		fw.append("% Orders/Planned movements are: track(orderId, trackId, originStation, destinationStation, plannedDeparture, plannedArrival, actualDeparture, actualArrival, firstLocomotiveId, numberOfLocomotives).\n");
		fw.append("% Locomotive jobs are: locojob(orderId, trackId, originStation, destinationStation, plannedDeparture, plannedArrival, lovomotiveId, numberOfLocomotives).\n");
		fw.append("% Locomotives are: loco(locomotiveId, allowedCountries).\n");

		for (Atom fact : program.getFacts()) {
			if (fact.getPredicate().getName().equals("trackBEU")) {
				String anonFact = trackBEU(fact);
				System.out.println("% " + fact);
				fw.append(anonFact).append("\n");
			}
			if (fact.getPredicate().getName().equals("locojobC")) {
				String anonFact = locojobC(fact);
				//System.out.println("% " + fact);
				fw.append(anonFact).append("\n");

			}
			if (fact.getPredicate().getName().equals("locoC")) {
				String anonFact = locoC(fact);
				//System.out.println("% " + fact);
				fw.append(anonFact).append("\n");

			}
		}
		fw.close();
	}

	private void initAnonymizer(String file) throws IOException {
		InputConfig inputConfig = new InputConfig();
		inputConfig.setFiles(Collections.singletonList(file));
		Alpha alpha = new Alpha();
		Program program = alpha.readProgram(inputConfig);
		for (Atom fact : program.getFacts()) {
			if (fact.getPredicate().getName().equals("trackBEU")) {
				String anonFact = trackBEU(fact);
			}
			if (fact.getPredicate().getName().equals("locojobC")) {
				String anonFact = locojobC(fact);

			}
			if (fact.getPredicate().getName().equals("locoC")) {
				String anonFact = locoC(fact);
			}
		}
	}

	private String locoC(Atom fact) {

		// columns [Locomotive No, Allowed countries, Alias, Owner]
		Term locoId = fact.getTerms().get(0);
		Term allowedCountries = fact.getTerms().get(1);
		return "loco(" +
			getAnonymizedLocoId(getStringFromConstantTerm(locoId)) + ", " +
			allowedCountries + ").";

	}

	private String locojobC(Atom fact) {
		// columns [Track+OrderNo, OriginLocationCode, DestinationLocationCode,
		// TractionLocomotiveCount, FirstLocomotiveNo, PlannedDeparture, PlannedArrival,
		// InfraManager]
		Term orderPlusTrackId = fact.getTerms().get(0);
		Term originStationCode = fact.getTerms().get(1);
		Term destinationStationCode = fact.getTerms().get(2);
		Term locoCount = fact.getTerms().get(3);
		Term firstLocoId = fact.getTerms().get(4);
		Term plannedDeparture = fact.getTerms().get(5);
		Term plannedArrival = fact.getTerms().get(6);

		String orderTrackString = getStringFromConstantTerm(orderPlusTrackId);
		TrackOrderId trackOrderId = new TrackOrderId(orderTrackString);
		String orderId = trackOrderId.orderId;
		String trackId = trackOrderId.trackId;

		return "locojob(" +
			getAnonymizedOrderId(orderId) + ", " +
			getAnonymizedTrackId(trackId) +	", " +
			getAnonymizedStationCode(getStringFromConstantTerm(originStationCode)) + ", " +
			getAnonymizedStationCode(getStringFromConstantTerm(destinationStationCode)) + ", " +
			plannedDeparture + ", " +
			plannedArrival + ", " +
			getAnonymizedLocoId(getStringFromConstantTerm(firstLocoId)) + ", " +
			locoCount + ").";
	}

	private String trackBEU(Atom fact) {
		//  columns [Track+OrderNo, OriginLocationName, OriginLocationCode, DestinationLocationName,
		//  DestinationLocationCode, PlannedDeparture, PlannedArrival, ActualDeparture, ActualArrival,
		//  TransportType, InfraManager, FirstLocomotiveNo, TractionLocomotiveCount, FirstDutyEmployee, FirstDutyActivityEN, Employee No]
		Term orderPlusTrackId = fact.getTerms().get(0);
		Term originStationCode = fact.getTerms().get(2);
		Term destinationStationCode = fact.getTerms().get(4);
		Term plannedDeparture = fact.getTerms().get(5);
		Term plannedArrival = fact.getTerms().get(6);
		Term actualDeparture = fact.getTerms().get(7);
		Term actualArrival = fact.getTerms().get(8);
		Term firstLocoId = fact.getTerms().get(11);
		Term locoCount = fact.getTerms().get(12);

		String orderTrackString = getStringFromConstantTerm(orderPlusTrackId);
		TrackOrderId trackOrderId = new TrackOrderId(orderTrackString);
		String orderId = trackOrderId.orderId;
		String trackId = trackOrderId.trackId;

		return "track(" +
			getAnonymizedOrderId(orderId) + ", " +
			getAnonymizedTrackId(trackId) +	", " +
			getAnonymizedStationCode(getStringFromConstantTerm(originStationCode)) + ", " +
			getAnonymizedStationCode(getStringFromConstantTerm(destinationStationCode)) + ", " +
			plannedDeparture + ", " +
			plannedArrival + ", " +
			actualDeparture + ", " +
			actualArrival +	", " +
			getAnonymizedLocoId(getStringFromConstantTerm(firstLocoId)) + ", " +
			locoCount + ").";
	}

	private String getStringFromConstantTerm(Term term) {
		return (String)(((ConstantTerm)term).getObject());
	}

	private static class TrackOrderId {
		String orderId;
		String trackId;

		TrackOrderId(String orderTrackString) {
			String orderTrackSanitized = orderTrackString.replaceAll("//", "/");
			String[] trackOrderId = orderTrackSanitized.split("/");
			// Split order id from track id (may contain multiple '/' )
			if (trackOrderId.length == 3) {
				// Track contains /
				trackId = trackOrderId[0] + "/" + trackOrderId[1];
				orderId = trackOrderId[2];
			} else if (trackOrderId.length == 2) {
				trackId = trackOrderId[0];
				orderId = trackOrderId[1];
			} else if (trackOrderId.length == 4 &&
				trackOrderId[0].equals("HU") &&
				trackOrderId[2].equals("7712 LKW 5") && trackOrderId[3].equals("7 HU")) {
				// Special case where order also contains /
				trackId = "HU/" + trackOrderId[1];
				orderId = "7712 LKW 5/7 HU";
			} else {
				throw new RuntimeException("Unexpected orderPlusTrackId encountered: " + orderTrackString);
			}
		}
	}
 */
}
