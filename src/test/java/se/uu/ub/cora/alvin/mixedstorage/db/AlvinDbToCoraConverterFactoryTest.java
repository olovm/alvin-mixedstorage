/*
 * Copyright 2018, 2019 Uppsala University Library
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
package se.uu.ub.cora.alvin.mixedstorage.db;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.sqldatabase.DataReaderImp;

public class AlvinDbToCoraConverterFactoryTest {
	private AlvinDbToCoraConverterFactory alvinDbToCoraConverterFactoryImp;

	@BeforeMethod
	public void beforeMethod() {
		alvinDbToCoraConverterFactoryImp = new AlvinDbToCoraConverterFactoryImp();
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorUnknownTypeThrowsException() throws Exception {
		alvinDbToCoraConverterFactoryImp.factor("someType");
	}

	@Test
	public void testFactoryCountry() throws Exception {
		AlvinDbToCoraConverter converter = alvinDbToCoraConverterFactoryImp.factor("country");
		assertTrue(converter instanceof AlvinDbToCoraCountryConverter);
	}

	@Test
	public void testCoraUser() throws Exception {
		AlvinDbToCoraConverter converter = alvinDbToCoraConverterFactoryImp.factor("coraUser");
		assertTrue(converter instanceof AlvinDbToCoraUserConverter);
	}

	@Test
	public void testDataReaderInUserConverter() throws Exception {
		AlvinDbToCoraUserConverter converter = (AlvinDbToCoraUserConverter) alvinDbToCoraConverterFactoryImp
				.factor("coraUser");

		assertTrue(converter.getDataReader() instanceof DataReaderImp);
	}
}
