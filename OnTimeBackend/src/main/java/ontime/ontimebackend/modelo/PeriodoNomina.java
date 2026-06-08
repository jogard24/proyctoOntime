package ontime.ontimebackend.modelo;

public class PeriodoNomina {
    
    private int id;
    private String fechaInicio; // Usamos String para transferir la fecha sin complicaciones al JS
    private String fechaFin;
    private String estado; // Almacenará los ENUM ('abierto', 'cerrado') de tu MySQL

    // Constructor vacío obligatorio
    public PeriodoNomina() {
    }

    public PeriodoNomina(int id, String fechaInicio, String fechaFin, String estado) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    // --- Métodos Getters y Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
