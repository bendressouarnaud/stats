package com.cnmci.stats.controller;

import com.cnmci.stats.beans.QueryResponse;
import com.cnmci.stats.ia.Text2SqlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name="Ia-Controller")
public class IaController {

    // A T T R I B U T E S :
    private final Text2SqlService text2SqlService;


    // M E T H O D S
    @GetMapping("/requete/{requete}")
    public ResponseEntity<QueryResponse> query(@PathVariable String requete) {
        if (requete.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(QueryResponse.error("", null, "Question cannot be empty"));
        }
        return ResponseEntity.ok(text2SqlService.processQuestion(requete));
    }

}
