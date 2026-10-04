import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Modelo de un Pokemon listo para ser usado por la interfaz y la batalla. */
public final class Pokemon {
    private final int id;
    private final String nombre;
    private final List<String> tipos;
    private final String spriteUrl;
    private final int hpMaximo;
    private final int ataque;
    private final int defensa;
    private final int ataqueEspecial;
    private final int defensaEspecial;
    private final int velocidad;
    private final int peso;
    private final int altura;
    private final List<String> habilidades;
    private int hpActual;

    public Pokemon(int id, String nombre, List<String> tipos, String spriteUrl,
                   int hpMaximo, int ataque, int defensa, int velocidad) {
        this(id, nombre, tipos, spriteUrl, hpMaximo, ataque, defensa, 0, 0, velocidad, 0, 0, List.of());
    }

    public Pokemon(int id, String nombre, List<String> tipos, String spriteUrl,
               int hpMaximo, int ataque, int defensa, int ataqueEspecial,
               int defensaEspecial, int velocidad,
                   int peso, int altura, List<String> habilidades) {
        if (id < 1 || hpMaximo < 1 || ataque < 0 || defensa < 0 || ataqueEspecial < 0
            || defensaEspecial < 0 || velocidad < 0 || peso < 0 || altura < 0) {
            throw new IllegalArgumentException("Los datos numericos del Pokemon no son validos");
        }
        this.id = id;
        this.nombre = requireText(nombre, "nombre").toLowerCase(Locale.ROOT);
        this.tipos = List.copyOf(Objects.requireNonNull(tipos, "tipos"));
        if (this.tipos.isEmpty()) {
            throw new IllegalArgumentException("El Pokemon debe tener al menos un tipo");
        }
        this.spriteUrl = spriteUrl == null ? "" : spriteUrl;
        this.hpMaximo = hpMaximo;
        this.hpActual = hpMaximo;
        this.ataque = ataque;
        this.defensa = defensa;
        this.ataqueEspecial = ataqueEspecial;
        this.defensaEspecial = defensaEspecial;
        this.velocidad = velocidad;
        this.peso = peso;
        this.altura = altura;
        this.habilidades = List.copyOf(Objects.requireNonNull(habilidades, "habilidades"));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " no puede estar vacio");
        }
        return value.trim();
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public List<String> getTipos() { return Collections.unmodifiableList(tipos); }
    public String getSpriteUrl() { return spriteUrl; }
    public int getHpMaximo() { return hpMaximo; }
    public int getHpActual() { return hpActual; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }
    public int getAtaqueEspecial() { return ataqueEspecial; }
    public int getDefensaEspecial() { return defensaEspecial; }
    public int getVelocidad() { return velocidad; }
    public int getPeso() { return peso; }
    public int getAltura() { return altura; }
    public List<String> getHabilidades() { return Collections.unmodifiableList(habilidades); }

    public void recibirDano(int dano) {
        if (dano < 0) {
            throw new IllegalArgumentException("El dano no puede ser negativo");
        }
        hpActual = Math.max(0, hpActual - dano);
    }

    public void restaurar() {
        hpActual = hpMaximo;
    }

    public boolean estaDerrotado() {
        return hpActual == 0;
    }

    @Override
    public String toString() {
        return nombre + " (HP " + hpActual + "/" + hpMaximo + ")";
    }
}
