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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.alvin.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.alvin.mixedstorage.user.DataReaderSpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.sqldatabase.DataReader;

public class AlvinDbToCoraConverterFactoryTest {
	private AlvinDbToCoraConverterFactory alvinDbToCoraConverterFactoryImp;
	private DataReader dataReader;
	private LoggerFactorySpy loggerFactorySpy;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataReader = new DataReaderSpy();
		alvinDbToCoraConverterFactoryImp = AlvinDbToCoraConverterFactoryImp
				.usingDataReader(dataReader);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorUnknownTypeThrowsException() throws Exception {
		alvinDbToCoraConverterFactoryImp.factor("someType");
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: country")
	public void testFactoryCountry() throws Exception {
		alvinDbToCoraConverterFactoryImp.factor("country");
	}

	@Test
	public void testCoraUser() throws Exception {
		AlvinDbToCoraConverter converter = alvinDbToCoraConverterFactoryImp.factor("user");
		assertTrue(converter instanceof AlvinDbToCoraUserConverter);
	}

	@Test
	public void testDataReaderInUserConverter() throws Exception {
		AlvinDbToCoraUserConverter converter = (AlvinDbToCoraUserConverter) alvinDbToCoraConverterFactoryImp
				.factor("user");

		assertEquals(converter.getDataReader(), dataReader);
	}
}
