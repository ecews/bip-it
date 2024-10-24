package com.ecews.bipit.service;

import com.ecews.bipit.data.DocumentStatus;
import com.ecews.bipit.repository.DocumentStatusRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PdfFileReader {

    private final VectorStore vectorStore;
    private final DocumentStatusRepository documentStatusRepository;

    @Value("classpath:FY24-MER-2.7.pdf")
    private Resource pdfResource1;

    @Value("classpath:FY-2024-PEPFAR-Technical-Considerations.pdf")
    private Resource pdfResource2;

    @Value("classpath:National-HIV-Treatment-Guideline.pdf")
    private Resource pdfResource3;

    @Value("classpath:PEPFAR-Addendum-to-Fiscal-Year-2024-Final.pdf")
    private Resource pdfResource4;

    public PdfFileReader(VectorStore vectorStore, DocumentStatusRepository documentStatusRepository) {
        this.vectorStore = vectorStore;
        this.documentStatusRepository = documentStatusRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        processPdfIfNotPopulated("FY24-MER-2.7.pdf", pdfResource1);
        processPdfIfNotPopulated("FY-2024-PEPFAR-Technical-Considerations.pdf", pdfResource2);
        processPdfIfNotPopulated("PEPFAR-Addendum-to-Fiscal-Year-2024-Final.pdf", pdfResource4);
    }

    private void processPdfIfNotPopulated(String documentName, Resource pdfResource) {
        Optional<DocumentStatus> docStatus = documentStatusRepository.findByDocumentName(documentName);

        if (docStatus.isEmpty() || !docStatus.get().isPopulated()) {
            processPdf(pdfResource);
            documentStatusRepository.save(new DocumentStatus(documentName, true));
        }
    }

    private void processPdf(Resource pdfResource) {
        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();
        var pdfReader = new PagePdfDocumentReader(pdfResource, config);
        var textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(pdfReader.get()));
    }
}
