package jajimenez.workpage.data.model;

import java.lang.Comparable;

public class TaskContext extends Entity implements Comparable<TaskContext> {
    private String name;
    private long order;

    public TaskContext() {
        super();

        name = "";
        order = 0;
    }

    public TaskContext(String name) {
        super();

        this.name = name;
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

    @Override
    public boolean equals(Object other) {
        return (other instanceof TaskContext && compareTo((TaskContext) other) == 0);
    }

    public int compareTo(TaskContext other) {
        int result = 1;
        if (other != null) result = this.name.compareTo(other.name);

        return result;
    }
}
