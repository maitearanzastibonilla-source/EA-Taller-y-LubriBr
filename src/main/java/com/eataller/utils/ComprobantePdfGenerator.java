package com.eataller.utils;

import com.eataller.entity.Cliente;
import com.eataller.entity.Comprobante;
import com.eataller.entity.ItemTrabajo;
import com.eataller.entity.TrabajoRealizado;
import com.eataller.entity.Vehiculo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsabilidad: generar el PDF del comprobante de servicio, consolidando
 * los datos del trabajo, el vehiculo, el cliente y los items cargados.
 * Autor: EA Taller y LubriBr - Equipo de Desarrollo
 */
public final class ComprobantePdfGenerator {

    private static final PDFont FUENTE_TITULO = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FUENTE_TEXTO = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FUENTE_TEXTO_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final float MARGEN_IZQUIERDO = 50;
    private static final float ANCHO_UTIL = PDRectangle.A4.getWidth() - (2 * MARGEN_IZQUIERDO);

    private ComprobantePdfGenerator() {
    }

    public static void generar(Path destino, Comprobante comprobante, TrabajoRealizado trabajo,
                                Vehiculo vehiculo, Cliente cliente, List<ItemTrabajo> items) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = PDRectangle.A4.getHeight() - 60;

                y = escribirLinea(content, FUENTE_TITULO, 18, y, "EA Taller y LubriBr");
                y = escribirLinea(content, FUENTE_TEXTO, 10, y, "Mecanica y lubricentro");
                y -= 15;
                y = escribirLinea(content, FUENTE_TITULO, 13, y,
                        "Comprobante de Servicio N. " + comprobante.getIdComprobante());
                y = escribirLinea(content, FUENTE_TEXTO, 10, y,
                        "Fecha de emision: " + comprobante.getFecha().format(FORMATO_FECHA));
                y -= 15;

                y = escribirLinea(content, FUENTE_TEXTO_BOLD, 11, y, "Cliente");
                y = escribirLinea(content, FUENTE_TEXTO, 10, y, cliente.getNombreCompleto()
                        + " - DNI " + cliente.getDni() + " - Tel. " + cliente.getTelefono());
                y -= 10;

                y = escribirLinea(content, FUENTE_TEXTO_BOLD, 11, y, "Vehiculo");
                y = escribirLinea(content, FUENTE_TEXTO, 10, y, vehiculo.getPatente()
                        + " - " + vehiculo.getMarca() + " " + vehiculo.getModelo() + " (" + vehiculo.getAnio() + ")");
                y -= 10;

                y = escribirLinea(content, FUENTE_TEXTO_BOLD, 11, y, "Descripcion del trabajo");
                for (String linea : partirEnLineas(trabajo.getDescripcion(), FUENTE_TEXTO, 10, ANCHO_UTIL)) {
                    y = escribirLinea(content, FUENTE_TEXTO, 10, y, linea);
                }
                y -= 20;

                float colDescripcion = MARGEN_IZQUIERDO;
                float colCantidad = MARGEN_IZQUIERDO + 260;
                float colPrecio = MARGEN_IZQUIERDO + 340;
                float colSubtotal = MARGEN_IZQUIERDO + 440;

                content.setFont(FUENTE_TEXTO_BOLD, 10);
                content.beginText();
                content.newLineAtOffset(colDescripcion, y);
                content.showText("Descripcion");
                content.endText();
                content.beginText();
                content.newLineAtOffset(colCantidad, y);
                content.showText("Cant.");
                content.endText();
                content.beginText();
                content.newLineAtOffset(colPrecio, y);
                content.showText("P. Unit.");
                content.endText();
                content.beginText();
                content.newLineAtOffset(colSubtotal, y);
                content.showText("Subtotal");
                content.endText();
                y -= 6;
                content.moveTo(MARGEN_IZQUIERDO, y);
                content.lineTo(MARGEN_IZQUIERDO + ANCHO_UTIL, y);
                content.stroke();
                y -= 15;

                content.setFont(FUENTE_TEXTO, 10);
                for (ItemTrabajo item : items) {
                    content.beginText();
                    content.newLineAtOffset(colDescripcion, y);
                    content.showText(recortar(item.getDescripcionLibre(), 42));
                    content.endText();
                    content.beginText();
                    content.newLineAtOffset(colCantidad, y);
                    content.showText(item.getCantidad().toPlainString());
                    content.endText();
                    content.beginText();
                    content.newLineAtOffset(colPrecio, y);
                    content.showText("$ " + item.getPrecioUnitario().toPlainString());
                    content.endText();
                    content.beginText();
                    content.newLineAtOffset(colSubtotal, y);
                    content.showText("$ " + item.getSubtotal().toPlainString());
                    content.endText();
                    y -= 16;
                }

                y -= 10;
                content.moveTo(MARGEN_IZQUIERDO, y);
                content.lineTo(MARGEN_IZQUIERDO + ANCHO_UTIL, y);
                content.stroke();
                y -= 20;

                y = escribirLinea(content, FUENTE_TEXTO_BOLD, 12, y, "Total: $ " + comprobante.getTotal().toPlainString());
                y = escribirLinea(content, FUENTE_TEXTO, 10, y, "Metodo de pago: " + comprobante.getMetodoPago().getEtiqueta());
                y = escribirLinea(content, FUENTE_TEXTO, 10, y, "Estado: " + comprobante.getEstado().getEtiqueta());

                y -= 30;
                content.setFont(FUENTE_TEXTO, 8);
                content.beginText();
                content.newLineAtOffset(MARGEN_IZQUIERDO, y);
                content.showText("Documento generado automaticamente por el Sistema de Gestion Integral de EA Taller y LubriBr.");
                content.endText();
            }

            document.save(destino.toFile());
        }
    }

    private static float escribirLinea(PDPageContentStream content, PDFont fuente, float tamanio, float y, String texto)
            throws IOException {
        content.setFont(fuente, tamanio);
        content.beginText();
        content.newLineAtOffset(MARGEN_IZQUIERDO, y);
        content.showText(texto);
        content.endText();
        return y - (tamanio + 6);
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + ".";
    }

    private static List<String> partirEnLineas(String texto, PDFont fuente, float tamanio, float anchoMaximo)
            throws IOException {
        List<String> lineas = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return lineas;
        }
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split("\\s+")) {
            String candidato = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (fuente.getStringWidth(candidato) / 1000 * tamanio > anchoMaximo && !actual.isEmpty()) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(candidato);
            }
        }
        if (!actual.isEmpty()) {
            lineas.add(actual.toString());
        }
        return lineas;
    }
}
