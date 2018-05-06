package com.owera.common.nicetohave;

/**
 * ValdationException blir brukt Validation-klassen. Poenget med klassen er
 * å tilby generelle valideringsexceptions, for på den måten å gi brukerene
 * de beste feilmeldinger. Imidlertid vil det være uvanlig å bruke metodene
 * i denne klassen direkte, Validation-klassen vil være det mest brukte 
 * interfacet.
 * @author: Morten Simonsen
 */

public class ValidationException extends IllegalArgumentException {

	private static final long serialVersionUID = 5871531348095192560L;

	public ValidationException(String s) {
		super(s);
	}

	/**
	 * Dersom man mangler en input-verdi, så bruker man denne metoden.
	 */
	public static ValidationException inputValueMissing(String name) {
		return new ValidationException(name + " is missing.");
	}

	/**
	 * Metoden skal brukes dersom en kombinasjon av parametre er feil.
	 */
	public static ValidationException inputValueWrongCombination(String validCombination) {
		return new ValidationException("A wrong combination of input. The valid combinations are (" + validCombination + ")");
	}

	/**
	 * Metoden skal brukes dersom verdien har feil format. Korrekt format bør helst angis som
	 * en "Numerisk" eller "Alfanumerisk" e.l.
	 */
	public static ValidationException inputValueWrongFormat(String name, String value, String validFormat) {
		return new ValidationException(name + " must follow this format: " + validFormat + ". The input was " + value);
	}

	/**
	 * Dersom input-verdien enten er for lang eller for kort, så bruker man denne metoden. 
	 * Slik representeres grensetilfellen "min,max":
	 * 1 -> uendelig : 1, Integer.MAX_VALUE
	 * 1 -> 5        : 1, 5
	 * 5             : 5, 5
	 * NB! Denne metoden validerer ikke idiotiske min/max-verdier. Dersom man skriver fra
	 * 1 til -1, så vil det stå det i feilmeldingen også, selv om dette ikke gir noen mening.
	 */
	public static ValidationException inputValueWrongLength(String name, int min, int max, String value) {
		String regel = lagLengdeRegel(min, max);
		return new ValidationException(name + " has the following limitations on length: " + regel + ". The input was " + value);
	}

	/**
	 * Dersom input-verdien enten er for lang eller for kort, så bruker man denne metoden.
	 * Imidlertid kan denne metodene brukes til å gi beskjed om et eller flere gyldige intervaller
	 * for verdien. Se kommentaren til inputValueWrongLength(.., min, max,..) for angivelse av
	 * et intervall. Dersom man har flere slike, så kan et tilfelle se slik ut:
	 * 		int[][] grenserEks = new int[][]{{3,3},{11,12}};
	 * Dette tilfellet spesifiserer at man kun godtar input med lengde 3 eller 11-12 tegn.
	 */
	public static ValidationException inputValueWrongLength(String name, int[][] grenser, String value) {
		String regel = "";
		for (int i = 0; i < grenser.length; i++) {
			int[] intervall = grenser[i];
			if (i < grenser.length - 1)
				regel += lagLengdeRegel(intervall[0], intervall[1]) + " ELLER ";
			else
				regel += lagLengdeRegel(intervall[0], intervall[1]);
		}
		return new ValidationException(name + " has the following limitations on length: " + regel + ". The input was " + value);
	}

	/**
	 * Metoden skal brukes dersom har en regel som uttrykker hva verdien bør være, SAMTIDIG som
	 * denne regelen ikke kan uttrykkes som det at verdien er for kort/lang eller har feil 
	 * format (da bruker du andre metoder).
	 */
	public static ValidationException inputValueWrongSet(String name, String rule, String value) {
		return new ValidationException(name + " can have these values: " + rule + ". The input was " + value);
	}

	/**
	 * Metoden skal brukes dersom du har et endelig (og sannsynligvis lite) sett av verdier som 
	 * er gyldige.  De første linjene i metoden er kun for å kopiere en stringarray, siden
	 * vi bruker dette arrayet for evt. å konstruere en feilmelding.
	 */
	public static ValidationException inputValueWrongSet(String name, String[] orgValidSet, String value) {
		String[] validSet = null;
		if (orgValidSet != null && orgValidSet.length > 0) {
			validSet = new String[orgValidSet.length];
			for (int i = 0; i < validSet.length; i++)
				validSet[i] = orgValidSet[i];
		}

		if (validSet[0].equals(Validation.VERDI_SET)) {
			validSet[0] = null;
		} else if (validSet[0].equals(Validation.INTERVALL_SET)) {
			for (int i = 1; i < validSet.length; i++) {
				String intervall = validSet[i];
				intervall = intervall.replace(",", "-");
				validSet[i] = intervall;
			}
		}
		String validValuesStr = StringUtility.join(validSet, ", ", false);
		return new ValidationException(name + " can have these values: " + validValuesStr + ". The input was " + value);
	}

	/* Bygger en regel-string som til syvende og sist skrives ut som en del av en feilmelding */
	private static String lagLengdeRegel(int min, int max) {
		String regel = "";
		if (max == min)
			regel += min + " tegn";
		else if (max == Integer.MAX_VALUE)
			regel += min + " tegn eller mer";
		else
			regel += min + " - " + max + " tegn";
		return regel;
	}

}
