package jajimenez.workpage.data.model;

import java.lang.Comparable;

public class TaskTag extends Entity implements Comparable<TaskTag> {
    private long contextId;
    private String name;
    private long order;

    public TaskTag() {
        super();

        contextId = -1;
        name = "";
        order = 0;
    }

    public TaskTag(long contextId, String name, long order) {
        super();

        this.contextId = contextId;
        this.name = name;
        this.order = order;
    }

    public TaskTag(long id, long contextId, String name, long order) {
        super(id);

        this.contextId = contextId;
        this.name = name;
        this.order = order;
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

    public void setContextId(long contextId) {
        this.contextId = contextId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(long order) {
        this.order = order;
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
