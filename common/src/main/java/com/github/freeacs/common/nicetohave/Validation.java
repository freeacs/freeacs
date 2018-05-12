package com.github.freeacs.common.nicetohave;

/**
 * Validation er en klasse for den som skal validere noe (naturlig nok).
 * Selvsagt kan man alltids skrive denne logikken selv, men jeg tror
 * at denne klassen har såpass fleksible og kraftige metoder, slik at
 * den kan brukes i svært mange sammenhenger. Jeg håper også at dersom
 * man ser fornuftige utvidelser her, så vil man gjøre det. I tillegg
 * får man kastet ValidationExceptions dersom man ønsker det, dvs. at
 * feilmeldingen er ferdig laget, på en slik måte at det skal bli svært
 * enkelt å skjønne hva som feilet i valideringen. Les kommentarene i
 * public-metodene for å forstå hvordan man skal bruke disse. 
 * 
 * @author ME3
 */
public class Validation {
	public static final String NO_FORMAT = null;

	public static final String NUMERICAL_FORMAT = "\\d+";

	public static final String ALPHANUM_FORMAT = "\\w+";

	public static final String INTERVALL_SET = "INTERVALL";

	public static final String VERDI_SET = "VERDI";

	public static final String VERDI_UNCASE_SET = "VERDI_UNCASE";

	public Validation() {
		super();
	}

	/**
	 * Denne metoden er en av de viktigste i denne klassen. Svært mange av de andre
	 * metodene er wrapper-metoder til denne metoden. Grunnen til å bruke wrapper-
	 * metodene kan være at disse er enklere å bruke, mens denne metoden er den som
	 * gir mest fleksibilitet, på bekostning av noe mer komplisert input. Poenget med
	 * metoden er altså å kunne validere en verdi. Dette foregår omtrent slik:
	 * 
	 * 1. Finnes verdien (er den forskjellig fra null?)
	 * 2. Dersom den finnes, har den korrekt lengde?
	 * 3. Dersom den har korrekt lengde, har den et gyldig format?
	 * 4. Dersom formatet er ok, tilhører den et gyldig sett (både verdier og tall-intervall)
	 * 
	 * Hvis ikke alt dette stemmer så returneres det en korrekt utfylt ValidationException.
	 * Denne kan man velge å kaste videre dersom man f.eks. står i validateInput() i
	 * en StartTjeneste. Nedenfor følger en nokså detaljer forklaring på input-verdiene:
	 * 
	 * name:  				Navnet på verdien, slik at feilmeldingen skal kunne identifisere 
	 * 								hvilken variabel som ikke var gyldig. Oblig.
	 * value					Selve verdien som skal valideres. Oblig.
	 * validLength		2-dim array som inneholder grenser for godtatte lengder. Valgfri.
	 * 								Eks: "new int[][] {{2-3},{11,11},{20,Integer.MAX_VALUE}}" validerer
	 * 								at verdien er enten 2-3, 11 eller mer enn 19 tegn lang. Dersom man
	 * 								setter verdien til null, så godtas alle lengder.
	 * validFormat 		Bruk en av konstantene i denne klassen, eller send inn din egen regExp. 
	 * 								Valgfri. Hvis satt til null, så godtas alle formater. Se lenger ned
	 * 								for en kort forklaring om regExp.
	 * validSet 			Array som inneholder gyldige verdier eller gyldige intervaller.
	 * 								Valgfri. Dersom denne er satt til null, så godtas alle. 
	 * 								Intervall-Eks: new String[] {"INTERVALL", ",-5","200,500","1000,"} validerer at
	 * 								verdien må være mindre eller lik -5 eller fom. 200 til tom. 500 eller
	 * 								større eller lik 1000. Dersom et tegn IKKE skulle være et tall, så blir 
	 * 								alt tolket som et VERDI-Sett: (Merk også at det ikke godtas Float-tall)
	 * 								Verdi-Eks: new String[] {"VERDI", "Mandag", "Onsdag", "Fredag"}
	 * 								
	 * Et sett av godkjente verdier. 
	 * 
	 * Litt om RegExp:
	 * Det finnes masse om dette på nettet og i bøker; Her fant jeg en adresse som gir litt
	 * kompakt informasjon: http://www.contactor.se/~dast/mail2sms/regex.shtml.
	 * Poenget er at man kan lage en RegExp som uttrykker et slags "format". Noen eksempler:
	 * abc.e				matcher			abcde abcxe abcce osv... fordi .=any char
	 * 0+						matcher			0 00 000 0000 osv... fordi +=1 eller flere
	 * [a-m]+				matcher 		a b c ab mm mb osv... fordi a-m representerer hele rangen mellom a og m.
	 * @author: Morten Simonsen  
	 */
	public static ValidationException validateInput(String name, String value, boolean required, int[][] validLength, String validFormat, String[] validSet) {
		if (value == null || value.trim().length() == 0) {
			if (required)
				return ValidationException.inputValueMissing(name);
		} else {
			ValidationException se = null;
			se = validateLength(name, value, validLength);
			if (se != null)
				return se;
			se = validateFormat(name, value, validFormat);
			if (se != null)
				return se;
			se = validateSet(name, value, validSet);
			if (se != null)
				return se;
		}
		return null;
	}

	public static ValidationException validateInput(ValidationInput vi) {
		return validateInput(vi.getName(), vi.getValue(), vi.isRequired(), vi.getLength(), vi.getFormat(), vi.getSet());
	}

	public static String validateInputThrows(ValidationInput vi) throws ValidationException {
		return validateInputThrows(vi.getName(), vi.getValue(), vi.isRequired(), vi.getLength(), vi.getFormat(), vi.getSet());
	}

