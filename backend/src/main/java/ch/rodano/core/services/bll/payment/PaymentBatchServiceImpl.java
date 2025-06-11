package ch.rodano.core.services.bll.payment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import ch.rodano.configuration.model.dataset.DatasetModel;
import ch.rodano.configuration.model.payment.Payable;
import ch.rodano.configuration.model.payment.PaymentStep;
import ch.rodano.configuration.utils.ExportableUtils;
import ch.rodano.core.model.dataset.Dataset;
import ch.rodano.core.model.payment.Payment;
import ch.rodano.core.model.payment.PaymentBatch;
import ch.rodano.core.model.scope.Scope;
import ch.rodano.core.services.bll.dataset.DatasetService;
import ch.rodano.core.services.bll.event.EventService;
import ch.rodano.core.services.bll.field.FieldService;
import ch.rodano.core.services.bll.scope.ScopeService;
import ch.rodano.core.services.bll.study.StudyService;
import ch.rodano.core.services.bll.workflowStatus.WorkflowStatusService;
import ch.rodano.core.services.dao.payment.PaymentDAOService;
import ch.rodano.core.services.dao.scope.ScopeDAOService;
import ch.rodano.core.services.dao.workflow.WorkflowStatusDAOService;

@Service
public class PaymentBatchServiceImpl implements PaymentBatchService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DecimalFormat NUMBER_FORMATTER;
	static {
		NUMBER_FORMATTER = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		NUMBER_FORMATTER.applyPattern("0.0");
	}

	private final StudyService studyService;
	private final ScopeService scopeService;
	private final ScopeDAOService scopeDAOService;
	private final EventService eventService;
	private final DatasetService datasetService;
	private final FieldService fieldService;
	private final PaymentService paymentService;
	private final PaymentDAOService paymentDAOService;
	private final WorkflowStatusService workflowStatusService;
	private final WorkflowStatusDAOService workflowStatusDAOService;

	public PaymentBatchServiceImpl(
		final ScopeService scopeService,
		final EventService eventService,
		final DatasetService datasetService,
		final FieldService fieldService,
		final PaymentService paymentService,
		final PaymentDAOService paymentDAOService,
		final WorkflowStatusService workflowStatusService,
		final ScopeDAOService scopeDAOService,
		final WorkflowStatusDAOService workflowStatusDAOService,
		final StudyService studyService
	) {
		this.studyService = studyService;
		this.scopeService = scopeService;
		this.scopeDAOService = scopeDAOService;
		this.eventService = eventService;
		this.datasetService = datasetService;
		this.fieldService = fieldService;
		this.paymentService = paymentService;
		this.paymentDAOService = paymentDAOService;
		this.workflowStatusService = workflowStatusService;
		this.workflowStatusDAOService = workflowStatusDAOService;
	}

	//TODO move this in scope service and use a scope pk
	@Override
	public Scope getScope(final PaymentBatch batch) {
		return scopeDAOService.getScopeById(batch.getScopeId());
	}

	public List<Payment> getPayments(final PaymentBatch batch, final PaymentStep step) {
		return paymentDAOService.getPaymentByBatchPk(batch.getPk()).stream()
			.filter(p -> p.getStepId().equals(step.getId()))
			.sorted()
			.toList();
	}

	public List<Payable> getPayables(final List<Payment> payments) {
		final List<Payable> payables = new ArrayList<>();
		for(final var payment : payments) {
			final var paymentPayables = paymentService.getPayables(payment);
			for(final var payable : paymentPayables) {
				if(!payables.contains(payable)) {
					payables.add(payable);
				}
			}
		}
		return payables;
	}

	public List<PaymentStep> getSteps(final List<Payment> payments) {
		return payments.stream().map(Payment::getPaymentStep).distinct().toList();
	}

	private String getCurrency(final Dataset configurationDataset) {
		final var fieldModel = configurationDataset.getDatasetModel().getFieldModel("CURRENCY");
		return fieldService.get(configurationDataset, fieldModel).getValue();
	}

	public Double getVat(final Dataset configurationDataset) {
		final var fieldModel = configurationDataset.getDatasetModel().getFieldModel("VAT");
		return Double.valueOf(fieldService.get(configurationDataset, fieldModel).getValue());
	}

	public String getIBAN(final Dataset configurationDataset, final Payable payable) {
		final var fieldModelId = String.format("IBAN_%s", payable.getPayableModelId().toUpperCase());
		final var fieldModel = configurationDataset.getDatasetModel().getFieldModel(fieldModelId);
		return fieldService.get(configurationDataset, fieldModel).getValue();
	}

	public String getBIC(final Dataset configurationDataset, final Payable payable) {
		final var fieldModelId = String.format("BIC_%s", payable.getPayableModelId().toUpperCase());
		final var fieldModel = configurationDataset.getDatasetModel().getFieldModel(fieldModelId);
		return fieldService.get(configurationDataset, fieldModel).getValue();
	}

	public String getSwift(final Dataset configurationDataset, final Payable payable) {
		final var fieldModelId = String.format("SWIFT_%s", payable.getPayableModelId().toUpperCase());
		final var fieldModel = configurationDataset.getDatasetModel().getFieldModel(fieldModelId);
		return fieldService.get(configurationDataset, fieldModel).getValue();
	}

	public String getAccountNo(final Dataset configurationDataset, final Payable payable) {
		final var fieldModelId = String.format("ACCOUNT_NO_%s", payable.getPayableModelId().toUpperCase());
		final var fieldModel = configurationDataset.getDatasetModel().getFieldModel(fieldModelId);
		return fieldService.get(configurationDataset, fieldModel).getValue();
	}

	public String getSpecialInstruction(final Dataset configurationDataset, final Payable payable) {
		final var fieldModelId = String.format("SPEC_INSTR_%s", payable.getPayableModelId().toUpperCase());
		final var fieldModel = configurationDataset.getDatasetModel().getFieldModel(fieldModelId);
		return fieldService.get(configurationDataset, fieldModel).getValue();
	}

	public Double getTotalVat(final List<Payment> payments, final Double vat) {
		return getTotalValue(payments) * (vat / 100);
	}

	public Double getTotalVatByPayable(final List<Payment> payments, final Payable payable, final Double vat) {
		return getTotalValue(payments, payable) * (vat / 100);
	}

	public double getTotalValue(final List<Payment> payments) {
		return payments.stream().mapToDouble(paymentService::getTotalValue).sum();
	}

	public double getTotalValue(final List<Payment> payments, final Payable payable) {
		return payments.stream().mapToDouble(p -> paymentService.getTotalValue(p, payable)).sum();
	}

	public double getTotalValue(final List<Payment> payments, final PaymentStep step) {
		return payments.stream()
			.filter(p -> p.getStepId().equals(step.getId()))
			.mapToDouble(paymentService::getTotalValue)
			.sum();
	}

	public double getTotalValue(final List<Payment> payments, final Payable payable, final PaymentStep step) {
		return payments.stream()
			.filter(p -> p.getStepId().equals(step.getId()))
			.mapToDouble(p -> paymentService.getTotalValue(p, payable))
			.sum();
	}

	public DatasetModel getDatasetModel(final PaymentBatch batch) {
		return studyService.getStudy().getDatasetModel(batch.getPlanId());
	}

	@Override
	public HSSFWorkbook getExcelExport(final PaymentBatch batch, final String... languages) {
		final var workbook = new HSSFWorkbook();
		createSheetOnWorkbook(batch, workbook, "Payments", languages);
		return workbook;
	}

	public void createSheetOnWorkbook(final PaymentBatch batch, final HSSFWorkbook workbook, final String sheetTitle, final String... languages) {
		final var scope = getScope(batch);
		final var payments = paymentDAOService.getPaymentByBatchPk(batch.getPk());

		final var sheet = workbook.createSheet(sheetTitle);
		sheet.setDefaultColumnWidth((short) 30);

		final var headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());

		final var headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.LEFT);
		headerStyle.setFont(headerFont);

		final var paymentFont = workbook.createFont();
		paymentFont.setBold(true);
		paymentFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());

		final var paymentStyle = workbook.createCellStyle();
		paymentStyle.setAlignment(HorizontalAlignment.CENTER);
		paymentStyle.setFont(headerFont);
		paymentStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.GREY_80_PERCENT.getIndex());

		var numRow = 0;

		final var batchRow = sheet.createRow(numRow++);
		final var batchModelCell = batchRow.createCell((short) 0);
		batchModelCell.setCellValue("Batch ID");
		batchModelCell.setCellStyle(headerStyle);
		batchRow.createCell((short) 1).setCellValue(batch.getPk());

		final var scopeRow = sheet.createRow(numRow++);

		final var scopeModelCell = scopeRow.createCell((short) 0);
		scopeModelCell.setCellValue(scope.getScopeModel().getLocalizedShortname(languages));
		scopeModelCell.setCellStyle(headerStyle);
		scopeRow.createCell((short) 1).setCellValue(scope.getShortname());

		final var creationDateRow = sheet.createRow(numRow++);
		final var creationDateCell = creationDateRow.createCell((short) 0);
		creationDateCell.setCellValue("Batch creation");
		creationDateCell.setCellStyle(headerStyle);
		creationDateRow.createCell((short) 1).setCellValue(DATE_FORMATTER.format(batch.getCreationTime()));

		final var statusRow = sheet.createRow(numRow++);
		final var statusCell = statusRow.createCell((short) 0);
		statusCell.setCellValue("Status");
		statusCell.setCellStyle(headerStyle);
		statusRow.createCell((short) 1).setCellValue(batch.getStatus().toString());

		final var paymentDateRow = sheet.createRow(numRow++);
		final var paymentDateCell = paymentDateRow.createCell((short) 0);
		paymentDateCell.setCellValue("Payment date");
		paymentDateCell.setCellStyle(headerStyle);
		paymentDateRow.createCell((short) 1).setCellValue(batch.getPaymentDate() != null ? DATE_FORMATTER.format(batch.getPaymentDate()) : "NA");

		final var paymentNumberRow = sheet.createRow(numRow++);
		final var paymentNumberCell = paymentNumberRow.createCell((short) 0);
		paymentNumberCell.setCellValue("Number of payments");
		paymentNumberCell.setCellStyle(headerStyle);
		paymentNumberRow.createCell((short) 1).setCellValue(Integer.toString(payments.size()));

		final var configurationDatasetModel = getDatasetModel(batch);
		final var configurationDataset = datasetService.get(scope, configurationDatasetModel);

		final var currencyRow = sheet.createRow(numRow++);
		final var currencyCell = currencyRow.createCell((short) 0);
		currencyCell.setCellValue("Currency");
		currencyCell.setCellStyle(headerStyle);
		currencyRow.createCell((short) 1).setCellValue(getCurrency(configurationDataset));

		final var vat = getVat(configurationDataset);
		final var vatRow = sheet.createRow(numRow++);
		final var vatCell = vatRow.createCell((short) 0);
		vatCell.setCellValue("VAT");
		vatCell.setCellStyle(headerStyle);
		vatRow.createCell((short) 1).setCellValue(vat);

		final var payables = getPayables(payments);

		for(final var payable : payables) {
			final var IBANRow = sheet.createRow(numRow++);
			final var IBANCell = IBANRow.createCell((short) 0);
			IBANCell.setCellValue(String.format("%s IBAN", payable.getLocalizedShortname(languages)));
			IBANCell.setCellStyle(headerStyle);
			IBANRow.createCell((short) 1).setCellValue(getIBAN(configurationDataset, payable));

			final var BICRow = sheet.createRow(numRow++);
			final var BICCell = BICRow.createCell((short) 0);
			BICCell.setCellValue(String.format("%s BIC", payable.getLocalizedShortname(languages)));
			BICCell.setCellStyle(headerStyle);
			BICRow.createCell((short) 1).setCellValue(getBIC(configurationDataset, payable));

			final var SWIFTRow = sheet.createRow(numRow++);
			final var SWIFTCell = SWIFTRow.createCell((short) 0);
			SWIFTCell.setCellValue(String.format("%s Swift", payable.getLocalizedShortname(languages)));
			SWIFTCell.setCellStyle(headerStyle);
			SWIFTRow.createCell((short) 1).setCellValue(getSwift(configurationDataset, payable));

			final var AccountNoRow = sheet.createRow(numRow++);
			final var AccountNoCell = AccountNoRow.createCell((short) 0);
			AccountNoCell.setCellValue(String.format("%s Account No.", payable.getLocalizedShortname(languages)));
			AccountNoCell.setCellStyle(headerStyle);
			AccountNoRow.createCell((short) 1).setCellValue(getAccountNo(configurationDataset, payable));

			final var SpecialInstructionRow = sheet.createRow(numRow++);
			final var SpecialInstructionCell = SpecialInstructionRow.createCell((short) 0);
			SpecialInstructionCell.setCellValue(String.format("%s Special Instruction", payable.getLocalizedShortname(languages)));
			SpecialInstructionCell.setCellStyle(headerStyle);
			SpecialInstructionRow.createCell((short) 1).setCellValue(getSpecialInstruction(configurationDataset, payable));
		}

		for(final var payable : payables) {
			final var payableRow = sheet.createRow(numRow++);
			final var payableCell = payableRow.createCell((short) 0);
			payableCell.setCellValue(payable.getLocalizedShortname(languages));
			payableCell.setCellStyle(headerStyle);
			payableRow.createCell((short) 1).setCellValue(getTotalValue(payments, payable) + getTotalVatByPayable(payments, payable, vat));
		}

		numRow += 3;

		final var firstWorkflowStatus = workflowStatusDAOService.getWorkflowStatusByPk(payments.get(0).getWorkflowStatusFk());

		final var paymentHeadRow = sheet.createRow(numRow++);
		final var paymentScopeHeadCell = paymentHeadRow.createCell((short) 0);
		try {
			final var paymentScope = scopeService.get(firstWorkflowStatus);
			paymentScopeHeadCell.setCellValue(paymentScope.getScopeModel().getLocalizedShortname(languages));
		}
		catch(final Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			paymentScopeHeadCell.setCellValue("");
		}
		paymentScopeHeadCell.setCellStyle(paymentStyle);

		final var paymentWorkflowableHeadCell = paymentHeadRow.createCell((short) 1);
		paymentWorkflowableHeadCell.setCellValue(workflowStatusService.getWorkflowable(firstWorkflowStatus).getWorkflowableModel().getEntity().toString());
		paymentWorkflowableHeadCell.setCellStyle(paymentStyle);

		final var paymentWorkflowableTotalHeadCell = paymentHeadRow.createCell((short) 2);
		paymentWorkflowableTotalHeadCell.setCellValue("Amount");
		paymentWorkflowableTotalHeadCell.setCellStyle(paymentStyle);

		for(final var payment : payments) {
			final var paymentRow = sheet.createRow(numRow++);
			final var paymentScopeCell = paymentRow.createCell((short) 0);
			paymentScopeCell.setCellValue(paymentService.getScope(payment).getCodeAndShortname());
			final var paymentWorkflowableCell = paymentRow.createCell((short) 1);
			final var ws = workflowStatusDAOService.getWorkflowStatusByPk(payment.getWorkflowStatusFk());
			final var workflowable = workflowStatusService.getWorkflowable(ws);
			paymentWorkflowableCell.setCellValue(workflowable.getWorkflowableModel().getId());
			final var paymentWorkflowableTotalCell = paymentRow.createCell((short) 2);
			paymentWorkflowableTotalCell.setCellValue(paymentService.getTotalValue(payment));
		}
	}

	@Override
	public Document getExportForXml(final PaymentBatch batch, final String... languages) throws Exception {
		final var scope = getScope(batch);
		final var payments = paymentDAOService.getPaymentByBatchPk(batch.getPk());
		final var configurationDatasetModel = getDatasetModel(batch);
		final var configurationDataset = datasetService.get(scope, configurationDatasetModel);

		final var factory = DocumentBuilderFactory.newInstance();
		final var builder = factory.newDocumentBuilder();
		final var doc = builder.newDocument();

		final var batchE = doc.createElement("batch");
		batchE.setAttribute("id", String.valueOf(batch.getPk()));
		batchE.setAttribute("creationDate", batch.getCreationTime() != null ? DATE_FORMATTER.format(batch.getCreationTime()) : "");
		batchE.setAttribute("paymentDate", batch.getPaymentDate() != null ? DATE_FORMATTER.format(batch.getPaymentDate()) : "");
		batchE.setAttribute("printedDate", batch.getPrintedDate() != null ? DATE_FORMATTER.format(batch.getPrintedDate()) : "");
		batchE.setAttribute("closedDate", batch.getClosedDate() != null ? DATE_FORMATTER.format(batch.getClosedDate()) : "");

		final var paymentInfoE = doc.createElement("paymentInfo");

		for(final var v : eventService.getAll(scope)) {
			for(final var dataset : datasetService.getAll(v)) {
				if(dataset.getDatasetModelId().equals(batch.getPlan().getId())) {
					for(final var pm : batch.getPlan().getPayableModels()) {

						final var BIC_KEY = String.format("BIC_%s", pm.getId().toUpperCase());
						final var bicFieldModel = dataset.getDatasetModel().getFieldModel(BIC_KEY);
						final var BIC = doc.createElement(BIC_KEY);
						BIC.setTextContent(fieldService.get(dataset, bicFieldModel).getValue());

						final var SWIFT_KEY = String.format("SWIFT_%s", pm.getId().toUpperCase());
						final var swiftFieldModel = dataset.getDatasetModel().getFieldModel(SWIFT_KEY);
						final var SWIFT = doc.createElement(SWIFT_KEY);
						SWIFT.setTextContent(fieldService.get(dataset, swiftFieldModel).getValue());

						final var IBAN_KEY = String.format("IBAN_%s", pm.getId().toUpperCase());
						final var ibanFieldModel = dataset.getDatasetModel().getFieldModel(IBAN_KEY);
						final var IBAN = doc.createElement(IBAN_KEY);
						IBAN.setTextContent(fieldService.get(dataset, ibanFieldModel).getValue());

						final var ACCOUNT_NO_KEY = String.format("ACCOUNT_NO_%s", pm.getId().toUpperCase());
						final var accountNoFieldModel = dataset.getDatasetModel().getFieldModel(ACCOUNT_NO_KEY);
						final var ACCOUNT_NO = doc.createElement(ACCOUNT_NO_KEY);
						ACCOUNT_NO.setTextContent(fieldService.get(dataset, accountNoFieldModel).getValue());

						final var SPEC_INSTR_KEY = String.format("SPEC_INSTR_%s", pm.getId().toUpperCase());
						final var specInstrFieldModel = dataset.getDatasetModel().getFieldModel(SPEC_INSTR_KEY);
						final var SPEC_INSTR = doc.createElement(SPEC_INSTR_KEY);
						SPEC_INSTR.setTextContent(fieldService.get(dataset, specInstrFieldModel).getValue());

						paymentInfoE.appendChild(BIC);
						paymentInfoE.appendChild(SWIFT);
						paymentInfoE.appendChild(IBAN);
						paymentInfoE.appendChild(ACCOUNT_NO);
						paymentInfoE.appendChild(SPEC_INSTR);
					}
				}
			}
		}

		final var VAT = doc.createElement("VAT");
		VAT.setTextContent(NUMBER_FORMATTER.format(getVat(configurationDataset)));
		paymentInfoE.appendChild(VAT);

		batchE.appendChild(paymentInfoE);

		final var scopeE = doc.createElement("scope");

		scopeE.setAttribute("id", scope.getId());
		scopeE.setAttribute("code", scope.getCode());

		final var scopeNameE = doc.createElement("name");
		scopeNameE.setTextContent(scope.getCodeAndShortname());
		scopeE.appendChild(scopeNameE);
		batchE.appendChild(scopeE);

		final var amountE = doc.createElement("amount");
		amountE.setTextContent(NUMBER_FORMATTER.format(getTotalValue(payments)));
		batchE.appendChild(amountE);

		batchE.appendChild(ExportableUtils.getExportForXml(doc, batch.getPlan(), languages));

		final var paymentsE = doc.createElement("payments");
		for(final var p : payments) {
			final var paymentNode = paymentService.getExportForXml(p, languages).getFirstChild();
			paymentsE.appendChild(doc.importNode(paymentNode, true));
		}
		batchE.appendChild(paymentsE);

		final var payablesE = doc.createElement("payables");
		for(final var pa : getPayables(payments)) {
			final var payableE = doc.createElement("payable");

			final var payableNameE = doc.createElement("name");
			payableNameE.setTextContent(pa.getPayableModel().getLocalizedShortname(languages));

			final var payableAmountE = doc.createElement("amount");
			payableAmountE.setTextContent(NUMBER_FORMATTER.format(getTotalValue(payments, pa)));

			payableE.appendChild(payableNameE);
			payableE.appendChild(payableAmountE);

			payablesE.appendChild(payableE);

		}
		batchE.appendChild(payablesE);

		double totalAmount = 0;
		var totalNumber = 0;

		final var stepsE = doc.createElement("steps");
		for(final var step : batch.getPlan().getSteps()) {
			final var stepE = doc.createElement("step");

			final var payableAmountE = doc.createElement("amount");
			payableAmountE.setTextContent(NUMBER_FORMATTER.format(getTotalValue(payments, step)));

			totalAmount += getTotalValue(payments, step);

			final var stepPayments = payments.stream()
				.filter(p -> p.getStepId().equals(step.getId()))
				.sorted()
				.toList();

			final var nbE = doc.createElement("nb");
			nbE.setTextContent(String.valueOf(stepPayments.size()));

			totalNumber += stepPayments.size();

			stepE.appendChild(payableAmountE);
			stepE.appendChild(nbE);
			stepE.appendChild(ExportableUtils.getExportForXml(doc, step, languages));

			stepsE.appendChild(stepE);
		}
		batchE.appendChild(stepsE);

		// TOTAL
		final var totalStepsE = doc.createElement("total_steps");
		final var payableAmountE = doc.createElement("amount");
		final var vatAmountE = doc.createElement("vatAmount");
		final var vat = getVat(configurationDataset);
		payableAmountE.setTextContent(NUMBER_FORMATTER.format(totalAmount + getTotalVat(payments, vat)));
		vatAmountE.setTextContent(NUMBER_FORMATTER.format(getTotalVat(payments, vat)));
		final var nbE = doc.createElement("nb");
		nbE.setTextContent(String.valueOf(totalNumber));
		totalStepsE.appendChild(payableAmountE);
		totalStepsE.appendChild(vatAmountE);
		totalStepsE.appendChild(nbE);
		batchE.appendChild(totalStepsE);
		// END TOTAL

		doc.appendChild(batchE);
		doc.getDocumentElement().normalize();

		return doc;
	}
}
