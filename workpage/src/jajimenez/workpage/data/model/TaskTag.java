package jajimenez.workpage.data.model;

public class TaskTag extends Entity {
    private int workspaceId;
    private String name;
    private int order;

    public TaskTag() {
        super();
        this.workspaceId = -1;
        this.name = "";
        this.order = 0;
    }

    public TaskTag(int workspaceId, String name, int order) {
        super();
        this.workspaceId = workspaceId;
        this.name = name;
        this.order = order;
    }

    public TaskTag(int id, int workspaceId, String name, int order) {
        super(id);
        this.workspaceId = workspaceId;
        this.name = name;
        this.order = order;
    }

    public int getWorkspaceId() {
        return this.workspaceId;
    }

    public String getName() {
        return this.name;
    }

    public int getOrder() {
        return this.order;
    }

    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
