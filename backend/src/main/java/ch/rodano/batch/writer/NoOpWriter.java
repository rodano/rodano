package ch.rodano.batch.writer;

import java.util.List;

import jakarta.batch.api.chunk.AbstractItemWriter;

public class NoOpWriter extends AbstractItemWriter {

	@Override
	public void writeItems(final List<Object> list) throws Exception {
		System.out.println("Read batch of " + list.size() + " items: " + list.stream().map(Object::toString).reduce("", (a, b) -> a + ", " + b));
	}
}
