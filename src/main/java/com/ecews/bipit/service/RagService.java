package com.ecews.bipit.service;

import org.commonmark.node.BulletList;
import org.commonmark.node.ListItem;
import org.commonmark.node.Paragraph;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import org.commonmark.parser.Parser;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Logger log = LoggerFactory.getLogger(RagService.class);


    public RagService(ChatClient.Builder chatBuilder, VectorStore vectorStore) {
        this.chatClient = chatBuilder.build();
        this.vectorStore = vectorStore;
    }

    public Flux<String> streamChat(String message) {
        log.info("generating response for {}", message);
        var prompt = createPrompt(message);
        // Initialize CommonMark parser and HTML renderer
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return chatClient.prompt(prompt).stream().content()
                .bufferUntil(s -> s.endsWith(" ")) // Buffer by sentence or meaningful units
                .map(list -> String.join("", list)) // Join buffered parts into a single string
                .map(content -> {
                    String adjustedContent = content.replaceAll(" - ", "\n - ")
                            .replaceAll("important keyword", "**important keyword**")  // Make keywords bold
                            .replaceAll("\\b(Note|Warning):\\b", "**$1:**")
                            .replaceAll("\\s*\\n{2,}", "\n\n- ")
                            .replaceAll("(?i)\\bNote:\\b", "\n\n### Note:\n\n")  // Convert "Note:" to a heading
                            .replaceAll("(?i)\\bImportant:\\b", "\n\n- **Important:** "); // Convert to bullet points// Bold keywords like "Note" or "Warning"// Example for creating bullet lists
                    var document = parser.parse(adjustedContent);

                    // Optionally, make specific content modifications programmatically
                    BulletList bulletList = new BulletList();
                    ListItem item = new ListItem();
                    Paragraph paragraph = new Paragraph();
                    item.appendChild(paragraph);
                    bulletList.appendChild(item);
                    document.appendChild(bulletList);

                    return renderer.render(document);
                });
    }


    private Prompt createPrompt(String message) {
        SearchRequest searchRequest = SearchRequest.query(message);
        var documents = getDocuments(this.vectorStore.similaritySearch(searchRequest));
        String SYSTEM_PROMPT = """
                You are a helpful and friendly AI assistant who can answer questions about PEPFAR's Related documents and information.
                Use the information from the DOCUMENTS section to provide accurate answers, as if you possess this knowledge innately.
                If unsure or if the answer isn't found in the DOCUMENTS section, simply state that you don't know the answer.  
                DOCUMENTS:
                {documents}
                """;
        var systemMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documents));
        var userMessage = new UserMessage(message);
        return new Prompt(String.valueOf(List.of(systemMessage, userMessage)));
    }





    private String getDocuments(List<Document> documentList) {
        return documentList.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
