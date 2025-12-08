package ch.rodano.batch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.StepExecution;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.rodano.core.services.project.ProjectIdResolver;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

	private final ImportJobProperties properties;
	private final ProjectIdResolver projectIdResolver;
	private final JobOperator jobOperator = BatchRuntime.getJobOperator();

	public BatchController(final ImportJobProperties properties,
						   final ProjectIdResolver projectIdResolver) {
		this.properties = properties;
		this.projectIdResolver = projectIdResolver;
	}

	public record ImportRequest(
		String job,
		String projectId,
		String config,
		String dbUrl,
		String dbUser,
		String dbPassword
	) {
	}

	@PostMapping("/import")
	public ResponseEntity<Map<String, Object>> startImport(@RequestBody(required = false) final ImportRequest request) {
		final Properties params = new Properties();
		params.setProperty("job", or(request != null ? request.job() : null, properties.getJob()));
		params.setProperty("config", or(request != null ? request.config() : null, properties.getConfig()));
		params.setProperty("db.url", or(request != null ? request.dbUrl() : null, properties.getDb().getUrl()));
		params.setProperty("db.user", or(request != null ? request.dbUser() : null, properties.getDb().getUser()));
		params.setProperty("db.pass", or(request != null ? request.dbPassword() : null, properties.getDb().getPassword()));

		final String finalProjectId = resolveProjectId(request != null ? request.projectId() : null);

		params.setProperty("projectId", finalProjectId);

		final String jobName = params.getProperty("job");
		final long execId = jobOperator.start(jobName, params);

		final Map<String, Object> body = new LinkedHashMap<>();
		body.put("executionId", execId);
		body.put("job", jobName);
		body.put("parameters", propsToMap(params));
		return ResponseEntity.accepted().body(body);
	}

	@GetMapping("/execution/{id}")
	public ResponseEntity<Map<String, Object>> getExecution(@PathVariable final long id) {
		final JobExecution jobExecution = jobOperator.getJobExecution(id);
		final Map<String, Object> body = new LinkedHashMap<>();
		body.put("id", jobExecution.getExecutionId());
		body.put("jobName", jobExecution.getJobName());
		body.put("status", jobExecution.getBatchStatus().toString());
		body.put("startTime", jobExecution.getStartTime());
		body.put("endTime", jobExecution.getEndTime());
		body.put("exitStatus", jobExecution.getExitStatus());
		final List<Map<String, Object>> steps = new ArrayList<>();
		for(StepExecution stepExecution : jobOperator.getStepExecutions(id)) {
			final Map<String, Object> step = new LinkedHashMap<>();
			step.put("stepName", stepExecution.getStepName());
			step.put("status", stepExecution.getBatchStatus().toString());
			step.put("startTime", stepExecution.getStartTime());
			step.put("endTime", stepExecution.getEndTime());
			step.put("exitStatus", stepExecution.getExitStatus());
			steps.add(step);
		}
		body.put("steps", steps);
		return ResponseEntity.ok(body);
	}

	@PostMapping("/execution/{id}/stop")
	public ResponseEntity<?> stop(@PathVariable final long id) {
		jobOperator.stop(id);
		return ResponseEntity.accepted().build();
	}

	private static String or(final String a, final String b) {
		return (a != null && !a.isBlank()) ? a : b;
	}

	private static Map<String, String> propsToMap(final Properties props) {
		final Map<String, String> map = new LinkedHashMap<>();
		for(String name : props.stringPropertyNames()) {
			map.put(name, props.getProperty(name));
		}
		return map;
	}

	private String resolveProjectId(final String input) {
		final String fromRequest = normalizeProjectId(input, true);
		if(fromRequest != null) {
			return fromRequest;
		}

		if(projectIdResolver.hasProject()) {
			return projectIdResolver.id().toString();
		}

		final String fromProps = normalizeProjectId(properties.getProjectId(), false);
		if(fromProps != null) {
			return fromProps;
		}

		throw new IllegalStateException(
			"No projectId provided, no project selected, and no default projectId configured"
		);
	}

	private String normalizeProjectId(final String raw, final boolean allowCreateIfUnknown) {
		if(raw == null || raw.isBlank()) {
			return null;
		}

		try {
			UUID.fromString(raw);
			return raw;
		}
		catch(IllegalArgumentException ignore) {
			System.out.println("Ignoring unknown project id: " + raw);
		}

		final UUID resolved = projectIdResolver.resolveCode(raw);
		if(resolved != null) {
			return resolved.toString();
		}

		if(allowCreateIfUnknown) {
			final UUID random = UUID.randomUUID();
			return random.toString();
		}

		throw new IllegalArgumentException("Unknown project code: " + raw);
	}
}
