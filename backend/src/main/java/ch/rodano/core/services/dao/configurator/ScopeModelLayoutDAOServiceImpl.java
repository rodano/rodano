package ch.rodano.core.services.dao.configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rodano.api.config.SectionDTO;
import ch.rodano.api.config.WidgetDTO;
import ch.rodano.api.config.WidgetLayoutDTO;

import static ch.rodano.core.model.jooq.tables.ScopeModel.SCOPE_MODEL;

@Repository
public class ScopeModelLayoutDAOServiceImpl implements ScopeModelLayoutDAOService {

	private final DSLContext dslContext;
	private final ObjectMapper objectMapper;

	public ScopeModelLayoutDAOServiceImpl(final DSLContext dslContext, final ObjectMapper objectMapper) {
		this.dslContext = dslContext;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "scopeModelLayout", key = "#projectId.toString() + ':' + #scopeModelId.toString() + ':layout'")
	public WidgetLayoutDTO getLayout(final UUID projectId, final UUID scopeModelId) {
		final var record = dslContext.selectFrom(SCOPE_MODEL)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.fetchOne();

		if(record == null || record.getLayout() == null || record.getLayout().isBlank()) {
			return new WidgetLayoutDTO(new ArrayList<>());
		}

		return parseLayout(record.getLayout());
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "scopeModelLayout", key = "#projectId.toString() + ':' + #scopeModelId.toString() + ':layout'"),
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':summary'"),
		@CacheEvict(value = "scopeModels", key = "#projectId.toString() + ':full'"),
		@CacheEvict(value = "scopeModel", key = "#projectId.toString() + ':' + #scopeModelId.toString()")
	})
	public WidgetLayoutDTO saveLayout(final UUID projectId, final UUID scopeModelId, final WidgetLayoutDTO dto) {
		final String json = serializeLayout(dto);

		dslContext.update(SCOPE_MODEL)
			.set(SCOPE_MODEL.LAYOUT, json)
			.where(SCOPE_MODEL.PROJECT_ID.eq(projectId))
			.and(SCOPE_MODEL.SCOPE_MODEL_ID.eq(scopeModelId))
			.execute();

		return getLayout(projectId, scopeModelId);
	}

	@SuppressWarnings("unchecked")
	private WidgetLayoutDTO parseLayout(final String json) {
		try {
			final Map<String, Object> root = objectMapper.readValue(json, new TypeReference<>() {
			});
			final List<Map<String, Object>> rawSections = (List<Map<String, Object>>) root.getOrDefault("sections", new ArrayList<>());

			final List<SectionDTO> sections = new ArrayList<>();
			for(int i = 0; i < rawSections.size(); i++) {
				final Map<String, Object> s = rawSections.get(i);
				final List<Map<String, Object>> rawWidgets = (List<Map<String, Object>>) s.getOrDefault("widgets", new ArrayList<>());

				final List<WidgetDTO> widgets = new ArrayList<>();
				for(int j = 0; j < rawWidgets.size(); j++) {
					final Map<String, Object> w = rawWidgets.get(j);
					final Map<String, Object> rawParams = (Map<String, Object>) w.getOrDefault("parameters", new HashMap<>());
					final Map<String, String> params = new HashMap<>();
					rawParams.forEach((k, v) -> params.put(k, v != null ? v.toString() : null));

					widgets.add(new WidgetDTO(
						parseUuid(w.get("widgetId")),
						(String) w.get("type"),
						(String) w.getOrDefault("width", "FULL"),
						j,
						(String) w.get("textBefore"),
						(String) w.get("textAfter"),
						parseUuid(w.get("requiredFeatureId")),
						(String) w.get("rightEntity"),
						(String) w.get("rightValue"),
						parseUuid(w.get("rightTargetId")),
						params
					));
				}

				final TreeMap<String, String> labels = new TreeMap<>();
				final Object rawLabels = s.get("labels");
				if(rawLabels instanceof Map<?, ?> labelsMap) {
					labelsMap.forEach((k, v) -> labels.put(k.toString(), v != null ? v.toString() : null));
				}

				sections.add(new SectionDTO(
					parseUuid(s.get("sectionId")),
					(String) s.get("id"),
					labels,
					i,
					parseUuid(s.get("requiredFeatureId")),
					(String) s.get("rightEntity"),
					(String) s.get("rightValue"),
					parseUuid(s.get("rightTargetId")),
					widgets
				));
			}

			return new WidgetLayoutDTO(sections);
		}
		catch(Exception e) {
			return new WidgetLayoutDTO(new ArrayList<>());
		}
	}

	private UUID parseUuid(final Object value) {
		if(value == null) {
			return null;
		}
		try {
			return UUID.fromString(value.toString());
		}
		catch(IllegalArgumentException e) {
			return null;
		}
	}

	private String serializeLayout(final WidgetLayoutDTO dto) {
		try {
			final List<Map<String, Object>> sections = new ArrayList<>();
			for(final SectionDTO section : dto.sections()) {
				final Map<String, Object> s = new LinkedHashMap<>();
				if(section.sectionId() != null) {
					s.put("sectionId", section.sectionId().toString());
				}
				s.put("id", section.id());
				s.put("labels", section.label());
				if(section.requiredFeatureId() != null) {
					s.put("requiredFeatureId", section.requiredFeatureId().toString());
				}
				if(section.rightEntity() != null) {
					s.put("rightEntity", section.rightEntity());
				}
				if(section.rightValue() != null) {
					s.put("rightValue", section.rightValue());
				}
				if(section.rightTargetId() != null) {
					s.put("rightTargetId", section.rightTargetId().toString());
				}

				final List<Map<String, Object>> widgets = new ArrayList<>();
				for(final WidgetDTO widget : section.widgets()) {
					final Map<String, Object> w = new LinkedHashMap<>();
					if(widget.widgetId() != null) {
						w.put("widgetId", widget.widgetId().toString());
					}
					w.put("type", widget.type());
					w.put("parameters", widget.parameters() != null ? widget.parameters() : new HashMap<>());
					w.put("width", widget.width() != null ? widget.width() : "FULL");
					if(widget.textBefore() != null) {
						w.put("textBefore", widget.textBefore());
					}
					if(widget.textAfter() != null) {
						w.put("textAfter", widget.textAfter());
					}
					if(widget.requiredFeatureId() != null) {
						w.put("requiredFeatureId", widget.requiredFeatureId().toString());
					}
					if(widget.rightEntity() != null) {
						w.put("rightEntity", widget.rightEntity());
					}
					if(widget.rightValue() != null) {
						w.put("rightValue", widget.rightValue());
					}
					if(widget.rightTargetId() != null) {
						w.put("rightTargetId", widget.rightTargetId().toString());
					}
					widgets.add(w);
				}
				s.put("widgets", widgets);
				sections.add(s);
			}

			final Map<String, Object> root = new LinkedHashMap<>();
			root.put("sections", sections);
			return objectMapper.writeValueAsString(root);
		}
		catch(Exception e) {
			return "{\"sections\":[]}";
		}
	}
}
