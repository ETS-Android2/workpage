package jajimenez.workpage.data.model;

import java.lang.Comparable;

public class TaskTag extends Entity implements Comparable<TaskTag> {
    private long contextId;
    private String name;
    private long order;
    private String color; // Hexadecimal color, i.e. "fffff".

    public TaskTag() {
        super();

        contextId = -1;
        name = "";
        order = 0;
        color = null;
    }

    public TaskTag(long contextId, String name, long order, String color) {
        super();

        this.contextId = contextId;
        this.name = name;
        this.order = order;
        this.color = color;
    }

    public TaskTag(long id, long contextId, String name, long order, String color) {
        super(id);

        this.contextId = contextId;
        this.name = name;
        this.order = order;
        this.color = color;
    }

    public long getContextId() {
        return contextId;
    }

    public String getName() {
        return name;
    }

    public long getOrder() {
        return order;
    }

    public String getColor() {
        return color;
    }

    public void setContextId(long contextId) {
        this.contextId = contextId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof TaskTag && compareTo((TaskTag) other) == 0);
    }

    public int compareTo(TaskTag other) {
        int result = 1;
        if (other != null) result = this.name.compareTo(other.name);

        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
