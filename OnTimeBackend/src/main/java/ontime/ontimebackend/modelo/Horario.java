package ontime.ontimebackend.modelo;

public class Horario {
    
    private int id;
    private String nombrejornada;
    private String horaEntrada; // Usamos String para mapear limpiamente el TIME de MySQL hacia el Frontend
    private String horaSalida;

    public Horario() {
    }

    public Horario(int id, String nombrejornada, String horaEntrada, String horaSalida) {
        this.id = id;
        this.nombrejornada = nombrejornada;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
    }

    // --- Métodos Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombrejornada() { return nombrejornada; }
    public void setNombrejornada(String nombrejornada) { this.nombrejornada = nombrejornada; }

    public String getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(String horaEntrada) { this.horaEntrada = horaEntrada; }

    public String getHoraSalida() { return horaSalida; }
    public void setHoraSalida(String horaSalida) { this.horaSalida = horaSalida; }
}

