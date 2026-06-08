package ontime.ontimebackend.modelo;

public class PermisoLaboral {
    
    private int id;
    private int usuarioId;
    private String tipoPermiso;
    private String fechaAsignacion;
    private String fechaInicio; // Usamos String para pasar las fechas limpias al Frontend
    private String fechaFin;
    private String estado;
    
    // Campo analítico para pintar el nombre en la tabla de reportes sin dar vueltas
    private String nombreEmpleado;

    public PermisoLaboral() {
    }

    // --- Métodos Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }

    public String getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(String fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
}