	/**
	 * En wrapper metode, men denne metoden sørger for å kaste en exception (istedet
	 * for å returnere en exception). Returnerer verdien som ble sjekket.
	 */
	public static String validateInputThrows(String name, String value, boolean required, int[][] validLength, String validFormat, String[] validSet)
			throws ValidationException {
		ValidationException s = validateInput(name, value, required, validLength, validFormat, validSet);
		if (s != null)
			throw s;
		else
			return value;
	}

	/**
	 * En wrapper metode, men denne metoden sørger for å kaste en exception (istedet
	 * for å returnere en exception). Returnerer verdien som ble sjekket.
	 */
	public static String validateInputThrows(String name, String value, boolean required) throws ValidationException {
		ValidationException s = validateInput(name, value, required, null, NO_FORMAT, null);
		if (s != null)
			throw s;
		else
			return value;
	}

	/**
	 * En wrapper metode, kaster exception, sjekker om lengden på verdien er OK.
	 */
	public static String validateInputThrows(String name, String value, boolean required, int minLength, int maxLength) throws ValidationException {
		return validateInputThrows(name, value, required, new int[][] { { minLength, maxLength } }, NO_FORMAT, null);
	}

	/**
	 * En wrapper metode, kaster exception, sjekker om intervallet på verdien er OK
	 */
	public static String validateInputThrows(String name, String value, boolean required, long minNumber, long maxNumber) throws ValidationException {
		return validateInputThrows(name, value, required, null, NO_FORMAT, new String[] { INTERVALL_SET, minNumber + "," + maxNumber });
	}

	/**
	 * Metoden sjekker om kombinasjonen av input er ok sett i forhold til de gyldige kombinasjoner som
	 * sendes med. Metoden er "dum" i den forstand at den ikke sjekker annet enn om en input er null eller
	 * ikke.
	 * @param input Array med forskjellige objekter. Array-str må være lik de str-length som ligger i validCombinations-array'et. 
	 * @param validCombinations Array hvor alle stringene uttrykker en gyldig kombinasjon. Eks:
	 * new String[] {"0101","1111"} betyr at enten så må andre og fjerde objekt være != null, eller så må alle være != null.
	 * @return
	 */
	public static boolean validateCombination(Object[] input, String[] validCombinations) {
		String actualCombination = "";
		for (int i = 0; i < input.length; i++) {
			if (input[i] == null)
				actualCombination += "0";
			else
				actualCombination += "1";
		}
		for (int i = 0; i < validCombinations.length; i++) {
			if (actualCombination.equals(validCombinations[i]))
				return true;
		}
		return false;
	}

	/********************* PRIVATE METODER **********************/

	/* En private-metode som gjør validering. Se kommentaren til public-metoden som kaller
	 * denne metoden.
	 */
	private static ValidationException validateLength(String name, String value, int[][] validLength) {
		boolean lengthOK = false;
		if (validLength != null) {
			int length = value.length();
			for (int i = 0; i < validLength.length; i++) {
				int[] intervall = validLength[i];
				if (length >= intervall[0] && length <= intervall[1])
					lengthOK = true;
			}
			if (!lengthOK)
				return ValidationException.inputValueWrongLength(name, validLength, value);
		}
		return null;
	}

	/* En private-metode som gjør validering. Se kommentaren til public-metoden som kaller
	 * denne metoden.
	 */
	private static ValidationException validateFormat(String name, String value, String validFormat) {
		String tekstFormat = "Custom (RegExp)";
		if (validFormat != null && validFormat.equals(NUMERICAL_FORMAT)) {
			tekstFormat = "Numerisk";
		}
		if (validFormat != null && validFormat.equals(ALPHANUM_FORMAT)) {
			tekstFormat = "Alfanumerisk";
		}
		if (validFormat != null) {
			if (!value.matches(validFormat))
				return ValidationException.inputValueWrongFormat(name, value, tekstFormat + "(" + validFormat + ")");
		}
		return null;
	}

	/* En private-metode som gjør validering. Se kommentaren til public-metoden som kaller
	 * denne metoden.
	 * Både value og elementene i validSettet trimmes før sammenligningen
	 */
	private static ValidationException validateSet(String name, String value, String[] validSet) {
		if (validSet != null) {
			boolean setOK = false;
			if (validSet[0].equals(INTERVALL_SET)) {
				for (int i = 1; i < validSet.length; i++) {
					try {
						long valueL = new Long(value).longValue();
						String[] intervall = validSet[i].split(",");
						long low = Long.MIN_VALUE;
						if (intervall[0] != null) {
							low = new Long(intervall[0]).longValue();
						}
						long high = Long.MAX_VALUE;
						if (intervall != null)
							high = new Long(intervall[1]).longValue();
						if (valueL >= low && valueL <= high)
							setOK = true;
					} catch (NumberFormatException nfe) {
						validSet[0] = VERDI_SET;
						break;
					}
				}
			} else if (validSet[0].equals(VERDI_SET)) {
				for (int i = 1; i < validSet.length; i++) {
					if (value.trim().equals(validSet[i].trim()))
						setOK = true;
				}
			} else if (validSet[0].equals(VERDI_UNCASE_SET)) {
				for (int i = 1; i < validSet.length; i++) {
					if (value.trim().equalsIgnoreCase(validSet[i].trim()))
						setOK = true;
				}
			}
			if (!setOK) {
				String[] msgSet = new String[1];
				msgSet[0] = "Ingen lovlige verdier angitt";
				if (validSet != null && validSet.length > 1) {
					msgSet = new String[validSet.length - 1];
					for (int i = 0; i < msgSet.length; i++) {
						msgSet[i] = validSet[i + 1];
					}
				}
				return ValidationException.inputValueWrongSet(name, msgSet, value);
			}
		}

		return null;
	}
}
