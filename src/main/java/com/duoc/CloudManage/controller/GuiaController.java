package com.duoc.CloudManage.controller;


import com.duoc.CloudManage.dto.GuiaRequestDTO;
import com.duoc.CloudManage.model.GuiaDespacho;
import com.duoc.CloudManage.service.GuiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/guias")
@RequiredArgsConstructor
public class GuiaController {

    private final GuiaService guiaService;

    // POST /guias — crear guía y guardar PDF en EFS
    @PostMapping
    public ResponseEntity<GuiaDespacho> crear(
            @RequestBody GuiaRequestDTO dto) {
        return ResponseEntity
                .status(201)
                .body(guiaService.crearGuia(dto));
    }

    // POST /guias/subir/{id} — subir guía de EFS a S3
    @PostMapping("/subir/{id}")
    public ResponseEntity<GuiaDespacho> subirS3(
            @PathVariable String id) throws IOException {
        return ResponseEntity.ok(guiaService.subirAws3(id));
    }

    // GET /guias/descargar/{id} — descargar PDF desde S3
    @GetMapping("/descargar/{id}")
    public ResponseEntity<byte[]> descargar(
            @PathVariable String id,
            @RequestParam String transportista) throws IOException {
        byte[] pdf = guiaService.descargar(id, transportista);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"guia-" + id + ".pdf\"")
                .body(pdf);
    }

    // PUT /guias/actualizar/{id} — actualizar guía en S3
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<GuiaDespacho> actualizar(
            @PathVariable String id,
            @RequestBody GuiaRequestDTO dto) {
        return ResponseEntity.ok(guiaService.actualizar(id, dto));
    }

    // DELETE /guias/eliminar/{id} — eliminar guía de S3
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {
        guiaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // GET /guias/consultar — historial con filtros opcionales
    @GetMapping("/consultar")
    public ResponseEntity<List<GuiaDespacho>> historial(
            @RequestParam(required = false) String transportista,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(guiaService.historial(transportista, fecha));
    }

    // GET /guias/buscar?numeroGuia=G-1234567890 — buscar guía por número
    @GetMapping("/buscar")
    public ResponseEntity<GuiaDespacho> buscarPorNumero(
            @RequestParam String numeroGuia) {
        return ResponseEntity.ok(guiaService.buscarPorNumero(numeroGuia));
    }
}