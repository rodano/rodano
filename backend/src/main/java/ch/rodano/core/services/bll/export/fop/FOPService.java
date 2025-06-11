package ch.rodano.core.services.bll.export.fop;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.w3c.dom.Document;

public interface FOPService {
	void writePDF(final OutputStream out, final Document doc, final InputStream xsl, final Map<String, String> parameters);

}
