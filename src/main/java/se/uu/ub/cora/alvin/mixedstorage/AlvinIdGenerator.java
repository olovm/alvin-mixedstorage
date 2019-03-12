/*
 * Copyright 2019 Uppsala University Library
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

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.spider.record.storage.RecordIdGenerator;

public class AlvinIdGenerator implements RecordIdGenerator {

	public static AlvinIdGenerator usingHttpHandlerFactoryAndConnectionInfo(
			HttpHandlerFactory httpHandlerFactory,
			IdGeneratorConnectionInfo idGeneratorConnectionInfo) {
		return new AlvinIdGenerator(httpHandlerFactory, idGeneratorConnectionInfo);
	}

	private AlvinIdGenerator(HttpHandlerFactory httpHandlerFactory,
			IdGeneratorConnectionInfo idGeneratorConnectionInfo) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getIdForType(String type) {
		return type + ":" + System.nanoTime();
	}

}
