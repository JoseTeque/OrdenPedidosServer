package m.google.ordenpedidosserver.model;

public class Category {
    private String name;
    private String Imagen;

    public Category() {
    }

    public Category(String name, String imagen) {
        this.name = name;
        Imagen = imagen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String imagen) {
        Imagen = imagen;
    }
}
