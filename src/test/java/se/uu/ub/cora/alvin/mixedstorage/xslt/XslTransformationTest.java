package se.uu.ub.cora.alvin.mixedstorage.xslt;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import se.uu.ub.cora.alvin.mixedstorage.parse.ParseException;
import se.uu.ub.cora.alvin.mixedstorage.resource.ResourceReader;

public class XslTransformationTest {

	private static final String XSLT_FEDORA_TO_CORA_PLACE = "xslt/AlvinFedoraToCoraPlace.xsl";
	private static final String XML_FEDORA_PLACE = "place/xmlFedoraAlvinPlace_679.xml";
	private static final String XML_CORA_PLACE = "place/xmlCoraAlvinPlace_679.xml";

	@Test
	public void testInitWithPXslt() throws Exception {
		XsltTransformation xsltTransformation = getXsltTransformation();
		String usedXslt = xsltTransformation.getXslt();
		String xsltFedoraToCoraPlace = ResourceReader
				.readResourceAsString(XSLT_FEDORA_TO_CORA_PLACE);
		assertEquals(usedXslt, xsltFedoraToCoraPlace);
	}

	private XsltTransformation getXsltTransformation() {
		XsltTransformation xsltTransformation = new XsltTransformation(XSLT_FEDORA_TO_CORA_PLACE);
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

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Unable to read resource to string for file: path/not/found.xls")
	public void testExceptionThrownCannotReadXsltFile() throws Exception {
		new XsltTransformation("path/not/found.xls");
	}

}
