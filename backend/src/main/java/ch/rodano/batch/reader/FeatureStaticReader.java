package ch.rodano.batch.reader;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import jakarta.batch.api.chunk.AbstractItemReader;

import ch.rodano.configuration.model.feature.FeatureStatic;

public class FeatureStaticReader extends AbstractItemReader {

	private Iterator<FeatureStatic> featureStaticIterator;

	@Override
	public void open(final Serializable checkpoint) throws Exception {
		featureStaticIterator = Arrays.asList(FeatureStatic.values()).iterator();
	}

	@Override
	public Object readItem() {
		if(featureStaticIterator.hasNext()) {
			return featureStaticIterator.next();
		}
		return null;
	}
}
