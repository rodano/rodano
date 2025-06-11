package ch.rodano.core.services.bll.payment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import ch.rodano.configuration.model.common.Entity;
import ch.rodano.configuration.model.payment.Payable;
import ch.rodano.configuration.model.payment.PaymentDistribution;
import ch.rodano.configuration.model.profile.Profile;
import ch.rodano.configuration.model.rights.Rights;
import ch.rodano.configuration.utils.ExportableUtils;
import ch.rodano.core.model.exception.MissingDataException;
import ch.rodano.core.model.payment.Payment;
import ch.rodano.core.model.payment.PaymentTarget;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.model.scope.ScopeExtension;
import ch.rodano.core.model.user.User;
import ch.rodano.core.model.user.UserSearch;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeRelationService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.payment.PaymentTargetDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.user.UserDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;

@Service
public class PaymentServiceImpl implements PaymentService {
	private static final DecimalFormat NUMBER_FORMATTER;
	static {
		NUMBER_FORMATTER = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		NUMBER_FORMATTER.applyPattern("0.0");
	}

	private final StudyService studyService;
	private final ScopeDAOService scopeDAOService;
	private final ScopeRelationService scopeRelationService;
	private final DatasetService datasetService;
	private final FieldService fieldService;
	private final WorkflowStatusService workflowStatusService;
	private final WorkflowStatusDAOService workflowStatusDAOService;
	private final PaymentTargetService paymentTargetService;
	private final PaymentTargetDAOService paymentTargetDAOService;
	private final UserDAOService userDAOService;

	public PaymentServiceImpl(
		final StudyService studyService,
		final ScopeDAOService scopeDAOService,
		final ScopeRelationService scopeRelationService,
		final DatasetService datasetService,
		final FieldService fieldService,
		final WorkflowStatusService workflowStatusService,
		final PaymentTargetService paymentTargetService,
		final PaymentTargetDAOService paymentTargetDAOService,
		final UserDAOService userDAOService,
		final WorkflowStatusDAOService workflowStatusDAOService
	) {
		this.studyService = studyService;
		this.scopeDAOService = scopeDAOService;
		this.scopeRelationService = scopeRelationService;
		this.datasetService = datasetService;
		this.fieldService = fieldService;
		this.workflowStatusService = workflowStatusService;
		this.workflowStatusDAOService = workflowStatusDAOService;
		this.paymentTargetService = paymentTargetService;
		this.paymentTargetDAOService = paymentTargetDAOService;
		this.userDAOService = userDAOService;
	}

	//TODO move this in scope service
	@Override
	public Scope getScope(final Payment payment) {
		final var ws = workflowStatusDAOService.getWorkflowStatusByPk(payment.getWorkflowStatusFk());
		return scopeDAOService.getScopeByPk(ws.getScopeFk());
	}

	private Scope getConfigurationScope(final Payment payment) throws Exception {
		final List<Scope> ancestors = new ArrayList<>(scopeRelationService.getDefaultAncestors(getScope(payment)));
		Collections.reverse(ancestors);

		for(final var scope : ancestors) {
			if(scope.getScopeModelId().equals(payment.getPlan().getInvoicedScopeModel())) {
				return scope;
			}
		}

		throw new Exception(String.format("Unable to find configuration scope for plan [%s] and step [%s]", payment.getPlan().getId(), payment.getStepId()));
	}

	@Override
	public List<Payable> getPayables(final Payment payment) {
		final List<Payable> payables = new ArrayList<>();

		for(final var paymentTarget : paymentTargetDAOService.getPaymentTargetsByPaymentPk(payment.getPk())) {
			final var payable = paymentTargetService.getPayable(paymentTarget);
			if(!payables.contains(payable)) {
				payables.add(payable);
			}
		}
		return payables;
	}

	@Override
	public double getTotalValue(final Payment payment) {
		return getTotalValue(paymentTargetDAOService.getPaymentTargetsByPaymentPk(payment.getPk()));
	}

	private double getTotalValue(final List<PaymentTarget> targets) {
		return targets.stream().mapToDouble(PaymentTarget::getValue).sum();
	}

	@Override
	public double getTotalValue(final Payment payment, final Payable payable) {
		return getTotalValue(paymentTargetDAOService.getPaymentTargetsByPaymentPk(payment.getPk()), payable);
	}

