package jajimenez.workpage.data.model;

public class TaskContext extends Entity {
    private String name;
    private long order;

    public TaskContext() {
        super();

        name = "";
        order = 0;
    }

    public TaskContext(String name, long order) {
        super();

        this.name = name;
        this.order = order;
    }

    public TaskContext(long id, String name, long order) {
        super(id);

        this.name = name;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public long getOrder() {
        return order;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(long order) {
        this.order = order;
    }
}
