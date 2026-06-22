package ontime.ontimebackend.modelo;

import java.math.BigDecimal;

public class Nomina {
    
    private int id;
    private int usuarioId;
    private int periodoId;
    private BigDecimal salarioBasePeriodo;
    private double totalHorasExtras;
    private BigDecimal totalNeto;
    private int diasAsistidos;

    // Campos de transferencia analíticos Soportan tu lógica en nomina.js
    private String documento;
    private String nombre;
    private String apellido;
    private int totalRetardos;
    private int totalExtras;

    public Nomina() {
    }

    // --- Métodos Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getPeriodoId() { return periodoId; }
    public void setPeriodoId(int periodoId) { this.periodoId = periodoId; }

    public BigDecimal getSalarioBasePeriodo() { return salarioBasePeriodo; }
    public void setSalarioBasePeriodo(BigDecimal salarioBasePeriodo) { this.salarioBasePeriodo = salarioBasePeriodo; }

    public double getTotalHorasExtras() { return totalHorasExtras; }
    public void setTotalHorasExtras(double totalHorasExtras) { this.totalHorasExtras = totalHorasExtras; }

    public BigDecimal getTotalNeto() { return totalNeto; }
    public void setTotalNeto(BigDecimal totalNeto) { this.totalNeto = totalNeto; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public int getTotalRetardos() { return totalRetardos; }
    public void setTotalRetardos(int totalRetardos) { this.totalRetardos = totalRetardos; }

    public int getTotalExtras() { return totalExtras; }
    public void setTotalExtras(int totalExtras) { this.totalExtras = totalExtras; }
    
    public int getDiasAsistidos() {return diasAsistidos;}
    public void setDiasAsistidos(int diasAsistidos) { this.diasAsistidos = diasAsistidos;
}
}

