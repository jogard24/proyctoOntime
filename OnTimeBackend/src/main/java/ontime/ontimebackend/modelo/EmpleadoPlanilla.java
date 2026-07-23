package ontime.ontimebackend.modelo;

/**
 * Modelo de Datos (DTO) - Resumen Matricial de Tiempos
 * Encapsula la información del empleado y sus 31 marcas diarias de asistencia.
 */
public class EmpleadoPlanilla {
    private int id;
    private String nombre;
    private String apellido;
    private String cargo;
    private int[] diasArray = new int[31]; // ◄ 31 casilleros en la RAM para los 31 días del mes

    // Métodos públicos Getters y Setters para el encapsulamiento seguro
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public int[] getDiasArray() { return diasArray; }
    
    // Método especializado para inyectar el valor (1 o 0) en el día exacto del calendario
    public void setDiaValor(int numeroDia, int valor) {
        if (numeroDia >= 1 && numeroDia <= 31) {
            this.diasArray[numeroDia - 1] = valor; // Resta 1 porque los arreglos en Java arrancan en la posición 0
        }
    }
}

