package ontime.ontimebackend.modelo;

public class Asistencia {
    // Atributos privados: Encapsulamiento para proteger los datos
    private int id;
    private String nombreEmpleado;
    private String fechaHora;
    private String tipoEvento;
    private String observacion;
    private String tipoTurno;

    // --- GETTERS Y SETTERS ---
    // Son los métodos de acceso: permiten que otras clases (como el DAO o el Servlet)
    // consulten o modifiquen los valores de forma controlada.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; } 

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getTipoTurno() { return tipoTurno; }
    public void setTipoTurno(String tipoTurno) { this.tipoTurno = tipoTurno; }
}