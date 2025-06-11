package ch.rodano.core.services.bll.scope;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ch.rodano.api.dto.paging.PagedResult;
import ch.rodano.configuration.exceptions.NoRespectForConfigurationException;
import ch.rodano.configuration.model.scope.ScopeModel;
import ch.rodano.configuration.model.workflow.WorkflowAction;
import ch.rodano.core.model.audit.DatabaseActionContext;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.event.Event;
import ch.rodano.core.model.event.Timeframe;
import ch.rodano.core.model.exception.DeletedObjectException;
import ch.rodano.core.model.exception.LockedObjectException;
import ch.rodano.core.model.field.Field;
import ch.rodano.core.model.form.Form;
import ch.rodano.core.model.resource.Resource;
import ch.rodano.core.model.role.Role;
import ch.rodano.core.model.rules.data.DataState;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeRelation;
import ch.rodano.core.model.scope.ScopeSearch;
import ch.rodano.core.model.scope.exceptions.ImpossibleScopeModelPathException;
import ch.rodano.core.model.scope.exceptions.ImpossibleVirtualChainException;
import ch.rodano.core.model.scope.exceptions.MaxDescendantScopesReachedException;
import ch.rodano.core.model.scope.exceptions.OutOfEnrollmentWindowException;
import ch.rodano.core.model.scope.exceptions.ScopeCodeAlreadyUsedException;
import ch.rodano.core.model.workflow.WorkflowStatus;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.ValidationService;
import ch.rodano.core.services.bll.form.FormService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.DataFamily;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.event.EventDAOService;
import ch.rodano.core.services.dao.field.FieldDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.rule.RuleService;

@Service
public class ScopeServiceImpl implements ScopeService {
	private static final String ALPHA_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final StudyService studyService;
	private final RuleService ruleService;
	private final EventService eventService;
	private final EventDAOService eventDAOService;
	private final DatasetService datasetService;
	private final FormService formService;
	private final ScopeDAOService scopeDAOService;
	private final ScopeRelationService scopeRelationService;
	private final ValidationService validationService;
	private final FieldDAOService fieldDAOService;
	private final WorkflowStatusService workflowStatusService;

	private final Pattern siblingsPattern;
	private final Pattern sameScopeModelPattern;

	public ScopeServiceImpl(
		final StudyService studyService,
		final RuleService ruleService,
		final EventService eventService,
		final EventDAOService eventDAOService,
		final DatasetService datasetService,
		final FormService formService,
		final FieldDAOService fieldDAOService,
		final WorkflowStatusService workflowStatusService,
		final ValidationService validationService,
		final ScopeDAOService scopeDAOService,
		final ScopeRelationService scopeRelationService
	) {
		this.studyService = studyService;
		this.ruleService = ruleService;
		this.eventDAOService = eventDAOService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.formService = formService;
		this.fieldDAOService = fieldDAOService;
		this.workflowStatusService = workflowStatusService;
		this.validationService = validationService;
		this.scopeDAOService = scopeDAOService;
		this.scopeRelationService = scopeRelationService;

		siblingsPattern = Pattern.compile("\\$\\{siblingsNumber:(\\d+)}");
		sameScopeModelPattern = Pattern.compile("\\$\\{sameScopeModelNumber:(\\d+)}");
	}

	/**
	 * Create a candidate scope
	 *
	 * @param model     The scope model
	 * @param startDate The start date of the scope
	 * @param parent    The parent of the scope
	 * @return A newly created candidate scope
	 */
	@Override
	public Scope createCandidate(
		final ScopeModel model,
		final ZonedDateTime startDate,
		final Scope parent
	) {
		final var scope = new Scope();
		scope.setScopeModel(model);
		scope.setVirtual(model.isVirtual());
		scope.setStartDate(startDate);

		if(StringUtils.isNotBlank(model.getScopeFormat())) {
			scope.setCode(getNextCode(model, parent));
			scope.setShortname(scope.getCode());
		}

		return scope;
	}

