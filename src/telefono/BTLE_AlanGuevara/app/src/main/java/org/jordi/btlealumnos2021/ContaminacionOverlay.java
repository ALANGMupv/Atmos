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
 * @date 10/12/2025 - Modificado: 16/12/2025 (para que se pinte bien el peor nivel cuando todos los contaminantes estén seleccionados).
 */
public class ContaminacionOverlay extends Overlay {

    /**
     * @class PuntoContaminacion
     * @brief Representa un punto de muestreo: latitud, longitud y nivel.
     * @details Cada punto influirá en la interpolación en función de su distancia
     * respecto al píxel consultado.
     */
    public static class PuntoContaminacion {
        public double lat;
        public double lon;
        public double nivel;

        public PuntoContaminacion(double lat, double lon, double nivel) {
            this.lat = lat;
            this.lon = lon;
            this.nivel = nivel;
        }
    }

    /**
     * Lista interna de puntos usados para interpolar.
     */
    private final List<PuntoContaminacion> puntos = new ArrayList<>();

    // ===============================================================
    // === BLOQUE PARA CÁLCULO DEL ÍNDICE DE CALIDAD  ================
    // ===============================================================

    /**
     * @brief Contadores internos para calcular el porcentaje de cada categoría.
     */
    private int contBuena = 0;
    private int contModerada = 0;
    private int contInsalubre = 0;
    private int contMala = 0;
    private int totalCeldas = 0;

    /**
     * @brief Listener que envía al Activity los porcentajes calculados.
     */
    public interface OnIndiceUpdateListener {
        /**
         * @param pctBuena     Porcentaje de celdas clasificadas como "Buena".
         * @param pctModerada  Porcentaje de celdas clasificadas como "Moderada".
         * @param pctInsalubre Porcentaje de celdas clasificadas como "Insalubre".
         * @param pctMala      Porcentaje de celdas clasificadas como "Mala".
         * @param dominante    Categoría predominante.
         */
        void onIndiceUpdated(int pctBuena, int pctModerada, int pctInsalubre, int pctMala, String dominante);
    }

    private OnIndiceUpdateListener indiceListener;

    /**
     * @param l Implementación del listener.
     * @brief Asigna un listener que recibirá el índice actualizado.
     */
    public void setOnIndiceUpdateListener(OnIndiceUpdateListener l) {
        this.indiceListener = l;
    }

    /**
     * @param nuevos Lista de objetos PuntoContaminacion.
     * @brief Sobrescribe la lista de puntos existentes por una nueva lista de entrada.
     * @details Esta operación invalida todos los puntos actuales y los sustituye por los nuevos.
     * Debe llamarse antes de repintar el mapa.
     */
    public void setPuntos(List<PuntoContaminacion> nuevos) {
        puntos.clear();
        puntos.addAll(nuevos);
    }

    /**
     * @param r Componente rojo.
     * @param g Componente verde.
     * @param b Componente azul.
     * @return Texto con la categoría: "buena", "moderada", "insalubre" o "mala".
     * @brief Clasifica un color RGB como nivel de calidad del aire.
     * @details Usa la misma lógica que la versión Web.
     */
    private String clasificar(int r, int g, int b) {

        // Verde → Buena
        if (g > 200 && r < 80) return "buena";

        // Amarillo → Moderada
        if (r > 200 && g > 200) return "moderada";

        // Naranja → Insalubre
        if (r > 200 && g < 200 && g > 80) return "insalubre";

        // Rojo → Mala
        return "mala";
    }


    // ===============================================================
    // ===  FIN BLOQUE PARA CÁLCULO DEL ÍNDICE DE CALIDAD  ===========
    // ===============================================================

    /**
     * @brief Método principal encargado de dibujar el mapa interpolado.
     * @details Este método divide el canvas en una cuadrícula de GRID x GRID celdas.
     * Para cada centro de celda:
     * 1. Se proyecta el punto del píxel a coordenadas geográficas.
     * 2. Se calcula una interpolación IDW usando los puntos de contaminación.
     * 3. Se dibuja un círculo coloreado con la mezcla obtenida.
     * <p>
     * El parámetro `shadow` es estándar en osmdroid, pero aquí no se usa.
     */
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {

        // Si se trata de una llamada al shadow layer o no hay puntos, no dibujar nada.
        if (shadow || puntos.isEmpty()) return;


        // Reiniciar contadores índices
        contBuena = 0;
        contModerada = 0;
        contInsalubre = 0;
        contMala = 0;
        totalCeldas = 0;


        // Número de divisiones por eje del lienzo.
        final int GRID = 50;

        // Objeto Paint reutilizado para dibujar los círculos interpolados.
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);       // suaviza bordes
        paint.setDither(true);          // mejora mezcla de colores en gradientes
        paint.setFilterBitmap(true);    // suaviza al escalar

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Tamaño de cada celda (píxeles).
        float stepX = width / (float) GRID;
        float stepY = height / (float) GRID;

