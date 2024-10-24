package com.ecews.bipit.web;

import com.ecews.bipit.service.RagService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/v1/ai")
public class BipitController {
    private final RagService ragService;

    public BipitController(RagService ragService) {
        this.ragService = ragService;
    }


    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String > streamChat1(@RequestParam String message) {
        return ragService.streamChat(message);
    }









}
