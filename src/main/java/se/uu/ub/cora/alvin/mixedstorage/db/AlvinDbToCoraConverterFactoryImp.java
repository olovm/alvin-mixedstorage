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

import se.uu.ub.cora.alvin.mixedstorage.NotImplementedException;
import se.uu.ub.cora.sqldatabase.DataReader;

public class AlvinDbToCoraConverterFactoryImp implements AlvinDbToCoraConverterFactory {

	public static AlvinDbToCoraConverterFactoryImp usingDataReader(DataReader dataReader) {
		return new AlvinDbToCoraConverterFactoryImp(dataReader);
	}

	private DataReader dataReader;

	private AlvinDbToCoraConverterFactoryImp(DataReader dataReader) {
		this.dataReader = dataReader;
	}

	@Override
	public AlvinDbToCoraConverter factor(String type) {
		if ("user".equals(type)) {
			return AlvinDbToCoraUserConverter.usingDataReader(dataReader);
		}
		throw NotImplementedException.withMessage("No converter implemented for: " + type);
	}

}
