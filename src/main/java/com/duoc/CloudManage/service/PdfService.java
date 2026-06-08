package com.duoc.CloudManage.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import java.awt.Color;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import com.duoc.CloudManage.exceptions.PdfException;
import com.duoc.CloudManage.model.GuiaDespacho;

@Service
public class PdfService {

    public byte[] generarPdf(GuiaDespacho guia) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document documento = new Document(PageSize.A4);
            PdfWriter.getInstance(documento, baos);
            documento.open();

            agregarEncabezado(documento, guia);
            agregarDatosGuia(documento, guia);
            agregarTablaItems(documento);
            agregarPie(documento);

            documento.close();
            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            throw new PdfException("Error al generar PDF: " + e.getMessage());
        }
    }

    // ── Secciones del PDF ────────────────────────────────────────────

    private void agregarEncabezado(Document doc, GuiaDespacho guia)
            throws DocumentException {
        Font fuenteTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font fuenteSub    = new Font(Font.HELVETICA, 11, Font.NORMAL);

        Paragraph titulo = new Paragraph("Guía de Despacho", fuenteTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph numero = new Paragraph("N° " + guia.getNumeroGuia(), fuenteSub);
        numero.setAlignment(Element.ALIGN_CENTER);
        numero.setSpacingAfter(20f);
        doc.add(numero);

        doc.add(new LineSeparator());
    }

    private void agregarDatosGuia(Document doc, GuiaDespacho guia)
            throws DocumentException {
        Font etiqueta = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font valor    = new Font(Font.HELVETICA, 11, Font.NORMAL);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1, 2});
        tabla.setSpacingBefore(12f);
        tabla.setSpacingAfter(16f);

        agregarFila(tabla, "Transportista:", guia.getTransportista(), etiqueta, valor);
        agregarFila(tabla, "Fecha:",  guia.getFecha().toString(),  etiqueta, valor);
        agregarFila(tabla, "Estado:", guia.getEstado().name(),     etiqueta, valor);

        doc.add(tabla);
    }

    private void agregarTablaItems(Document doc) throws DocumentException {
        Font encabezado = new Font(Font.HELVETICA, 11, Font.BOLD,
                                   Color.WHITE);

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1, 3, 1, 1});
        tabla.setSpacingAfter(20f);

        // Cabecera con fondo oscuro
        String[] columnas = {"#", "Descripción", "Cantidad", "Peso (kg)"};
        for (String col : columnas) {
            PdfPCell celda = new PdfPCell(new Phrase(col, encabezado));
            celda.setBackgroundColor(new Color(55, 71, 79));
            celda.setPadding(6);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celda);
        }

        // Fila de ejemplo — en producción vendrían del DTO
        Font cuerpo = new Font(Font.HELVETICA, 10);
        String[][] items = {
            {"1", "Paquete estándar", "5", "12.5"},
            {"2", "Carga frágil",     "2", "8.0"}
        };
        for (String[] item : items) {
            for (String dato : item) {
                PdfPCell celda = new PdfPCell(new Phrase(dato, cuerpo));
                celda.setPadding(5);
                tabla.addCell(celda);
            }
        }

        doc.add(tabla);
    }

    private void agregarPie(Document doc) throws DocumentException {
        Font pieFuente = new Font(Font.HELVETICA, 9, Font.ITALIC,
                                  Color.GRAY);
        Paragraph pie = new Paragraph(
            "Documento generado automáticamente — " + LocalDate.now(), pieFuente);
        pie.setAlignment(Element.ALIGN_CENTER);
        doc.add(pie);
    }

    // ── Utilidad ─────────────────────────────────────────────────────

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor,
                              Font fEtiqueta, Font fValor) {
        PdfPCell celdaEt = new PdfPCell(new Phrase(etiqueta, fEtiqueta));
        PdfPCell celdaVl = new PdfPCell(new Phrase(valor,    fValor));
        celdaEt.setBorder(Rectangle.NO_BORDER);
        celdaVl.setBorder(Rectangle.NO_BORDER);
        celdaEt.setPaddingBottom(4);
        celdaVl.setPaddingBottom(4);
        tabla.addCell(celdaEt);
        tabla.addCell(celdaVl);
    }
}
