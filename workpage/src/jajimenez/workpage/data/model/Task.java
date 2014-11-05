package jajimenez.workpage.data.model;

import java.util.List;
import java.util.ArrayList;

public class Task extends Entity {
    private long workspaceId;
    private String title;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private boolean done;

    private List<Long> tags;
    private List<Long> subtasks;
    private List<Long> requiredTasks;

    public Task() {
        super();
        
        this.workspaceId = -1;
        this.title = "";
        this.description = "";
        this.startDateTime = null;
        this.endDateTime = null;
        this.done = false;

        this.tags = new ArrayList<Long>();
        this.subtasks = new ArrayList<Long>();
        this.requiredTasks = new ArrayList<Long>();
    }

    public Task(long workspaceId, String title, String description, String startDateTime, String endDateTime, boolean done, List<Long> tags, List<Long> subtasks, List<Long> requiredTasks) {
        super();

        this.workspaceId = workspaceId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.done = done;

        this.tags = tags;
        this.subtasks = subtasks;
        this.requiredTasks = requiredTasks;
    }

    public Task(long id, long workspaceId, String title, String description, String startDateTime, String endDateTime, boolean done, List<Long> tags, List<Long> subtasks, List<Long> requiredTasks) {
        super(id);
        this.workspaceId = workspaceId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.done = done;

        this.tags = tags;
        this.subtasks = subtasks;
        this.requiredTasks = requiredTasks;
    }

    public long getWorkspaceId() {
        return this.workspaceId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStartDateTime() {
        return this.startDateTime;
    }

    public String getEndDateTime() {
        return this.endDateTime;
    }

    public boolean isDone() {
        return this.done;
    }

    public List<Long> getTags() {
        return this.tags;
    }

    public List<Long> getSubtasks() {
        return this.subtasks;
    }

    public List<Long> getRequiredTasks() {
        return this.requiredTasks;
    }

    public void setWorkspaceId(long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }

    public void setSubstasks(List<Long> subtasks) {
        this.subtasks = subtasks;
    }

    public void setRequiredTasks(List<Long> requiredTasks) {
        this.requiredTasks = requiredTasks;
    }
}
