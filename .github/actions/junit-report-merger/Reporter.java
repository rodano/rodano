import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Reporter {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private static final String POM_FILE_NAME = "pom.xml";

	private static final String SUREFIRE_REPORTS_DIR = "target/surefire-reports/";
	private static final String RESULT_FILE = "test-output/results.xml";

	private static final String TESTCASE = "testcase";
	private static final String TESTSUITE = "testsuite";
	private static final String FAILURE = "failure";

	private static final ArrayList<XMLEvent> xmlEvents = new ArrayList<>();

	private static double TotalTime = 0;
	private static int TotalTests = 0;
	private static int TotalFailures = 0;

	public static void main(String[] args) throws IOException, XMLStreamException, ParserConfigurationException, SAXException {
		//load all reports in memory
		processAllReports(SUREFIRE_REPORTS_DIR);
		//create result file
		File result = new File(RESULT_FILE);
		result.getParentFile().mkdirs();
		result.createNewFile();
		//write resulting report
		writeReport(result);
		System.out.println("The report is ready!");
	}

	private static void processAllReports(String folderName) {
		File folder = new File(folderName);
		File[] listFiles = folder.listFiles();

		if(listFiles != null) {
			Arrays.stream(listFiles)
				.filter(file -> file.getName().endsWith(".xml"))
				.sorted()
				.forEach(file -> processReport(folderName + file.getName()));
		}
	}

	private static void processReport(String reportName) {
		try {
			//Create a new XML Factory.
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			//Setup a new EventReader.
			InputStream in = new FileInputStream(reportName);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

			//Now read the xml document.
			while(eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();

				if(event.isStartElement()) {
					StartElement startElem = event.asStartElement();

					if(startElem.getName().getLocalPart().equals(TESTSUITE)) {
						Iterator attributes = startElem.getAttributes();
						while(attributes.hasNext()) {
							Attribute attr = (Attribute) attributes.next();

							if(attr.getName().getLocalPart().equals("time")) {
								TotalTime += Double.parseDouble(attr.getValue());
							}
							if(attr.getName().getLocalPart().equals("tests")) {
								TotalTests += Double.parseDouble(attr.getValue());
							}
							if(attr.getName().getLocalPart().equals("failures")) {
								TotalFailures += Double.parseDouble(attr.getValue());
							}
						}

						xmlEvents.add(startElem);
					}

					if(startElem.getName().getLocalPart().equals(TESTCASE)) {
						xmlEvents.add(startElem);
					}

					//Get the failure information if present.
					if(startElem.getName().getLocalPart().equals(FAILURE)) {
						xmlEvents.add(startElem);

						//And get the error body.
						event = eventReader.nextEvent();
						xmlEvents.add(event);
					}
				}

				if(event.isEndElement()) {
					EndElement endElem = event.asEndElement();

					if(endElem.getName().getLocalPart().equals(TESTSUITE)) {
						xmlEvents.add(endElem);
					}

					if(endElem.getName().getLocalPart().equals(TESTCASE)) {
						xmlEvents.add(endElem);
					}

					if(endElem.getName().getLocalPart().equals(FAILURE)) {
						xmlEvents.add(endElem);
					}
				}
			}
		}
		catch(FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
		}
	}

	private static void writeReport(File result) throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
		//Create XML Output Factory.
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		//Create XML Event Writer.
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(result));

		//Create an event factory.
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLEvent endLine = eventFactory.createDTD("\n");

		//Add the start of the document.
		StartDocument startDocument = eventFactory.createStartDocument("UTF-8");
		eventWriter.add(startDocument);
		eventWriter.add(endLine);

		//Wrap everything in a single root tag.
		StartElement testsuitesStartElement = eventFactory.createStartElement("", "", "testsuites");
		eventWriter.add(testsuitesStartElement);

		Map<String, String> projectInfo = getProjectInfo();
		Attribute nameAttr = eventFactory.createAttribute("name", projectInfo.get("name"));
		Attribute versionAttr = eventFactory.createAttribute("version", projectInfo.get("version"));
		eventWriter.add(nameAttr);
		eventWriter.add(versionAttr);

		Attribute totalTimeAttr = eventFactory.createAttribute("time", String.valueOf(TotalTime));
		Attribute totalTestsAttr = eventFactory.createAttribute("tests", String.valueOf(TotalTests));
		Attribute totalFailuresAttr = eventFactory.createAttribute("failures", String.valueOf(TotalFailures));
		Attribute dateAttr = eventFactory.createAttribute("date", DATE_FORMAT.format(ZonedDateTime.now()));
		eventWriter.add(totalTimeAttr);
		eventWriter.add(totalTestsAttr);
		eventWriter.add(totalFailuresAttr);
		eventWriter.add(dateAttr);

		eventWriter.add(endLine);

		//Add all the contents to the document.
		for(XMLEvent event : xmlEvents) {
			eventWriter.add(event);

			if(event.isEndElement()) {
				eventWriter.add(endLine);
			}
		}

		//Close the single root tag.
		eventWriter.add(eventFactory.createEndElement("", "", "testsuites"));
		eventWriter.add(endLine);

		//Add an end to the document and close the writer.
		eventWriter.add(eventFactory.createEndDocument());
		eventWriter.close();
	}

	private static Map<String, String> getProjectInfo() throws ParserConfigurationException, IOException, SAXException {
		Map<String, String> infoMap = new HashMap<>();

		File pomFile = new File(POM_FILE_NAME);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(pomFile);

		doc.getDocumentElement().normalize();

		Node projectNameNode = doc.getElementsByTagName("name").item(0);
		infoMap.put("name", projectNameNode.getTextContent());

		Node versionNode = doc.getElementsByTagName("version").item(0);
		infoMap.put("version", versionNode.getTextContent());

		return infoMap;
	}
}
