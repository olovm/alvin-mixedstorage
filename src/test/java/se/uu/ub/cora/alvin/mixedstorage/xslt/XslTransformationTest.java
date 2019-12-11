package se.uu.ub.cora.alvin.mixedstorage.xslt;

import static org.testng.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.parse.ParseException;
import se.uu.ub.cora.alvin.mixedstorage.resource.ResourceReader;

public class XslTransformationTest {

	private static final String RESOURCE_PATH = "src/main/resources/";
	private static final String XSLT_FEDORA_TO_CORA_PLACE = "xslt/AlvinFedoraToCoraPlace.xsl";
	private static final String XML_FEDORA_PLACE = "place/xmlFedoraAlvinPlace_679.xml";
	private static final String XML_CORA_PLACE = "place/xmlCoraAlvinPlace_679.xml";

	@Test
	public void testInitWithPXslt() throws Exception {
		XsltTransformation xsltTransformation = getXsltTransformation();
		String usedXslt = xsltTransformation.getXslt();
		String xsltFedoraToCoraPlace = Files.readString(
				Path.of(RESOURCE_PATH + XSLT_FEDORA_TO_CORA_PLACE), StandardCharsets.UTF_8);
		assertEquals(usedXslt, xsltFedoraToCoraPlace);
	}

	private XsltTransformation getXsltTransformation() {
		Path xsltPath = Path.of(RESOURCE_PATH + XSLT_FEDORA_TO_CORA_PLACE);
		XsltTransformation xsltTransformation = new XsltTransformation(xsltPath);
		return xsltTransformation;
	}

	@Test
	public void testSimpleTransformation() throws Exception {
		XsltTransformation xsltTransformation = getXsltTransformation();
		String inputXml = ResourceReader.readResourceAsString(XML_FEDORA_PLACE);
		String outputXml = xsltTransformation.transform(inputXml);

		String expectedOutput = ResourceReader.readResourceAsString(XML_CORA_PLACE);
		assertEquals(outputXml, expectedOutput);
	}

	@Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting place to Cora place: Can not read xml: "
			+ "javax.xml.transform.TransformerException: "
			+ "com.sun.org.apache.xml.internal.utils.WrappedRuntimeException: "
			+ "The element type \"pid\" must be terminated by the matching end-tag \"</pid>\".")
	public void parseExceptionShouldBeThrownOnMalformedXML() throws Exception {
		XsltTransformation xsltTransformation = getXsltTransformation();
		String inputXml = "<pid></notPid>";
		xsltTransformation.transform(inputXml);
	}

	@Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = "Error "
			+ "converting place to Cora place: Can not read xslt file with path path/not/found.xls")
	public void testExceptionThrownCannotReadXsltFile() throws Exception {
		new XsltTransformation(Path.of("path/not/found.xls"));
	}

}
