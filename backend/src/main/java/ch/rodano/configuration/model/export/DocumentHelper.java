package ch.rodano.configuration.model.export;

import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class DocumentHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentHelper.class);

	public static Document createDocument() {
		try {
			final var factory = DocumentBuilderFactory.newInstance();
			final var builder = factory.newDocumentBuilder();
			return builder.newDocument();
		}
		catch(final ParserConfigurationException e) {
			throw new RuntimeException("Unable to create a document", e);
		}
	}

	public static Element createElement(final Document document, final String tag) {
		return document.createElement(tag);
	}

	public static Element createElement(final Document document, final String tag, final Map<String, String> attributes) {
		final var element = createElement(document, tag);
		attributes.forEach(element::setAttribute);
		return element;
	}

	public static Element createElement(final Document document, final String tag, final String text) {
		final var element = createElement(document, tag);
		element.setTextContent(StringUtils.defaultString(text));
		return element;
	}

	public static Element createElement(final Document document, final String tag, final Object object) {
		final var element = createElement(document, tag);
		final var text = object != null ? String.valueOf(object) : "";
		element.setTextContent(text);
		return element;
	}

	public static Element appendSimpleChildren(final Element element, final Map<String, String> children) {
		children.entrySet().stream()
		.map(e -> createElement(element.getOwnerDocument(), e.getKey(), e.getValue()))
		.forEach(e -> element.appendChild(e));
		return element;
	}

	public static Node textAsNode(final Document document, final String tag, final String text) {
		//try to parse the text as XML content
		try {
			final var content = String.format("<%1$s>%2$s</%1$s>", tag, text);
			final var parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final var doc = parser.parse(new InputSource(new StringReader(content)));
			final var node = doc.getFirstChild();
			return document.importNode(node, true);
		}
		//fallback on raw text if parsing fails
		catch(final Exception e) {
			LOGGER.info(String.format("Unable to parse text: %s as valid XML", text));
			final var element = document.createElement(tag);
			element.setTextContent(StringUtils.defaultString(text));
			return element;
		}
	}
}
