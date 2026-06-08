package ontime.ontimebackend.modelo;

public class Asistencia {
    
    private int id;
    private int usuarioId; // Mapea la llave foránea física de la tabla asistencia
    private String documentoIdentidad; // Útil para pintar la cédula en los reportes (RF15)
    private String nombreEmpleado;
    private String fechaHora;
    private String tipoEvento;
    private String observacion;
    private int jornadaId;
    private String nombreJornada; // Mapea el campo 'nombrejornada' de tu tabla jornadaLaboral

    // Constructor vacío obligatorio
    public Asistencia() {
    }

    // --- GETTERS Y SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; } 

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public int getJornadaId() { return jornadaId; }
    public void setJornadaId(int jornadaId) { this.jornadaId = jornadaId; }

    public String getNombreJornada() { return nombreJornada; }
    public void setNombreJornada(String nombreJornada) { this.nombreJornada = nombreJornada; }
}
