package ontime.ontimebackend.modelo;

public class Usuario {
    private int id;
    private String nombre;
    private String Rol;

    // Constructor vacío: permite instanciar y luego usar los setters
    public Usuario() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getRol() { return Rol; }
    public void setRol(String Rol) { this.Rol = Rol; }
}