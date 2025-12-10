package org.jordi.btlealumnos2021;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Color;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;

/**
 * @class ContaminacionOverlay
 * @brief Capa personalizada para pintar interpolación espacial IDW de contaminación.
 *
 * @details
 * Esta clase implementa un overlay que dibuja un mapa de calor basado en valores RGB que
 * llegan ya normalizados. Es funcionalmente similar al CanvasOverlay de *Leaflet*.
 *
 * El método draw() divide el lienzo en una cuadrícula y, para cada celda, calcula
 * un color interpolado mediante IDW usando los puntos de contaminación.
 *
 * El resultado es un suavizado espacial donde zonas cercanas a puntos con valores altos
 * son coloreadas con mayor intensidad.
 *
 * Se trata de una interpolación local: solo influencian puntos a una distancia menor
 * que RADIO. Esto optimiza el cálculo al evitar procesar puntos que no aportarán peso.
 *
 * Parámetros importantes:
 *  - GRID:   resolución de la cuadrícula (celdas por eje).
 *  - RADIO:  radio geográfico máximo de influencia de cada punto.
 *  - EXP:    exponente IDW (cuanto mayor, más penaliza distancia).
 *  - ALPHA:  opacidad del círculo pintado (mezcla con el mapa).
 *
 * @author Alan Guevara Martinez
 * @date 10/12/2025
 */
public class ContaminacionOverlay extends Overlay {

    /**
     * @class PuntoContaminacion
     * @brief Representa un punto de muestreo: latitud, longitud y color en RGB.
     *
     * @details
     * Los valores RGB deben venir ya normalizados (ej. 0–255).
     * Cada punto influirá en la interpolación en función de su distancia
     * respecto al píxel consultado.
     */
    public static class PuntoContaminacion {
        public double lat;
        public double lon;
        public int r, g, b;

        public PuntoContaminacion(double lat, double lon, int r, int g, int b) {
            this.lat = lat;
            this.lon = lon;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    /** Lista interna de puntos usados para interpolar. */
    private final List<PuntoContaminacion> puntos = new ArrayList<>();

    /**
     * @brief Sobrescribe la lista de puntos existentes por una nueva lista de entrada.
     *
     * @details
     * Esta operación invalida todos los puntos actuales y los sustituye por los nuevos.
     * Debe llamarse antes de repintar el mapa.
     *
     * @param nuevos Lista de objetos PuntoContaminacion.
     */
    public void setPuntos(List<PuntoContaminacion> nuevos) {
        puntos.clear();
        puntos.addAll(nuevos);
    }

    /**
     * @brief Método principal encargado de dibujar el mapa interpolado.
     *
     * @details
     * Este método divide el canvas en una cuadrícula de GRID x GRID celdas.
     * Para cada centro de celda:
     *  1. Se proyecta el punto del píxel a coordenadas geográficas.
     *  2. Se calcula una interpolación IDW usando los puntos de contaminación.
     *  3. Se dibuja un círculo coloreado con la mezcla obtenida.
     *
     * El parámetro `shadow` es estándar en osmdroid, pero aquí no se usa.
     */
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {

        // Si se trata de una llamada al shadow layer o no hay puntos, no dibujar nada.
        if (shadow || puntos.isEmpty()) return;

        // Número de divisiones por eje del lienzo.
        final int GRID = 60;

        // Objeto Paint reutilizado para dibujar los círculos interpolados.
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Tamaño de cada celda (píxeles).
        float stepX = width / (float) GRID;
        float stepY = height / (float) GRID;

        // Radio de influencia geográfico para IDW.
        final double RADIO = 0.012;  // ≈1–1.2 km dependiendo latitud
        final double EXP = 2;        // exponente IDW, 2 = estándar
        final float ALPHA = 0.18f;   // opacidad del heatmap

        // Reutilizado para obtener proyecciones de píxeles
        Point pixel = new Point();

        // Recorrer toda la cuadrícula
        for (int ix = 0; ix < GRID; ix++) {
            for (int iy = 0; iy < GRID; iy++) {

                // Centro del píxel del grid
                float cx = ix * stepX + stepX / 2f;
                float cy = iy * stepY + stepY / 2f;

                // Convertimos el centro del píxel a coordenadas geo
                GeoPoint gp = (GeoPoint) mapView.getProjection().fromPixels((int) cx, (int) cy);

                double lat = gp.getLatitude();
                double lon = gp.getLongitude();

                // Acumuladores IDW
                double sumW = 0;      // suma de pesos
                double sumR = 0, sumG = 0, sumB = 0;

                // Evaluar influencia de cada punto sobre la celda actual
                for (PuntoContaminacion p : puntos) {

                    // Distancia geográfica aproximada
                    double dx = lon - p.lon;
                    double dy = lat - p.lat;
                    double d = Math.sqrt(dx * dx + dy * dy);

                    // Si está fuera del radio, no aporta nada
                    if (d > RADIO) continue;

                    // Peso IDW (si d=0 se fuerza peso máximo)
                    double w = d == 0 ? 1 : 1 / Math.pow(d, EXP);

                    sumW += w;
                    sumR += w * p.r;
                    sumG += w * p.g;
                    sumB += w * p.b;
                }

                // Si ningún punto aporta peso, este píxel se descarta
                if (sumW == 0) continue;

                // Color resultante por interpolación ponderada
                int rr = (int) (sumR / sumW);
                int gg = (int) (sumG / sumW);
                int bb = (int) (sumB / sumW);

                // Ajustar color con alpha para permitir ver el mapa debajo
                paint.setColor(Color.argb((int) (ALPHA * 255), rr, gg, bb));

                // Se dibuja un círculo ligeramente menor que la celda
                canvas.drawCircle(cx, cy, stepX * 0.9f, paint);
            }
        }
    }
}