        // Radio de influencia geográfico para IDW.
        final double RADIO = 0.012;  // ≈1–1.2 km dependiendo latitud
        final double EXP = 2;        // exponente IDW, 2 = estándar
        final float ALPHA = 0.12f; // opacidad del heatmap

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
                double maxNivel = 0; // Nos quedaremos con el PEOR nivel para cuando se seleccionen todos los contaminantes

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

                    /*
                     * En lugar de interpolar colores,
                     * nos quedamos con el nivel MÁS ALTO
                     * (el contaminante más peligroso)
                     */
                    maxNivel = Math.max(maxNivel, p.nivel);
                }

                // Enviar resultados índices a la Activity
                if (indiceListener != null && totalCeldas > 0) {

                    int pctBuena = (int) ((contBuena * 100f) / totalCeldas);
                    int pctModerada = (int) ((contModerada * 100f) / totalCeldas);
                    int pctInsalubre = (int) ((contInsalubre * 100f) / totalCeldas);
                    int pctMala = (int) ((contMala * 100f) / totalCeldas);

                    // Determinar categoría predominante
                    String dominante = "buena";
                    int max = pctBuena;

                    if (pctModerada > max) {
                        dominante = "moderada";
                        max = pctModerada;
                    }
                    if (pctInsalubre > max) {
                        dominante = "insalubre";
                        max = pctInsalubre;
                    }
                    if (pctMala > max) {
                        dominante = "mala";
                    }

                    indiceListener.onIndiceUpdated(
                            pctBuena,
                            pctModerada,
                            pctInsalubre,
                            pctMala,
                            dominante
                    );
                }

                // Si ningún punto aporta peso, este píxel se descarta
                if (sumW == 0) continue;

                // Convertimos el PEOR nivel a color
                int[] rgb = colorPorNivel(maxNivel);
                int rr = rgb[0];
                int gg = rgb[1];
                int bb = rgb[2];

                // Índices calidad del aire, contabilizar celdas y clasificar
                String nivel = clasificar(rr, gg, bb);
                totalCeldas++;

                switch (nivel) {
                    case "buena":
                        contBuena++;
                        break;
                    case "moderada":
                        contModerada++;
                        break;
                    case "insalubre":
                        contInsalubre++;
                        break;
                    default:
                        contMala++;
                        break;
                }


                // Ajustar color con alpha para permitir ver el mapa debajo
                paint.setColor(Color.argb((int) (ALPHA * 255), rr, gg, bb));

                // Se dibuja un círculo ligeramente menor que la celda
                canvas.drawRect(
                        cx - stepX,
                        cy - stepY,
                        cx + stepX,
                        cy + stepY,
                        paint
                );
            }
        }
    }

    /**
     * @brief Convierte un nivel normalizado de contaminación en un color RGB.
     *
     * @details
     * Este método traduce un valor normalizado (entre 0.1 y 1.0) a un color
     * representativo de la calidad del aire, siguiendo la misma escala visual
     * utilizada en el índice de calidad:
     *
     *  - 0.10 → Verde (calidad buena)
     *  - 0.45 → Amarillo (calidad moderada)
     *  - 0.75 → Naranja (calidad insalubre)
     *  - 1.00 → Rojo (calidad mala)
     *
     * El color devuelto se utiliza tanto para:
     *  - Pintar el mapa de contaminación.
     *  - Clasificar las celdas visibles para calcular el índice global.
     *
     * @param n Nivel normalizado de contaminación.
     * @return Array de enteros {R, G, B} con valores entre 0 y 255.
     * @date 16/12/2025
     */
    private int[] colorPorNivel(double n) {

        // Calidad buena → verde
        if (n <= 0.10) {
            return new int[]{36, 255, 84};
        }

        // Calidad moderada → amarillo
        if (n <= 0.45) {
            return new int[]{255, 240, 0};
        }

        // Calidad insalubre → naranja
        if (n <= 0.75) {
            return new int[]{255, 144, 0};
        }

        // Calidad mala → rojo
        return new int[]{255, 48, 48};
    }

}
