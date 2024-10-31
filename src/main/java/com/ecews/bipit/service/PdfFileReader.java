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

//    @Value("classpath:National-HIV-Treatment-Guideline.pdf")
//    private Resource pdfResource3;

    @Value("classpath:PEPFAR-Addendum-to-Fiscal-Year-2024-Final.pdf")
    private Resource pdfResource4;

    @Value("classpath:FY25-MER-2.8-Indicator-Reference-Guide.pdf")
    private Resource pdfResource5;

    @Value("classpath:FY25 MER 2.8 Guidance FAQ.pdf")
    private Resource pdfResource6;

    @Value("classpath:Indicator Frequency Table MER v2.8.pdf")
    private Resource pdfResource7;

    @Value("classpath:MER Infographic 2.8.pdf")
    private Resource pdfResource8;

    public PdfFileReader(VectorStore vectorStore, DocumentStatusRepository documentStatusRepository) {
        this.vectorStore = vectorStore;
        this.documentStatusRepository = documentStatusRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
       // processPdfIfNotPopulated("FY24-MER-2.7.pdf", pdfResource1);
        processPdfIfNotPopulated("FY-2024-PEPFAR-Technical-Considerations.pdf", pdfResource2);
        processPdfIfNotPopulated("PEPFAR-Addendum-to-Fiscal-Year-2024-Final.pdf", pdfResource4);
        processPdfIfNotPopulated("FY25-MER-2.8-Indicator-Reference-Guide.pdf", pdfResource5);
        processPdfIfNotPopulated("FY25 MER 2.8 Guidance FAQ.pdf", pdfResource6);
        processPdfIfNotPopulated("Indicator Frequency Table MER v2.8.pdf", pdfResource7);
        processPdfIfNotPopulated("MER Infographic 2.8.pdf", pdfResource8);
    }

    private void processPdfIfNotPopulated(String documentName, Resource pdfResource) {
        Optional<DocumentStatus> docStatus = documentStatusRepository.findByDocumentName(documentName);

        if (docStatus.isEmpty() || !docStatus.get().isPopulated()) {
            System.out.println("populating : "+ documentName);
            processPdf(pdfResource);
            System.out.println("done populating : "+ documentName);
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
        vectorStore.add(textSplitter.apply(pdfReader.get()));
    }
}
