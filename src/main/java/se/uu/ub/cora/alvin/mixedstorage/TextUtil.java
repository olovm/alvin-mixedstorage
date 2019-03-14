/*
 * Copyright 2018 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.alvin.mixedstorage;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class TextUtil {

	private TextUtil() {
		throw new UnsupportedOperationException();
	}

	public static String normalizeString(String stringToNormalize) {
		return Normalizer.normalize(stringToNormalize, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	public static String turnStringIntoCamelCase(String stringToTurnIntoCamelCase) {
		String[] stringSplitByNonAlpha = stringToTurnIntoCamelCase.split("\\P{Alpha}+");

		String camelCased = Arrays.stream(stringSplitByNonAlpha)
				.map(TextUtil::turnFirstCharacterIntoUpperCase).collect(Collectors.joining(""));
		return turnFirstCharacterIntoLowerCase(camelCased);
	}

	private static final String turnFirstCharacterIntoUpperCase(String stringToModify) {
		return stringToModify.substring(0, 1).toUpperCase() + stringToModify.substring(1);
	}

	private static final String turnFirstCharacterIntoLowerCase(String stringToModify) {
		return stringToModify.substring(0, 1).toLowerCase() + stringToModify.substring(1);
	}
}
