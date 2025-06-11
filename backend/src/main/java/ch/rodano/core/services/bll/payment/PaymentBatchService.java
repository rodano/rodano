package ch.rodano.core.services.bll.payment;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;

import ch.rodano.core.model.payment.PaymentBatch;
import ch.rodano.core.model.scope.Scope;

public interface PaymentBatchService {

	//TODO move this in scope service and use a scope pk
	Scope getScope(PaymentBatch batch);

	HSSFWorkbook getExcelExport(PaymentBatch batch, String... languages);

	Document getExportForXml(PaymentBatch batch, String... languages) throws Exception;

}
