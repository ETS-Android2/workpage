package jajimenez.workpage.data.model;

public class TaskTag extends Entity {
    private long workspaceId;
    private String name;
    private long order;

    public TaskTag() {
        super();
        this.workspaceId = -1;
        this.name = "";
        this.order = 0;
    }

    public TaskTag(long workspaceId, String name, long order) {
        super();
        this.workspaceId = workspaceId;
        this.name = name;
        this.order = order;
    }

    public TaskTag(long id, long workspaceId, String name, long order) {
        super(id);
        this.workspaceId = workspaceId;
        this.name = name;
        this.order = order;
    }

    public long getWorkspaceId() {
        return this.workspaceId;
    }

    public String getName() {
        return this.name;
    }

    public long getOrder() {
        return this.order;
    }

    public void setWorkspaceId(long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(long order) {
        this.order = order;
    }
}
