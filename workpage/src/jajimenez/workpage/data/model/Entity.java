package jajimenez.workpage.data.model;

public class Entity {
    private long id;

    public Entity() {
        this.id = -1;
    }

    public Entity(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
}
