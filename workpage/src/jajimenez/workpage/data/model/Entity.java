package jajimenez.workpage.data.model;

public class Entity {
    private long id;

    public Entity() {
        id = -1;
    }

    public Entity(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
