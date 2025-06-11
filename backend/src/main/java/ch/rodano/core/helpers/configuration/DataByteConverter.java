package ch.rodano.core.helpers.configuration;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.Converter;

public class DataByteConverter implements Converter<byte[], Byte[]> {

	private static final long serialVersionUID = 1588612595752596800L;

	@Override
	public Byte[] from(final byte[] databaseObject) {
		return ArrayUtils.toObject(databaseObject);
	}

	@Override
	public byte[] to(final Byte[] userObject) {
		if(userObject != null) {
			return ArrayUtils.toPrimitive(userObject);
		}
		return null;
	}

	@Override
	public Class<byte[]> fromType() {
		return byte[].class;
	}

	@Override
	public Class<Byte[]> toType() {
		return Byte[].class;
	}

}
