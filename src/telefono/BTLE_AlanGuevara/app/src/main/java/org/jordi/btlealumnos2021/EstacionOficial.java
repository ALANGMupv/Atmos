package org.jordi.btlealumnos2021;

/**
 * @class EstacionOficial
 * @author Alan Guevara Martínez
 * @brief Representa una estación oficial de medida de calidad del aire.
 * @date 16/12/2025
 */
public class EstacionOficial {

    public String nombre;
    public double lat;
    public double lon;

    // Valores por gas (pueden no existir)
    public Double no2;
    public Double o3;
    public Double co;
    public Double so2;

    public String fecha;

    // Vamos a mostrar también las unidades de los gases
    public String unidadNO2;
    public String unidadO3;
    public String unidadCO;
    public String unidadSO2;
}
