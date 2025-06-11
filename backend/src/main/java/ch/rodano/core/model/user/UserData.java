package ch.rodano.core.model.user;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserData {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	// Encryption
	@JsonProperty
	private String encryptedPrivateKey;

	// Decrypted private key in memory
	// Remove the decrypted key as soon as the application can store data in the client browser
	@JsonIgnore
	private PrivateKey privateKey;

	@JsonProperty
	private PublicKey publicKey;

	// Two step
	// Status (enabled/disabled)
	@JsonProperty
	private boolean twoStep;

	// Encrypted two step key (on decrypted password) which will be stored in database
	private byte[] encryptedTwoStepKey;

	private List<UserClient> twoStepTrustedClients = new ArrayList<>();

	private List<byte[]> oneUseTwoStepCodes;

	// Database queries
	private Integer queriesNumber;
	private ZonedDateTime queriesDate;

	// Encode public key in hex to store it in json
	@JsonIgnore
	public String getPublicKeyAsString() {
		return publicKey != null ? new String(Hex.encodeHex(publicKey.getEncoded())) : null;
	}

	/**
	 * @return the publicKey
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(final String publicKey) {
		if(!StringUtils.isNotBlank(publicKey)) {
			return;
		}

		try {
			this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Hex.decodeHex(publicKey.toCharArray())));
		}
		catch(InvalidKeySpecException | NoSuchAlgorithmException | DecoderException e) {
			//no way to come here
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	// Encode encrypted key to base64 to store it in json
	public String getEncryptedTwoStepKey() {
		return encryptedTwoStepKey != null ? Base64.encodeBase64String(encryptedTwoStepKey) : null;
	}

	@JsonIgnore
	public byte[] getEncryptedTwoStepKeyAsBytes() {
		return encryptedTwoStepKey;
	}

	public void setEncryptedTwoStepKey(final String encryptedTwoStepKey) {
		if(encryptedTwoStepKey != null && !encryptedTwoStepKey.trim().isEmpty()) {
			this.encryptedTwoStepKey = Base64.decodeBase64(encryptedTwoStepKey);
		}
	}

	public UserClient getUserClient(final String key) throws Exception {
		for(final var client : twoStepTrustedClients) {
			if(client.getKey().equals(key)) {
				return client;
			}
		}

		throw new Exception(String.format("No client with key %s", key));
	}

	@JsonAnySetter
	public void setAnySetter(final String key, final Object value) {
		System.err.printf("Unknown property %s with value %s%n", key, value);
	}

	public String getEncryptedPrivateKey() {
		return encryptedPrivateKey;
	}

	public void setEncryptedPrivateKey(final String encryptedPrivateKey) {
		this.encryptedPrivateKey = encryptedPrivateKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(final PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public void setPublicKey(final PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public boolean isTwoStep() {
		return twoStep;
	}

	public void setTwoStep(final boolean twoStep) {
		this.twoStep = twoStep;
	}

	public void setEncryptedTwoStepKey(final byte[] encryptedTwoStepKey) {
		this.encryptedTwoStepKey = encryptedTwoStepKey;
	}

	public List<UserClient> getTwoStepTrustedClients() {
		return twoStepTrustedClients;
	}

	public void setTwoStepTrustedClients(final List<UserClient> twoStepTrustedClients) {
		this.twoStepTrustedClients = twoStepTrustedClients;
	}

	public List<byte[]> getOneUseTwoStepCodes() {
		return oneUseTwoStepCodes;
	}

	public void setOneUseTwoStepCodes(final List<byte[]> oneUseTwoStepCodes) {
		this.oneUseTwoStepCodes = oneUseTwoStepCodes;
	}

	public Integer getQueriesNumber() {
		return queriesNumber;
	}

	public void setQueriesNumber(final Integer queriesNumber) {
		this.queriesNumber = queriesNumber;
	}

	public ZonedDateTime getQueriesDate() {
		return queriesDate;
	}

	public void setQueriesDate(final ZonedDateTime queriesDate) {
		this.queriesDate = queriesDate;
	}
}
