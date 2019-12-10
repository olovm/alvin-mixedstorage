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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import se.uu.ub.cora.alvin.mixedstorage.parse.ParseException;
import se.uu.ub.cora.converter.Converter;
import se.uu.ub.cora.converter.ConverterProvider;
import se.uu.ub.cora.data.DataGroup;

public class AlvinFedoraToCoraPlaceConverter implements AlvinFedoraToCoraConverter {

	private static final String XSLT_PATH = "./src/main/resources/xslt/AlvinFedoraToCoraPlace.xsl";

	@Override
	public DataGroup fromXML(String xmlToTransform) {
		try {
			return tryToConvertToDataGroup(xmlToTransform);
		} catch (Exception e) {
			throw ParseException.withMessageAndException(
					"Error converting place to Cora place: Can not read xml: " + e.getCause(), e);
		}
	}

	private DataGroup tryToConvertToDataGroup(String xmlFromFedora) throws Exception {
		String coraXml = transformXmlUsingXslt(xmlFromFedora);
		return convertXMLToDataElement(coraXml);
	}

	private String transformXmlUsingXslt(String xmlFromFedora) throws Exception {
		Transformer transformer = generateTransformer();
		return transformUsingTransformer(xmlFromFedora, transformer);
	}

	private Transformer generateTransformer() throws Exception {
		String alvinFedoraToCoraPlaceXslt = Files.readString(Path.of(XSLT_PATH),
				StandardCharsets.UTF_8);
		Source xslInput = new StreamSource(new StringReader(alvinFedoraToCoraPlaceXslt));
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		return transformerFactory.newTransformer(xslInput);
	}

	private String transformUsingTransformer(String xmlFromFedora, Transformer transformer)
			throws TransformerException {
		Source xmlSource = createSourceFromFedoraXml(xmlFromFedora);
		return transformUsingTransformerAndSource(transformer, xmlSource);
	}

	private Source createSourceFromFedoraXml(String xmlFromFedora) {
		StringReader stringReader = new StringReader(xmlFromFedora);
		return new StreamSource(stringReader);
	}

	private String transformUsingTransformerAndSource(Transformer transformer, Source xmlSource)
			throws TransformerException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Result outputResult = new StreamResult(output);
		transformer.transform(xmlSource, outputResult);
		return output.toString(StandardCharsets.UTF_8);
	}

	private DataGroup convertXMLToDataElement(String xmlString) {
		Converter converter = ConverterProvider.getConverter("xml");
		return (DataGroup) converter.convert(xmlString);
	}
}
