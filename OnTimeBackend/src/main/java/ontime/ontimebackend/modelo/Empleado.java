package ontime.ontimebackend.modelo;

public class Empleado {

    private String id;
    private String nombre;
    private String documento;

    private String cargo;
    private String estado;
    private String fotoPerfil_url;
    private String telefono_celular;

    private String telefonoCelular;
    private String email;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;

    // Constructor vacío obligatorio
    public Empleado() {
    }

    // Constructor con parámetros
    public Empleado(String id, String nombre, String cargo, String estado, String fotoPerfil_url) {
        this.id = id;
        this.nombre = nombre;
        this.cargo = cargo;
        this.estado = estado;
        this.fotoPerfil_url = fotoPerfil_url;
    }

    // Métodos Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFoto() {
        return fotoPerfil_url;
    }

    public void setFoto(String fotoPerfil_url) {
        this.fotoPerfil_url = fotoPerfil_url;
    }

    public String getTelefonoCelular() {
        return telefonoCelular;
    }

    public void setTelefonoCelular(String telefonoCelular) {
        this.telefonoCelular = telefonoCelular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactoEmergenciaNombre() {
        return contactoEmergenciaNombre;
    }

    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) {
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
    }

    public String getContactoEmergenciaTelefono() {
        return contactoEmergenciaTelefono;
    }

    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) {
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }
}
