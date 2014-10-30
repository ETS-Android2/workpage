package jajimenez.workpage.data.model;

public class Entity {
    private int id;

    public Entity() {
        this.id = -1;
    }

    public Entity(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
