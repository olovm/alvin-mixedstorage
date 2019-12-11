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
package se.uu.ub.cora.alvin.mixedstorage.fedora;

import java.nio.file.Path;

import se.uu.ub.cora.alvin.mixedstorage.xslt.XsltTransformation;
import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;

public class AlvinFedoraToCoraPlaceConverter implements AlvinFedoraToCoraConverter {

	private static final String XSLT_PATH = "./src/main/resources/xslt/AlvinFedoraToCoraPlace.xsl";

	@Override
	public DataGroup fromXML(String xmlToTransform) {
		XsltTransformation xsltTransformation = new XsltTransformation(Path.of(XSLT_PATH));
		String coraXml = xsltTransformation.transform(xmlToTransform);
		return convertXMLToDataElement(coraXml);
	}

	private DataGroup convertXMLToDataElement(String xmlString) {
		Converter converter = ConverterProvider.getConverter("xml");
		return (DataGroup) converter.convert(xmlString);
	}
}
