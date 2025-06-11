package ch.rodano.api.controller.form.exception;

import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import ch.rodano.api.exception.ManagedException;
import ch.rodano.api.form.BlockingErrorDTO;
import ch.rodano.api.form.BlockingErrorsDTO;

public class DatasetSubmissionException extends Exception implements ManagedException {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Serial
	private static final long serialVersionUID = -6781949910402050811L;

	private final List<BlockingErrorDTO> blockingErrorDTOS;

	public DatasetSubmissionException(final List<BlockingErrorDTO> blockingErrorDTOS) {
		super("Some fields are invalid");
		final var fieldModelIds = blockingErrorDTOS.stream().map(BlockingErrorDTO::fieldModelId).collect(Collectors.joining(", "));
		logger.warn(String.format("The following fields are invalid: %s", fieldModelIds));
		this.blockingErrorDTOS = blockingErrorDTOS;
	}

	@Override
	public HttpStatus getHttpErrorStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	public BlockingErrorsDTO getBlockingErrorsDTO() {
		return new BlockingErrorsDTO(getMessage(), blockingErrorDTOS);
	}
}
