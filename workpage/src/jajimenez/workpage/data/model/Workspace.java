package jajimenez.workpage.data.model;

public class Workspace extends Entity {
    private String name;
    private long order;

    public Workspace() {
        super();
        this.name = "";
        this.order = 0;
    }

    public Workspace(String name, long order) {
        super();
        this.name = name;
        this.order = order;
    }

    public Workspace(long id, String name, long order) {
        super(id);
        this.name = name;
        this.order = order;
    }

    public String getName() {
        return this.name;
    }

    public long getOrder() {
        return this.order;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(long order) {
        this.order = order;
    }
}
