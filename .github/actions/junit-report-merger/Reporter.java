import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Reporter {
	private static final Path POM_FILE = Path.of("pom.xml");
	private static final Path SUREFIRE_REPORTS_DIR = Path.of("target", "surefire-reports");
	private static final Path RESULT_FILE = Path.of("test-output", "results.xml");

	private static final String TESTSUITE = "testsuite";
	private static final String FAILURE = "failure";
	private static final Set<String> COPIED_ELEMENTS = Set.of(TESTSUITE, "testcase", FAILURE);

	private static final XMLInputFactory INPUT_FACTORY = createInputFactory();
	private static final XMLEventFactory EVENT_FACTORY = XMLEventFactory.newInstance();

	private record Project(String name, String version) {}

	private record Report(List<XMLEvent> events, BigDecimal time, int tests, int failures) {}

	public static void main(final String[] args) throws IOException, XMLStreamException, ParserConfigurationException, SAXException {
		final List<Report> reports = readReports(SUREFIRE_REPORTS_DIR);
		Files.createDirectories(RESULT_FILE.getParent());
		writeReport(RESULT_FILE, readProject(POM_FILE), reports);
		System.out.println("The report is ready!");
	}

	private static XMLInputFactory createInputFactory() {
		final XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		//keep failure stack traces in a single event even when they contain entities
		factory.setProperty(XMLInputFactory.IS_COALESCING, true);
		return factory;
	}

	private static List<Report> readReports(final Path folder) throws IOException {
		if(!Files.isDirectory(folder)) {
			return List.of();
		}
		final List<Path> files;
		try(Stream<Path> paths = Files.list(folder)) {
			files = paths.filter(path -> path.getFileName().toString().endsWith(".xml")).sorted().toList();
		}
		final List<Report> reports = new ArrayList<>();
		for(final Path file : files) {
			try {
				reports.add(readReport(file));
			}
			catch(final IOException | XMLStreamException e) {
				System.err.printf("Unable to read report %s%n", file);
				e.printStackTrace();
			}
		}
		return reports;
	}

	private static Report readReport(final Path file) throws IOException, XMLStreamException {
		final List<XMLEvent> events = new ArrayList<>();
		BigDecimal time = BigDecimal.ZERO;
		int tests = 0;
		int failures = 0;
		boolean inFailure = false;

		try(InputStream input = Files.newInputStream(file)) {
			final XMLEventReader reader = INPUT_FACTORY.createXMLEventReader(input);
			try {
				while(reader.hasNext()) {
					final XMLEvent event = reader.nextEvent();
					if(event.isStartElement()) {
						final StartElement element = event.asStartElement();
						final String name = element.getName().getLocalPart();
						if(TESTSUITE.equals(name)) {
							time = time.add(new BigDecimal(getAttribute(element, "time", "0")));
							tests += Integer.parseInt(getAttribute(element, "tests", "0"));
							failures += Integer.parseInt(getAttribute(element, "failures", "0"));
						}
						if(COPIED_ELEMENTS.contains(name)) {
							events.add(event);
							inFailure = FAILURE.equals(name);
						}
					}
					else if(event.isEndElement()) {
						final String name = event.asEndElement().getName().getLocalPart();
						if(COPIED_ELEMENTS.contains(name)) {
							events.add(event);
							inFailure = false;
						}
					}
					else if(inFailure && event.isCharacters()) {
						events.add(event);
					}
				}
			}
			finally {
				reader.close();
			}
		}
		return new Report(events, time, tests, failures);
	}

	private static String getAttribute(final StartElement element, final String name, final String defaultValue) {
		final Attribute attribute = element.getAttributeByName(new QName(name));
		return attribute != null ? attribute.getValue() : defaultValue;
	}

	private static void writeReport(final Path file, final Project project, final List<Report> reports) throws IOException, XMLStreamException {
		final BigDecimal time = reports.stream().map(Report::time).reduce(BigDecimal.ZERO, BigDecimal::add);
		final int tests = reports.stream().mapToInt(Report::tests).sum();
		final int failures = reports.stream().mapToInt(Report::failures).sum();
		final XMLEvent endLine = EVENT_FACTORY.createSpace("\n");

		try(OutputStream output = Files.newOutputStream(file)) {
			final XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output, "UTF-8");
			try {
				writer.add(EVENT_FACTORY.createStartDocument("UTF-8"));
				writer.add(endLine);

				//wrap all test suites in a single root element
				writer.add(EVENT_FACTORY.createStartElement("", "", "testsuites"));
				writer.add(EVENT_FACTORY.createAttribute("name", project.name()));
				writer.add(EVENT_FACTORY.createAttribute("version", project.version()));
				writer.add(EVENT_FACTORY.createAttribute("time", time.stripTrailingZeros().toPlainString()));
				writer.add(EVENT_FACTORY.createAttribute("tests", String.valueOf(tests)));
				writer.add(EVENT_FACTORY.createAttribute("failures", String.valueOf(failures)));
				writer.add(EVENT_FACTORY.createAttribute("date", LocalDate.now().toString()));
				writer.add(endLine);

				for(final Report report : reports) {
					for(final XMLEvent event : report.events()) {
						writer.add(event);
						if(event.isEndElement()) {
							writer.add(endLine);
						}
					}
				}

				writer.add(EVENT_FACTORY.createEndElement("", "", "testsuites"));
				writer.add(endLine);
				writer.add(EVENT_FACTORY.createEndDocument());
			}
			finally {
				writer.close();
			}
		}
	}

	private static Project readProject(final Path pom) throws ParserConfigurationException, IOException, SAXException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final Element project = factory.newDocumentBuilder().parse(pom.toFile()).getDocumentElement();
		return new Project(getChildText(project, "name"), getChildText(project, "version"));
	}

	private static String getChildText(final Element parent, final String name) {
		for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if(child instanceof final Element element && name.equals(element.getTagName())) {
				return element.getTextContent();
			}
		}
		throw new IllegalArgumentException("Missing element %s in %s".formatted(name, POM_FILE));
	}
}
