package ontime.ontimebackend.modelo;

public class Nomina {
    private int id;
    private String cedula;
    private String nombre;
    private int diasTrabajados;
    private int totalExtras;
    private int totalRetardos;

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getDiasTrabajados() { return diasTrabajados; }
    public void setDiasTrabajados(int dias) { this.diasTrabajados = dias; }
    public int getTotalExtras() { return totalExtras; }
    public void setTotalExtras(int extras) { this.totalExtras = extras; }
    public int getTotalRetardos() { return totalRetardos; }
    public void setTotalRetardos(int retardos) { this.totalRetardos = retardos; }
}
