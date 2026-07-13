package com.duoc.CloudManage.service;

import com.duoc.CloudManage.aws.service.S3ServiceImpl;
import com.duoc.CloudManage.aws.service.EfsService;
import com.duoc.CloudManage.dto.GuiaMapper;
import com.duoc.CloudManage.dto.GuiaRequestDTO;
import com.duoc.CloudManage.dto.GuiaResponseDTO;
import com.duoc.CloudManage.exceptions.AccesoNoPermitidoException;
import com.duoc.CloudManage.exceptions.GuiaNotFoundException;
import com.duoc.CloudManage.model.GuiaDespacho;
import com.duoc.CloudManage.model.GuiaStatus;
import com.duoc.CloudManage.repository.GuiaRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuiaService {

    private final GuiaRepository guiaRepository;
    private final PdfService     pdfService;
    private final EfsService     efsService;
    private final S3ServiceImpl      s3Service;
    
    @Autowired
    private final GuiaProducer guiaProducer;

    // Crear guía — genera PDF y lo guarda en EFS
    public GuiaResponseDTO crearGuia(GuiaRequestDTO dto) {
        GuiaDespacho guia = new GuiaDespacho();
        
        if (dto.getNumeroGuia() != null && !dto.getNumeroGuia().isBlank()) {
            guia.setNumeroGuia(dto.getNumeroGuia());
        } else {
            guia.setNumeroGuia(generarNumero());
        }
        
        guia.setTransportista(dto.getTransportista());
        guia.setFecha(LocalDate.now());
        guia.setEstado(GuiaStatus.PENDIENTE);

        // 1. Generar PDF con los datos de la guía
        byte[] pdf = pdfService.generarPdf(guia);

        // 2. Persistir PDF en EFS y guardar la ruta
        String rutaEfs = efsService.guardar(guia.getNumeroGuia(), pdf);
        guia.setRutaEfs(rutaEfs);

        // 3. Enviar mensaje a la cola de RabbitMQ
        guiaProducer.publicarGuia(guia);

        return GuiaMapper.toResponse(guiaRepository.save(guia));
    }

    // Subir guía desde EFS a S3
    public GuiaResponseDTO subirAws3(String id) throws IOException {
        GuiaDespacho guia = null;
        try {
            guia = findOrThrow(id);
        } catch (GuiaNotFoundException e) {
            try {
                File archivoEfs = efsService.obtenerArchivoPorNumero(id);
                String rutaS3 = "fallback/" + id + ".pdf";
                
                s3Service.uploadS3File(rutaS3, archivoEfs);
                
                // Retornamos un objeto transitorio ya que no hay registro en DB
                GuiaDespacho transientGuia = new GuiaDespacho();
                transientGuia.setNumeroGuia(id);
                transientGuia.setRutaS3(rutaS3);
                transientGuia.setEstado(GuiaStatus.SUBIDA);
                return GuiaMapper.toResponse(transientGuia);
            } catch (Exception ex) {
                throw e; // Si tampoco está en disco, lanzamos la excepción original
            }
        }

        // 1. Obtener la referencia al archivo en EFS (sin cargarlo en RAM)
        File archivoEfs = efsService.obtenerArchivo(guia.getRutaEfs());

        // 2. Generar ruta organizada: /YYYYMM/transportista/guia.pdf
        String fechaFormateada = guia.getFecha().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String rutaS3 = String.format("%s/%s/%s.pdf", 
                fechaFormateada, 
                guia.getTransportista(), 
                guia.getNumeroGuia());

        // 3. Subir a S3 usando el archivo físico
        s3Service.uploadS3File( 
                rutaS3,
                archivoEfs);

        guia.setRutaS3(rutaS3);
        guia.setEstado(GuiaStatus.SUBIDA);

        // 4. Enviar mensaje a la cola de RabbitMQ
        guiaProducer.publicarGuia(guia);

        return GuiaMapper.toResponse(guiaRepository.save(guia));
    }

    // Descargar PDF desde S3 con validación de permisos
    public byte[] descargar(String id, String solicitante) throws IOException {
        GuiaDespacho guia = findOrThrow(id);

        if (!guia.getTransportista().equals(solicitante))
            throw new AccesoNoPermitidoException(
                    "El transportista '" + solicitante +
                    "' no tiene permiso para descargar esta guía");

        return s3Service.downloadS3File(guia.getRutaS3());
    }

    // Actualizar guía en S3
    public GuiaResponseDTO actualizar(String id, GuiaRequestDTO dto) {
        GuiaDespacho guia = findOrThrow(id);

        // Actualizar campos modificables
        if (dto.getTransportista() != null)
            guia.setTransportista(dto.getTransportista());

        // Regenerar PDF con los datos actualizados
        byte[] nuevoPdf = pdfService.generarPdf(guia);

        try {
            // Reemplazar en S3 (mismo key)
            s3Service.uploadS3File(guia.getRutaS3(), nuevoPdf);
            guia.setEstado(GuiaStatus.ACTUALIZADA);
        } catch (IOException e) {
            throw new RuntimeException("Error al actualizar el archivo en S3", e);
        }

        // Enviar mensaje a la cola de RabbitMQ
        guiaProducer.publicarGuia(guia);

        return GuiaMapper.toResponse(guiaRepository.save(guia));
    }

    // Eliminar guía de S3
    public void eliminar(String id) {
        GuiaDespacho guia = findOrThrow(id);

        s3Service.deleteS3Object(guia.getRutaS3());
        guia.setEstado(GuiaStatus.ELIMINADA);

        // Enviar mensaje a la cola de RabbitMQ
        guiaProducer.publicarGuia(guia);

        guiaRepository.save(guia);
    }

    // Historial con filtros opcionales
    public List<GuiaResponseDTO> historial(String transportista, LocalDate fecha) {
        if (transportista != null && fecha != null)
            return GuiaMapper.toResponseList(guiaRepository.findByTransportistaAndFecha(transportista, fecha));
        if (transportista != null)
            return GuiaMapper.toResponseList(guiaRepository.findByTransportista(transportista));
        if (fecha != null)
            return GuiaMapper.toResponseList(guiaRepository.findByFecha(fecha));
        return GuiaMapper.toResponseList(guiaRepository.findAll());
    }

    // Buscar guía por número de guía
    public GuiaResponseDTO buscarPorNumero(String numeroGuia) {
        return GuiaMapper.toResponse(guiaRepository.findByNumeroGuia(numeroGuia)
                .orElseThrow(() -> new GuiaNotFoundException("Guía no encontrada: " + numeroGuia)));
    }

    // ── Utilidades ───────────────────────────────────────────────────

    private GuiaDespacho findOrThrow(String id) {
        return guiaRepository.findById(id)
                .or(() -> guiaRepository.findByNumeroGuia(id))
                .orElseThrow(() ->
                        new GuiaNotFoundException("Guía no encontrada: " + id));
    }

    private String generarNumero() {
        return "G-" + System.currentTimeMillis();
    }
}
