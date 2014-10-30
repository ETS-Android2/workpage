package jajimenez.workpage.data.model;

public class Workspace extends Entity {
    private String name;
    private int order;

    public Workspace() {
        super();
        this.name = "";
        this.order = 0;
    }

    public Workspace(String name, int order) {
        super();
        this.name = name;
        this.order = order;
    }

    public Workspace(int id, String name, int order) {
        super(id);
        this.name = name;
        this.order = order;
    }

    public String getName() {
        return this.name;
    }

    public int getOrder() {
        return this.order;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
