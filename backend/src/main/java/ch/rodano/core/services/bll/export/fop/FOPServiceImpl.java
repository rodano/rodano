package ch.rodano.core.services.bll.export.fop;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections4.MapUtils;
import org.apache.fop.apps.FopFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Profile("!migration")
@Service
public class FOPServiceImpl implements FOPService {

	private Transformer getTransformer() throws TransformerConfigurationException {
		final var transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		return transformer;
	}

	@Override
	public void writePDF(final OutputStream out, final Document doc, final InputStream xsl, final Map<String, String> parameters) {

		//debug
		/*try {
			final Transformer printTransformer = TransformerFactory.newInstance().newTransformer();
			printTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
			printTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			printTransformer.transform(new DOMSource(doc), new StreamResult(System.out));
		}
		catch(IllegalArgumentException | TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		}*/
		//end of debug

		try {
			final var b = new ByteArrayOutputStream();
			final var source = new DOMSource(doc);
			final var result = new StreamResult(b);
			getTransformer().transform(source, result);

			final String xml = b.toString(StandardCharsets.UTF_8);

			final var uri = FOPServiceImpl.class.getResource("/documentation/fop.xml").toURI();
			final FopFactory fopFactory = FopFactory.newInstance(uri);
			final var fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), out);

			// Create transformer
			final var factory = TransformerFactory.newInstance();
			final Transformer transformer;

			// Transformer from xsl
			if(xsl != null) {
				transformer = factory.newTransformer(new StreamSource(xsl));

				// Add xsl parameters
				if(MapUtils.isNotEmpty(parameters)) {
					parameters.forEach(transformer::setParameter);
				}
			}
			else {
				// Basic transformer
				transformer = factory.newTransformer();
			}

			final Source src = new StreamSource(new StringReader(xml));
			final Result res = new SAXResult(fop.getDefaultHandler());

			transformer.transform(src, res);
		}
		catch(final Exception e) {
			throw new RuntimeException("Unable to produce a PDF from the document using FOP", e);
		}
	}
}
