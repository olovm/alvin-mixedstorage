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
package se.uu.ub.cora.alvin.mixedstorage.fedora;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class AlvinCoraToFedoraConverterSpy implements AlvinCoraToFedoraConverter {

	public DataGroup record;
	public String returnedXML = "<dummy>Dummy xml from AlvinCoraToFedoraConverterSpy</dummy>";
	public String returnedNewXML = "<dummy>Dummy new xml from AlvinCoraToFedoraConverterSpy</dummy>";

	@Override
	public String toXML(DataGroup record) {
		this.record = record;
		return returnedXML;
	}

	@Override
	public String toNewXML(DataGroup record) {
		this.record = record;
		return returnedNewXML;
	}

}
