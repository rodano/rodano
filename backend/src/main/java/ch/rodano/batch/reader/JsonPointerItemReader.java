package ch.rodano.batch.reader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.ItemReader;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Dependent
public class JsonPointerItemReader implements ItemReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonPointerItemReader.class);

	@Inject
	@BatchProperty(name = "resource")
	private String resource;

	@Inject
	@BatchProperty(name = "jsonPointer")
	private String jsonPointer;

	@Inject
	@BatchProperty(name = "beanType")
	private String beanType;

	private final ObjectMapper mapper = new ObjectMapper(new JsonFactory());
	private InputStream inputStream;
	private List<JsonNode> items;
	private int index = 0;
	private Class<?> targetClass;

	@Override
	public void open(final Serializable serializable) throws Exception {
		if(resource == null || resource.isBlank()) {
			throw new IllegalArgumentException("Resource must be set");
		}
		if(beanType == null || beanType.isBlank()) {
			throw new IllegalArgumentException("Bean type must be set");
		}

		targetClass = Class.forName(beanType);
		inputStream = openResource(resource);

		final JsonNode root = mapper.readTree(inputStream);
		final boolean pointerBlank = jsonPointer == null || jsonPointer.isBlank();
		final JsonNode node = pointerBlank ? root : root.at(jsonPointer);

		if(node.isMissingNode() || node.isNull()) {
			throw new IllegalArgumentException("JSON pointer '" + jsonPointer + "' not found in " + resource);
		}

		this.items = new ArrayList<>();
		if(node.isArray()) {
			for(Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
				this.items.add(it.next());
			}
		}
		else {
			this.items.add(node);
		}

		if(serializable instanceof Integer ser) {
			this.index = Math.min(ser, this.items.size());
		}
	}

	@Override
	public Object readItem() throws Exception {
		if(index >= items.size()) {
			return null;
		}
		final JsonNode node = items.get(index++);
		return mapper.treeToValue(node, targetClass);
	}

	@Override
	public Serializable checkpointInfo() {
		return index;
	}

	@Override
	public void close() throws Exception {
		if(inputStream != null) {
			try {
				inputStream.close();
			}
			catch(Exception e) {
				LOGGER.warn("Failed to close database connection", e);
			}
			inputStream = null;
		}
	}

	private static InputStream openResource(final String resource) throws Exception {
		if(resource.startsWith("classpath:")) {
			final String path = resource.substring("classpath:".length());
			final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(stripLeadingSlash(path));
			if(is == null) {
				throw new IllegalArgumentException("Resource not found: " + resource);
			}
			return is;
		}
		final Path p = Path.of(resource);
		if(!p.isAbsolute()) {
			final Path rel = Path.of("").toAbsolutePath().resolve(resource);
			if(Files.exists(rel)) {
				return new FileInputStream(rel.toFile());
			}
		}
		return new FileInputStream(p.toFile());
	}

	private static String stripLeadingSlash(final String s) {
		int i = 0;
		while(i < s.length() && s.charAt(i) == '/') {
			i++;
		}
		return s.substring(i);
	}
}