	private double getTotalValue(final List<PaymentTarget> targets, final Payable payable) {
		return targets.stream()
			.filter(t -> t.getPayableId().equals(payable.getId()))
			.mapToDouble(PaymentTarget::getValue)
			.sum();
	}

	@Override
	public Document getExportForXml(final Payment payment, final String... languages) throws Exception {
		final var targets = paymentTargetDAOService.getPaymentTargetsByPaymentPk(payment.getPk());

		final var factory = DocumentBuilderFactory.newInstance();
		final var builder = factory.newDocumentBuilder();
		final var doc = builder.newDocument();

		final var paymentE = doc.createElement("payment");
		paymentE.setAttribute("id", String.valueOf(payment.getPk()));
		paymentE.setAttribute("status", payment.getStatus());

		final var ws = workflowStatusDAOService.getWorkflowStatusByPk(payment.getWorkflowStatusFk());
		final var scope = scopeDAOService.getScopeByPk(ws.getScopeFk());

		final var scopeE = doc.createElement("scope");
		scopeE.setTextContent(scope.getCodeAndShortname());

		final var workflowable = workflowStatusService.getWorkflowable(ws);

		final var workflowE = doc.createElement("workflow");
		workflowE.setTextContent(workflowable.getWorkflowableModel().getLocalizedShortname(languages));

		final var amountE = doc.createElement("amount");
		amountE.setTextContent(NUMBER_FORMATTER.format(getTotalValue(targets)));

		paymentE.appendChild(scopeE);
		paymentE.appendChild(workflowE);
		paymentE.appendChild(amountE);
		paymentE.appendChild(ExportableUtils.getExportForXml(doc, payment.getPaymentStep(), languages));

		doc.appendChild(paymentE);
		doc.getDocumentElement().normalize();

		return doc;
	}

	@Override
	public final Collection<User> getUsersToNotify(final Payment payment) {
		final var scope = getScope(payment);

		//retrieve interted profiles
		final var profileIds = studyService.getStudy().getProfiles().stream()
			.filter(p -> p.hasRight(Entity.PAYMENT_PLAN, payment.getPlan().getId(), Rights.WRITE))
			.map(Profile::getId)
			.collect(Collectors.toSet());

		final var predicate = new UserSearch()
			.setProfileIds(Optional.of(profileIds))
			.setScopePks(Optional.of(Collections.singleton(scope.getPk())))
			.setExtension(Optional.of(ScopeExtension.ANCESTORS));
		return userDAOService.search(predicate).getObjects();
	}

	private Scope getPayableScope(final Payment payment, final PaymentDistribution distribution) {
		final var scope = getScope(payment);
		final List<Scope> payableScopes = new ArrayList<>(scopeRelationService.getEnabledAncestors(scope, distribution.getScopeModel()));

		if(payableScopes.isEmpty() || payableScopes.size() > 1) {
			return null;
		}

		return payableScopes.get(0);
	}

	@Override
	public Payable getPayable(final Payment payment, final PaymentDistribution distribution) {
		if(distribution.getProfileId() != null) {
			return distribution.getProfile();
		}
		return getPayableScope(payment, distribution);
	}

	@Override
	public List<PaymentTarget> validate(final Payment payment) throws Exception {
		if(payment.isExcluded()) {
			throw new Exception("Unable to validate an excluded payment");
		}

		final List<PaymentTarget> paymentTargets = new ArrayList<>();
		final var configurationDatasetModel = studyService.getStudy().getDatasetModel(payment.getPlanId());
		final var configurationDataset = datasetService.get(getConfigurationScope(payment), configurationDatasetModel);

		for(final var distribution : payment.getPaymentStep().getDistributions()) {
			//retrieve payable
			final var payable = getPayable(payment, distribution);

			//retrive configuration
			final var fieldModelId = String.format("%s_%s", payment.getStepId().toUpperCase(), distribution.getPayableModelId().toUpperCase());
			final var fieldModel = configurationDatasetModel.getFieldModel(fieldModelId);
			final var field = fieldService.get(configurationDataset, fieldModel);
			if(field.isBlank()) {
				throw new MissingDataException(String.format("No configuration for plan [%s] and step [%s] for payable [%s]", payment.getPlan().getId(), payment.getStepId(), payable.getId()));
			}

			final var target = new PaymentTarget();
			target.setPaymentFk(payment.getPk());
			target.setPayableId(payable.getId());
			target.setValue(Double.valueOf(field.getValue()));
		}

		return paymentTargets;
	}
}
