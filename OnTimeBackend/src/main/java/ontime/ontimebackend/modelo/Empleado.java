package ontime.ontimebackend.modelo;

//contiene al Usuario y representar 
//la información personal y laboral (nombre completo, documento, dirección, cargo, contrato).
public class Empleado {

    private String id;
    private String nombre;
    private String apellido;
    private String documento;
    private String direccion;

    private String cargo;
    private String estado;
    private String fotoPerfil_url;
    private String telefono_celular;
    private String tipoSangre;

    private String telefonoCelular;
    private String Email;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String contactoEmergenciaParentesco;

    // Constructor vacío: permite instanciar y luego usar los setters
    public Empleado() {
    }

    // Constructor con parámetros
    public Empleado(String id, String nombre, String apellido,String documento,
            String direccion, String cargo, String estado, String fotoPerfil_url,
            String contactoEmergenciaNombre, String contactoEmergenciaParentesco, String contactoEmergenciaTelefono ) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.direccion = direccion;
        this.cargo = cargo;
        this.estado = estado;
        this.fotoPerfil_url = fotoPerfil_url;
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
        this.contactoEmergenciaParentesco = contactoEmergenciaParentesco;
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }

    // Métodos Getters y Setters
    public String getId() 
    {return id;}

    public void setId(String id) 
    {this.id = id;}

    public String getNombre() 
    {return nombre;}

    public void setNombre(String nombre) 
    {this.nombre = nombre;}
    
    public String getApellido() 
    {return apellido;}

    public void setApellido(String apellido) 
    {this.apellido = apellido;}

    public String getDocumento() 
    {return documento;}

    public void setDocumento(String documento) 
    {this.documento = documento;}
    
    public String getTipoSangre()
    {return tipoSangre;}
    
    public void setTipoSangre (String tipoSangre)
    {this.tipoSangre = tipoSangre;}
    
    public String getEmail()
    {return Email;}
    
    public void setEmail(String Email)
    {this.Email = Email;}
    
    public String getDireccion()
    {return direccion;}
    
    public void setDireccion(String direccion)
    {this.direccion = direccion;}

    public String getCargo() 
    {return cargo;}

    public void setCargo(String cargo) 
    {this.cargo = cargo;}

    public String getEstado() 
    {return estado;}

    public void setEstado(String estado) 
    {this.estado = estado;}

    public String getFoto() 
    {return fotoPerfil_url;}

    public void setFoto(String fotoPerfil_url) 
    {this.fotoPerfil_url = fotoPerfil_url;}

    public String getTelefonoCelular() 
    {return telefonoCelular;}

    public void setTelefonoCelular(String telefonoCelular) 
    {this.telefonoCelular = telefonoCelular;}


    public String getContactoEmergenciaNombre() 
    {return contactoEmergenciaNombre;}

    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) 
    {this.contactoEmergenciaNombre = contactoEmergenciaNombre;}

    public String getContactoEmergenciaTelefono() 
    {return contactoEmergenciaTelefono;}

    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) 
    {this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;}
    
    public String getContactoEmergenciaParentesco() 
    { return contactoEmergenciaParentesco; }
    
    public void setContactoEmergenciaParentesco(String par) 
    { this.contactoEmergenciaParentesco = par; }
}
