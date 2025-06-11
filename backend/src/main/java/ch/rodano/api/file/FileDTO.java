package ch.rodano.api.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

import ch.rodano.core.model.file.File;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "File transmission DTO")
public record FileDTO(
	@NotNull
	Long pk,

	Long scopePk,
	Long eventPk,
	Long datasetPk,
	Long fieldPk,

	@Schema(description = "File name")
	@NotBlank
	String name,
	@Schema(description = "File checksum")
	@NotBlank
	String checksum,

	@Schema(description = "Concatenation of the file name, its checksum encoded in Base32 and file extension. To be used as the file field value.")
	@NotBlank
	String uniqueName
) {
	/**
	 * Generate a unique file name which consists of the name of the file, concatenated with its text-encoded checksum and its file extension.
	 * @param name      Name of the file
	 * @param checksum  Text-encoded checksum of the file
	 * @return          The new unique name of the file
	 */
	private static String generateUniqueName(final String name, final String checksum) {
		final var basename = FilenameUtils.getBaseName(name);
		final var extension = FilenameUtils.getExtension(name);
		return String.format("%s_%s.%s", basename, checksum, extension);
	}

	/**
	 * Encode the byte array checksum to text with the Base32 encoding
	 * @param checksum Checksum byte array
	 * @return         A Base32 text encoding of the byte array
	 */
	private static String encodeChecksum(final byte[] checksum) {
		final var encoder = new Base32();
		return encoder.encodeToString(checksum);
	}

	public FileDTO(final File file) {
		this(
			file.getPk(),
			file.getScopeFk(),
			file.getEventFk(),
			file.getDatasetFk(),
			file.getFieldFk(),
			file.getName(),
			encodeChecksum(file.getChecksum()),
			generateUniqueName(file.getName(), encodeChecksum(file.getChecksum()))
		);
	}
}
