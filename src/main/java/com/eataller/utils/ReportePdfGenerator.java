package com.eataller.utils;

import com.eataller.dto.ReporteDatos;
import com.eataller.entity.TipoReporte;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ReportePdfGenerator {

    private static final PDFont FUENTE_TITULO = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FUENTE_TEXTO = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FUENTE_TEXTO_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final float MARGEN_IZQUIERDO = 45;
    private static final float MARGEN_SUPERIOR = 60;
    private static final float MARGEN_INFERIOR = 60;
    private static final float ANCHO_UTIL = PDRectangle.A4.getWidth() - (2 * MARGEN_IZQUIERDO);

    private ReportePdfGenerator() {
    }

    public static void generar(Path destino, TipoReporte tipo, LocalDate fechaDesde, LocalDate fechaHasta,
                                LocalDateTime fechaGeneracion, String usuarioGenerador, ReporteDatos datos)
            throws IOException {

        try (PDDocument document = new PDDocument()) {
            EstadoPagina estado = new EstadoPagina(document);

            estado.escribir(FUENTE_TITULO, 16, "EA Taller y LubriBr");
            estado.escribir(FUENTE_TEXTO, 10, "Reporte generado automaticamente por el Sistema de Gestion Integral");
            estado.espaciar(10);
            estado.escribir(FUENTE_TITULO, 13, tipo.getEtiqueta());
            estado.escribir(FUENTE_TEXTO, 10, "Periodo: " + fechaDesde.format(FORMATO_FECHA) + " al " + fechaHasta.format(FORMATO_FECHA));
            estado.escribir(FUENTE_TEXTO, 10, "Generado el " + fechaGeneracion.format(FORMATO_FECHA_HORA) + " por " + usuarioGenerador);
            estado.espaciar(15);

            List<String> columnas = datos.getColumnas();
            float anchoColumna = ANCHO_UTIL / columnas.size();

            if (datos.getFilas().isEmpty()) {
                estado.escribir(FUENTE_TEXTO, 10, "No se encontraron datos para el periodo seleccionado.");
            } else {
                estado.escribirEncabezadoTabla(columnas, anchoColumna);
                for (List<String> fila : datos.getFilas()) {
                    estado.escribirFilaTabla(fila, anchoColumna, columnas.size());
                }
            }

            if (!datos.getResumen().isEmpty()) {
                estado.espaciar(15);
                for (String linea : datos.getResumen()) {
                    estado.escribir(FUENTE_TEXTO_BOLD, 11, linea);
                }
            }

            estado.cerrar();
            document.save(destino.toFile());
        }
    }

    private static class EstadoPagina {
        private final PDDocument document;
        private PDPage pagina;
        private PDPageContentStream content;
        private float y;

        EstadoPagina(PDDocument document) throws IOException {
            this.document = document;
            nuevaPagina();
        }

        private void nuevaPagina() throws IOException {
            pagina = new PDPage(PDRectangle.A4);
            document.addPage(pagina);
            content = new PDPageContentStream(document, pagina);
            y = PDRectangle.A4.getHeight() - MARGEN_SUPERIOR;
        }

        private void asegurarEspacio(float alturaNecesaria) throws IOException {
            if (y - alturaNecesaria < MARGEN_INFERIOR) {
                content.close();
                nuevaPagina();
            }
        }

        void escribir(PDFont fuente, float tamanio, String texto) throws IOException {
            asegurarEspacio(tamanio + 6);
            content.setFont(fuente, tamanio);
            content.beginText();
            content.newLineAtOffset(MARGEN_IZQUIERDO, y);
            content.showText(texto);
            content.endText();
            y -= (tamanio + 6);
        }

        void espaciar(float puntos) {
            y -= puntos;
        }

        void escribirEncabezadoTabla(List<String> columnas, float anchoColumna) throws IOException {
            asegurarEspacio(20);
            content.setFont(FUENTE_TEXTO_BOLD, 9);
            for (int i = 0; i < columnas.size(); i++) {
                content.beginText();
                content.newLineAtOffset(MARGEN_IZQUIERDO + (i * anchoColumna), y);
                content.showText(recortar(columnas.get(i), 32));
                content.endText();
            }
            y -= 6;
            content.moveTo(MARGEN_IZQUIERDO, y);
            content.lineTo(MARGEN_IZQUIERDO + ANCHO_UTIL, y);
            content.stroke();
            y -= 14;
        }

        void escribirFilaTabla(List<String> fila, float anchoColumna, int cantidadColumnas) throws IOException {
            asegurarEspacio(16);
            content.setFont(FUENTE_TEXTO, 9);
            for (int i = 0; i < cantidadColumnas; i++) {
                String valor = i < fila.size() ? fila.get(i) : "";
                content.beginText();
                content.newLineAtOffset(MARGEN_IZQUIERDO + (i * anchoColumna), y);
                content.showText(recortar(valor, 32));
                content.endText();
            }
            y -= 15;
        }

        void cerrar() throws IOException {
            content.close();
        }

        private String recortar(String texto, int maximo) {
            if (texto == null) {
                return "";
            }
            return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + ".";
        }
    }
}
