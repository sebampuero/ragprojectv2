package com.rag.backend.controller;

import com.rag.backend.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/queue/size")
    public ResponseEntity<Integer> getQueueSize() {
        return ResponseEntity.ok(queueService.getQueueSize());
    }
}
