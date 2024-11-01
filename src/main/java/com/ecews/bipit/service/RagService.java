package com.ecews.bipit.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
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

import java.util.List;
import java.util.Map;
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
        log.info("Generating response for {}", message);
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        // Generate prompt only once
        var prompt = createPrompt(message);
        return processPromptToFormattedFlux(prompt, parser, renderer);
    }

    private Flux<String> processPromptToFormattedFlux(Prompt prompt, Parser parser, HtmlRenderer renderer) {
        return chatClient.prompt(prompt).stream().content()
                .bufferUntil(s -> s.endsWith(" "))
                .map(list -> {
                    // Use StringBuilder for efficient concatenation
                    StringBuilder contentBuilder = new StringBuilder();
                    list.forEach(contentBuilder::append);
                    return contentBuilder.toString();
                })
                .map(content -> {
                    // Consolidate replacements into one regex pattern
                    String adjustedContent = content
                            .replaceAll(" - ", "\n - ")
                            .replaceAll("(?i)(\\bNote:\\b|\\bWarning:\\b|\\bImportant:\\b)", "**$1**")
                            .replaceAll("(?i)\\b(Note|Warning):\\b", "**$1:**")
                            .replaceAll("\\s*\\n{2,}(?![^()]*\\))", "\n\n- ");  // Bullet point creation, avoiding line breaks within parentheses

                    // Parse and render
                    Node document = parser.parse(adjustedContent);
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
