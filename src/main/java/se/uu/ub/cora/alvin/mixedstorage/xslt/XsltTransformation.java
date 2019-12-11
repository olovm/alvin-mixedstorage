package se.uu.ub.cora.alvin.mixedstorage.xslt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

public class XsltTransformation {

	private String xslt;

	public XsltTransformation(Path xsltPath) {

		try {
			this.xslt = Files.readString(xsltPath, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw ParseException.withMessageAndException(
					"Error converting place to Cora place: Can not read xslt file with path "
							+ xsltPath.toString(),
					exception);
		}
	}

	public String transform(String inputXml) {
		return tryToTrasnform(inputXml);
	}

	private String tryToTrasnform(String inputXml) {
		try {
			return transformXmlUsingXslt(inputXml).trim();
		} catch (Exception e) {
			throw ParseException.withMessageAndException(
					"Error converting place to Cora place: Can not read xml: " + e.getCause(), e);
		}
	}

	private String transformXmlUsingXslt(String xmlFromFedora) throws Exception {
		Transformer transformer = generateTransformer();
		return transformUsingTransformer(xmlFromFedora, transformer);
	}

	private Transformer generateTransformer() throws Exception {
		Source xslInput = new StreamSource(new StringReader(xslt));
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

	String getXslt() {
		return this.xslt;
	}
}
