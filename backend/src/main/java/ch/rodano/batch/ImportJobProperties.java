package ch.rodano.batch;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.import")
public class ImportJobProperties {

	private String job = "import-config";
	private String projectId = "TEST";
	private String config;
	private final Db db = new Db();

	public static class Db {
		private String url;
		private String user;
		private String password;

		public String getUrl() {
			return url;
		}

		public void setUrl(final String url) {
			this.url = url;
		}

		public String getUser() {
			return user;
		}

		public void setUser(final String user) {
			this.user = user;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}
	}

	public String getJob() {
		return job;
	}

	public void setJob(final String job) {
		this.job = job;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(final String config) {
		this.config = config;
	}

	public Db getDb() {
		return db;
	}
}
