package ch.rodano.batch;

import java.util.List;

import jakarta.batch.api.chunk.listener.ItemProcessListener;
import jakarta.batch.api.chunk.listener.ItemReadListener;
import jakarta.batch.api.chunk.listener.ItemWriteListener;
import jakarta.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("TraceListener")
public class TraceListener implements ItemReadListener, ItemProcessListener, ItemWriteListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(TraceListener.class);

	@Override
	public void beforeRead() {
	}

	@Override
	public void afterRead(final Object item) {
		LOGGER.info("READ item type={} value={}", item == null ? "null" : item.getClass().getSimpleName(), item);
	}

	@Override
	public void onReadError(final Exception e) {
		LOGGER.error("READ error", e);
	}

	@Override
	public void beforeProcess(final Object item) {
	}

	@Override
	public void afterProcess(final Object item, final Object result) {
		LOGGER.info("PROCESS itemInType={} -> itemOutType={}",
			item == null ? "null" : item.getClass().getName(),
			result == null ? "null" : result.getClass().getName());
	}

	@Override
	public void onProcessError(final Object item, final Exception e) {
		LOGGER.error("PROCESS error on {}", item, e);
	}

	@Override
	public void beforeWrite(final List<Object> items) {
	}

	@Override
	public void afterWrite(final List<Object> items) {
		LOGGER.info("WRITE chunk size={}", items == null ? 0 : items.size());
	}

	@Override
	public void onWriteError(final List<Object> items, final Exception e) {
		LOGGER.error("WRITE error on {} items", items == null ? 0 : items.size(), e);
	}
}
