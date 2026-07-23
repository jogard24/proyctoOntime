package ontime.ontimebackend.modelo;

import java.math.BigDecimal;

public class Contrato {
    
    private int id;
    private int usuarioId;
    private String tipoContrato;
    private String cargo;
    private BigDecimal salarioBase;
    private int jornadaId;
    private String fechaInicio;
    private String fechaFin;
    private String empleadoNombre; // Guarda el Nombre + Apellido devuelto por el JOIN
    private String periodoPago;    // Guarda el texto 'Mensual' o 'Quincenal' de MySQL


    // Constructor vacío obligatorio para instanciar en los Servlets/DAOs
    public Contrato() {
    }

    // Constructor con parámetros para desarrollo rápido
    public Contrato(int usuarioId, String tipoContrato, String cargo, BigDecimal salarioBase, int jornadaId) {
        this.usuarioId = usuarioId;
        this.tipoContrato = tipoContrato;
        this.cargo = cargo;
        this.salarioBase = salarioBase;
        this.jornadaId = jornadaId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // --- Métodos Getters y Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public void setSalarioBase(BigDecimal salarioBase) {
        this.salarioBase = salarioBase;
    }

    public int getJornadaId() {
        return jornadaId;
    }

    public void setJornadaId(int jornadaId) {
        this.jornadaId = jornadaId;
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
    
    public String getEmpleadoNombre() {
    return empleadoNombre;
    }

    public void setEmpleadoNombre(String empleadoNombre) {
        this.empleadoNombre = empleadoNombre;
    }

    public String getPeriodoPago() {
        return periodoPago;
    }

    public void setPeriodoPago(String periodoPago) {
        this.periodoPago = periodoPago;
    }
}