	@Override
	public Scope createFromCandidate(
		final Scope candidateScope,
		final ScopeModel model,
		final Scope parent,
		final DatabaseActionContext context,
		final String rationale
	) {
		// Check the correctness of the candidate scope.
		checkModelParentCorrectness(model, parent);

		// Check if the code of the scope is not already used (in case of a concurrent scope creation).
		if(scopeDAOService.getScopeByCode(candidateScope.getCode()) != null) {
			throw new ScopeCodeAlreadyUsedException(candidateScope.getCode());
		}

		final var enhancedRationale = StringUtils.isBlank(rationale) ? "Create scope" : "Create scope: " + rationale;

		// Add the scope
		create(candidateScope, parent, context, enhancedRationale);

		//initialize the workflows and events
		final var family = new DataFamily(candidateScope);
		workflowStatusService.createAll(family, candidateScope, null, context, enhancedRationale);
		eventService.createAll(candidateScope, context, enhancedRationale);
		datasetService.createAll(candidateScope, context, enhancedRationale);
		formService.createAll(candidateScope, context, enhancedRationale);

		final var state = new DataState(candidateScope);

		//execute scope creation rules
		var rules = model.getCreateRules();
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}

		//execute global scope creation rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.CREATE_SCOPE);
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}

		return candidateScope;
	}

	@Override
	public String getNextCode(final ScopeModel model, final Scope parent) {
		final var scopeFormat = model.getScopeFormat();
		if(StringUtils.isBlank(scopeFormat)) {
			throw new NoRespectForConfigurationException("Unable to generate new code for a scope model without a scope format");
		}
		var siblingsNumber = scopeDAOService.getScopesByScopeModelIdHavingAncestor(Collections.singleton(model.getId()), Collections.singleton(parent.getPk())).size();
		var sameScopeModelNumber = scopeDAOService.getScopesByScopeModelIdCount(model.getId()).intValue();

		while(true) {
			final var potentialCode = generateCode(model, parent, ++siblingsNumber, ++sameScopeModelNumber);
			if(scopeDAOService.getScopeByCode(potentialCode) == null) {
				return potentialCode;
			}
		}
	}

	@Override
	public void create(
		final Scope scope,
		final Scope parent,
		final DatabaseActionContext context,
		final String rationale
	) {
		// If the scope is added with no parent, we assume that it is the root scope
		// Otherwise need to check if the parent scope is locked or not
		if(parent != null && parent.getLocked()) {
			throw new LockedObjectException(parent);
		}

		if(parent != null && parent.isClosed()) {
			throw new OutOfEnrollmentWindowException(parent);
		}

		scopeDAOService.saveScope(scope, context, rationale);

		// Add relation with the parent
		if(parent != null) {
			scopeRelationService.createRelation(scope, parent, scope.getCreationTime(), Optional.empty(), context, rationale);
		}
	}

	@Override
	public void restore(
		final Scope scope,
		final DatabaseActionContext context,
		final String rationale
	) {
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}

		final var scopeModel = scope.getScopeModel();

		final var baseRationale = String.format("%s restored", scopeModel.getDefaultLocalizedShortname());
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
		scopeDAOService.restoreScope(scope, context, enhancedRationale);

		validateFieldsOnScopeAndEvents(scope, context, enhancedRationale);

		final var state = new DataState(scope);

		//execute scope model restoration rules
		var rules = scopeModel.getRestoreRules();
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}

		//execute global scope restoration rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.RESTORE_SCOPE);
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}
	}

	@Override
	public void delete(
		final Scope scope,
		final DatabaseActionContext context,
		final String rationale
	) {
		if(isRootScope(scope)) {
			return;
		}

		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}

		final var scopeModel = scope.getScopeModel();

		final var baseRationale = String.format("%s removed", scopeModel.getDefaultLocalizedShortname());
		final var enhancedRationale = StringUtils.isBlank(rationale) ? baseRationale : String.format("%s: %s", baseRationale, rationale);
		scopeDAOService.deleteScope(scope, context, enhancedRationale);

		final var state = new DataState(scope);

		//execute scope model removal rules
		var rules = scopeModel.getRemoveRules();
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}

		//execute global scope removal rules
		rules = studyService.getStudy().getEventActions().get(WorkflowAction.REMOVE_SCOPE);
		if(CollectionUtils.isNotEmpty(rules)) {
			ruleService.execute(state, rules, context);
		}
	}

	@Override
	public void save(
		final Scope scope,
		final DatabaseActionContext context,
		final String rationale
	) {
		// Check if the scope is locked
		if(scope.getLocked()) {
			throw new LockedObjectException(scope);
		}

		// Check if the code has already been used.
		final var scopeHavingSameCode = scopeDAOService.getScopeByCode(scope.getCode());
		if(scopeHavingSameCode != null && !scopeHavingSameCode.equals(scope)) {
			throw new ScopeCodeAlreadyUsedException(scope.getCode());
		}

		scopeDAOService.saveScope(scope, context, rationale);
	}

	@Override
	//refrain from optimizing this using an SQL query because the audit trail is required
	public void lock(final Scope scope, final DatabaseActionContext context) {
		if(scope.getDeleted()) {
			throw new DeletedObjectException(scope);
		}

		lockScope(scope, context, "Lock scope");
		eventService.getAll(scope).forEach(e -> eventService.lock(scope, e, context, "Lock scope"));

		//lock descendants scopes
		final var rationale = "Lock ancestor scope " + scope.getShortname();
		final var descendants = scopeRelationService.getAllEnabledDescendants(scope).stream()
			.filter(s -> !s.getLocked())
			.toList();
		for(final var descendant : descendants) {
			lockScope(descendant, context, rationale);
			eventService.getAll(descendant).stream()
				.filter(e -> !e.getLocked())
				.forEach(e -> eventService.lock(descendant, e, context, rationale));
		}
	}

	@Override
	//refrain from optimizing this using an SQL query because the audit trail is required
	public void unlock(final Scope scope, final DatabaseActionContext context) {
		if(scope.getDeleted()) {
			throw new DeletedObjectException(scope);
		}

		unlockScope(scope, context, "Unlock scope");
		eventService.getAll(scope).forEach(e -> eventService.unlock(scope, e, context, "Unlock scope"));

		//unlock descendants scopes
		final var rationale = "Unlock ancestor scope " + scope.getShortname();
		final var descendants = scopeRelationService.getAllEnabledDescendants(scope).stream()
			.filter(Scope::getLocked)
			.toList();
		for(final var descendant : descendants) {
			unlockScope(descendant, context, rationale);
			eventService.getAll(descendant).stream()
				.filter(Event::getLocked)
				.forEach(e -> eventService.unlock(descendant, e, context, rationale));
		}
	}

	@Override
	public void validateFieldsOnScope(
		final Scope scope,
		final DatabaseActionContext context,
		final String rationale
	) {
		//revalidate all fields that have a value
		final var fields = fieldDAOService.getFieldsFromScopeWithAValue(scope.getPk());
		for(final var f : fields) {
			final var dataset = datasetService.get(f);
			validationService.validateField(scope, Optional.empty(), dataset, f, context.toSystemAction(), rationale);
		}
	}

	@Override
	public void validateFieldsOnScopeAndEvents(
		final Scope scope,
		final DatabaseActionContext context,
		final String rationale
	) {
		for(final var dataset : datasetService.getAll(scope)) {
			datasetService.validateFieldsOnDataset(scope, Optional.empty(), dataset, context, rationale);
		}
		for(final var event : eventService.getAllIncludingRemoved(scope)) {
			if(!event.getDeleted() && !event.getLocked()) {
				eventService.validate(scope, event, context, rationale);
			}
		}
	}

	@Override
	public boolean isRootScope(final Scope scope) {
		return scope.getScopeModel().isRoot();
	}

	@Override
	public Scope getRootScope() {
		return scopeDAOService.getRootScope();
	}

	@Override
	public List<Scope> getAll(final ScopeModel scopeModel) {
		return scopeDAOService.getScopesByScopeModelId(scopeModel.getId());
	}

	@Override
	public List<Scope> getAll(final Collection<ScopeModel> scopeModels, final Collection<Scope> ancestors) {
		final var scopeModelIds = scopeModels.stream().map(ScopeModel::getId).toList();
		final var ancestorPks = ancestors.stream().map(Scope::getPk).toList();
		return scopeDAOService.getScopesByScopeModelIdHavingAncestor(scopeModelIds, ancestorPks);
	}

	@Override
	public Scope get(final Event event) {
		return scopeDAOService.getScopeByPk(event.getScopeFk());
	}

	@Override
	public Scope get(final Form form) {
		if(form.getScopeFk() != null) {
			return scopeDAOService.getScopeByPk(form.getScopeFk());
		}
		final var event = eventDAOService.getEventByPk(form.getEventFk());
		return get(event);
	}

	@Override
	public Scope get(final Dataset dataset) {
		if(dataset.getScopeFk() != null) {
			return scopeDAOService.getScopeByPk(dataset.getScopeFk());
		}
		final var event = eventDAOService.getEventByPk(dataset.getEventFk());
		return get(event);
	}

	@Override
	public Scope get(final Field field) {
		final var dataset = datasetService.get(field);
		return get(dataset);
	}

	@Override
	public Scope get(final WorkflowStatus workflowStatus) {
		return scopeDAOService.getScopeByPk(workflowStatus.getScopeFk());
	}

	@Override
	public Scope get(final Resource resource) {
		return scopeDAOService.getScopeByPk(resource.getScopeFk());
	}

	@Override
	public Scope get(final Role role) {
		return scopeDAOService.getScopeByPk(role.getScopeFk());
	}

	@Override
	public List<Scope> getAllIncludingRemoved() {
		return scopeDAOService.getAllScopes();
	}

	@Override
	public List<Scope> getAllIncludingRemoved(final ScopeModel scopeModel) {
		return scopeDAOService.getAllScopesByScopeModelId(scopeModel.getId());
	}

	@Override
	public PagedResult<Scope> search(final ScopeSearch search) {
		return scopeDAOService.search(search);
	}

	@Override
	public boolean isDeletedOrInDeletedScope(final Scope scope) {
		return scope.getDeleted() || scopeDAOService.hasDeletedDefaultAncestor(scope.getPk());
	}

	@Override
	public Integer getLeafCount(final Scope scope) {
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();
		//shortcut: leaf scopes cannot contain any leaves
		if(scope.getScopeModel().equals(leafScopeModel)) {
			return 0;
		}
		return scopeDAOService.getEnabledDescendantsByScopeModelIdCount(leafScopeModel.getId(), scope.getPk());
	}

	@Override
	public Map<Scope, Integer> getLeafCount(final Collection<Scope> scopes) {
		final var leafScopeModel = studyService.getStudy().getLeafScopeModel();
		//shortcut: leaf scopes cannot contain any leaves
		final var leavesCountByScope = new HashMap<Scope, Integer>();
		final var nonLeafScopePks = new ArrayList<Long>();
		for(final var scope : scopes) {
			if(scope.getScopeModel().equals(leafScopeModel)) {
				leavesCountByScope.put(scope, 0);
			}
			else {
				nonLeafScopePks.add(scope.getPk());
			}
		}
		//retrieve number of leaves for non leaf scopes
		if(!nonLeafScopePks.isEmpty()) {
			final var scopeByPks = scopes.stream().collect(Collectors.toMap(Scope::getPk, Function.identity()));
			for(final var entry : scopeDAOService.getEnabledDescendantsByScopeModelIdCount(leafScopeModel.getId(), nonLeafScopePks).entrySet()) {
				leavesCountByScope.put(scopeByPks.get(entry.getKey()), entry.getValue());
			}
		}
		return leavesCountByScope;
	}

	@Override
	public Optional<Timeframe> getRelationTimeframe(final Scope ancestor, final Scope descendant) {
		if(ancestor.equals(descendant)) {
			return Optional.of(new Timeframe(Optional.of(descendant.getStartDate()), Optional.empty()));
		}

		final var relations = getAncestorRelatedRelations(ancestor, descendant).values();
		if(relations.isEmpty()) {
			return Optional.empty();
		}

		//retrieve start date: null or the oldest start date
		final Optional<ZonedDateTime> startDate;
		if(relations.stream().map(ScopeRelation::getStartDate).anyMatch(Objects::isNull)) {
			startDate = Optional.empty();
		}
		else {
			startDate = relations.stream().map(ScopeRelation::getStartDate).min(ZonedDateTime::compareTo);
		}

		//retrieve stop date: null or the newest end date
		final Optional<ZonedDateTime> stopDate;
		if(relations.stream().map(ScopeRelation::getEndDate).anyMatch(Objects::isNull)) {
			stopDate = Optional.empty();
		}
		else {
			stopDate = relations.stream().map(ScopeRelation::getEndDate).max(ZonedDateTime::compareTo);
		}

		final var timeframe = new Timeframe(startDate, stopDate);
		return Optional.of(timeframe);
	}

	@Override
	public void moveBackPhysicalDate(final Scope scope, final ZonedDateTime date, final DatabaseActionContext context, final String rationale) {
		if(!isRootScope(scope)) {
			//find oldest relation
			final var relation = scopeRelationService.getAllParentRelations(scope).stream()
				.filter(r -> !scopeRelationService.isVirtual(r))
				.sorted()
				.findFirst()
				.orElseThrow();
			if(relation.getStartDate().isAfter(date)) {
				relation.setStartDate(date);
				scopeRelationService.saveScopeRelation(relation, context, rationale);
				moveBackPhysicalDate(scopeRelationService.getParent(relation), date, context, rationale);
			}
		}
	}

	@Override
	public ZonedDateTime getDefaultStartDate(final Scope scope) {
		final var relation = scopeRelationService.getDefaultParentRelation(scope);
		if(relation == null) {
			return null;
		}

		return relation.getStartDate();
	}

	@Override
	public ZonedDateTime getDefaultStopDate(final Scope scope) {
		final var relation = scopeRelationService.getDefaultParentRelation(scope);
		if(relation == null) {
			return null;
		}

		return relation.getEndDate();
	}

	@Override
	public int getTreeDepth(final Scope scope) {
		var count = 1;
		var s = scope;
		while(s != null) {
			count++;
			s = scopeRelationService.getDefaultParent(s);
		}

		return count;
	}

	/**
	 * Auxiliary method that checks the compatibility of the scope model and the scope parent
	 * <p>
	 * If they are not compatible a specific exception is thrown, depending on the reason
	 * for incompatibility
	 *
	 * @param model  The scope model
	 * @param parent The parent
	 * @throws ImpossibleScopeModelPathException         Thrown if no path is possible
	 * @throws ImpossibleVirtualChainException Thrown if no virtual chain is possible
	 * @throws MaxDescendantScopesReachedException       Thrown
	 */
	private void checkModelParentCorrectness(final ScopeModel model, final Scope parent) {
		// check if model and parent are compatible
		if(!model.isChildOf(parent.getScopeModel())) {
			throw new ImpossibleScopeModelPathException(model, parent.getScopeModel());
		}

		// check if parent is virtual
		if(parent.getVirtual() && !model.isVirtual()) {
			throw new ImpossibleVirtualChainException(model, parent);
		}

		// check if max number is not reached
		if(model.getMaxNumber() != null && model.getMaxNumber() != 0 && scopeDAOService.getScopesByScopeModelIdCount(model.getId()) >= model.getMaxNumber()) {
			throw new MaxDescendantScopesReachedException(model);
		}

		// check if leaf max number is not reached
		if(model.isLeaf()) {
			final List<Scope> ancestors = new ArrayList<>(scopeRelationService.getAncestors(parent));
			ancestors.add(parent);
			for(final var ancestor : ancestors) {
				if(ancestor.getMaxNumber() != null) {
					final var leafNumber = scopeDAOService.getEnabledDescendantsByScopeModelIdCount(model.getId(), ancestor.getPk());
					if(leafNumber >= ancestor.getMaxNumber()) {
						throw new MaxDescendantScopesReachedException(model, ancestor);
					}
				}
			}
		}
	}

	/**
	 * Generate a new scope code
	 *
	 * @param model          The scope model
	 * @param parent         The parent
	 * @param siblingsNumber The quantity of siblings
	 * @return A newly generated code
	 */
	private String generateCode(final ScopeModel model, final Scope parent, final long siblingsNumber, final long sameScopeModelNumber) {
		var scopeCode = model.getScopeFormat();
		if(scopeCode.contains("${parent}")) {
			scopeCode = scopeCode.replace("${parent}", StringUtils.defaultString(parent.getCode()));
		}

		if(scopeCode.contains("${model}")) {
			scopeCode = scopeCode.replace("${model}", model.getId());
		}

		if(scopeCode.contains("${siblingsNumber}")) {
			scopeCode = scopeCode.replace("${siblingsNumber}", Long.toString(siblingsNumber));
		}

		if(scopeCode.contains("${siblingsNumber:")) {
			final var attributePattern = new StringBuffer();

			final var matcher = siblingsPattern.matcher(scopeCode);
			while(matcher.find()) {
				final var format = String.format("%%0%sd", matcher.group(1));
				matcher.appendReplacement(attributePattern, String.format(format, siblingsNumber));
			}
			matcher.appendTail(attributePattern);
			scopeCode = attributePattern.toString();
		}

		if(scopeCode.contains("${sameScopeModelNumber}")) {
			scopeCode = scopeCode.replace("${sameScopeModelNumber}", Long.toString(sameScopeModelNumber));
		}

		if(scopeCode.contains("${sameScopeModelNumber:")) {
			final var attributePattern = new StringBuffer();

			final var matcher = sameScopeModelPattern.matcher(scopeCode);
			while(matcher.find()) {
				final var format = String.format("%%0%sd", matcher.group(1));
				matcher.appendReplacement(attributePattern, String.format(format, sameScopeModelNumber));
			}
			matcher.appendTail(attributePattern);
			scopeCode = attributePattern.toString();
		}

		if(scopeCode.contains("${checksum}")) {
			final var checkChar = generateCheckCharacter(scopeCode.replace("${checksum}", ""));
			scopeCode = scopeCode.replace("${checksum}", checkChar);
		}
		return scopeCode;
	}

	/**
	 * Generates a check character for the input string using the pre-defined alphabet and Luhn's Mod N algorithm.
	 * For more info: <a href="https://en.wikipedia.org/wiki/Luhn_mod_N_algorithm">...</a>
	 *
	 * @param input Input string
	 * @return A check character based on the pre-defined alphabet.
	 */
	private String generateCheckCharacter(final String input) {
		final var N = ALPHA_CHARACTERS.length();

		final var sum = calculateSum(input, false);

		final var remainder = sum % N;
		final var checkValue = (N - remainder) % N;

		return String.valueOf(ALPHA_CHARACTERS.charAt(checkValue));
	}

	/**
	 * Auxiliary method for the check character generation/verification.
	 *
	 * @param input    The input
	 * @param validate Validate
	 * @return A calculated sum
	 */
	private int calculateSum(final String input, final boolean validate) {
		var sum = 0;
		var factor = validate ? 1 : 2;
		final var N = ALPHA_CHARACTERS.length();

		for(var i = input.length() - 1; i >= 0; i--) {
			int charValue;
			if(ALPHA_CHARACTERS.indexOf(input.charAt(i)) > 0) {
				charValue = ALPHA_CHARACTERS.indexOf(input.charAt(i));
			}
			else {
				charValue = input.charAt(i);
			}
			charValue *= factor;

			sum += Math.floor(charValue / N) + charValue % N;
			factor = factor == 2 ? 1 : 2;
		}

		return sum;
	}

	/**
	 * Get ancestor related relations
	 *
	 * @param ancestor   The ancestor
	 * @param descendant The descendant
	 * @return Scope relations mapped by scope pk
	 */
	private Map<Long, ScopeRelation> getAncestorRelatedRelations(final Scope ancestor, final Scope descendant) {
		final Map<Long, ScopeRelation> results = new HashMap<>();
		scopeRelationService.getAllParentRelations(descendant).forEach(relation -> {
			final var parentScope = scopeRelationService.getParent(relation);
			if(parentScope.equals(ancestor) || scopeRelationService.getAncestors(parentScope).stream().anyMatch(a -> a.getPk().equals(ancestor.getPk()))) {
				results.put(parentScope.getPk(), relation);
			}
		});

		return results;
	}

	private void lockScope(final Scope scope, final DatabaseActionContext context, final String rationale) {
		scope.setLocked(true);
		scopeDAOService.saveScope(scope, context, rationale);
	}

	private void unlockScope(final Scope scope, final DatabaseActionContext context, final String rationale) {
		scope.setLocked(false);
		scopeDAOService.saveScope(scope, context, rationale);
	}
}
