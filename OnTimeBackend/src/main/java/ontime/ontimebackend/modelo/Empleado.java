package ontime.ontimebackend.modelo;

public class Empleado {

    private int id; // Se cambió a int para coincidir con la llave primaria de Ontime3BD
    private String documento;
    private String nombre;
    private String apellido;
    private String direccion;
    private String estado;
    private String tipoSangre;
    private String foto;
    private String fechaRegistro;
    private String usuarioWeb;
    private String claveWeb;

    // Datos de tablas asociadas (Para evitar redundancia de archivos en un entorno académico)
    private String email;
    private String telefonoCelular;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String contactoEmergenciaParentesco;
    
    // Campo analítico útil para el listado general de gestión
    private String cargo;

    public Empleado() {
    }

    // --- Métodos Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoSangre() { return tipoSangre; }
    public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefonoCelular() { return telefonoCelular; }
    public void setTelefonoCelular(String telefonoCelular) { this.telefonoCelular = telefonoCelular; }

    public String getContactoEmergenciaNombre() { return contactoEmergenciaNombre; }
    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) { this.contactoEmergenciaNombre = contactoEmergenciaNombre; }

    public String getContactoEmergenciaTelefono() { return contactoEmergenciaTelefono; }
    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) { this.contactoEmergenciaTelefono = contactoEmergenciaTelefono; }

    public String getContactoEmergenciaParentesco() { return contactoEmergenciaParentesco; }
    public void setContactoEmergenciaParentesco(String contactoEmergenciaParentesco) { this.contactoEmergenciaParentesco = contactoEmergenciaParentesco; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    
    public String getUsuarioWeb() { return usuarioWeb; }
    public void setUsuarioWeb(String usuarioWeb) { this.usuarioWeb = usuarioWeb; }
    
    public String getClaveWeb() { return claveWeb; }
    public void setClaveWeb(String claveWeb) { this.claveWeb = claveWeb; }
}
