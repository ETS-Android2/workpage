package jajimenez.workpage.data.model;

import java.util.List;
import java.util.ArrayList;

public class Task extends Entity {
    private int workspaceId;
    private String title;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private boolean done;

    private List<Integer> tags;
    private List<Integer> subtasks;
    private List<Integer> requiredTasks;

    public Task() {
        super();
        
        this.workspaceId = -1;
        this.title = "";
        this.description = "";
        this.startDateTime = null;
        this.endDateTime = null;
        this.done = false;

        this.tags = new ArrayList<Integer>();
        this.subtasks = new ArrayList<Integer>();
        this.requiredTasks = new ArrayList<Integer>();
    }

    public Task(int workspaceId, String title, String description, String startDateTime, String endDateTime, boolean done, List<Integer> tags, List<Integer> subtasks, List<Integer> requiredTasks) {
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

    public Task(int id, int workspaceId, String title, String description, String startDateTime, String endDateTime, boolean done, List<Integer> tags, List<Integer> subtasks, List<Integer> requiredTasks) {
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

    public int getWorkspaceId() {
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

    public List<Integer> getTags() {
        return this.tags;
    }

    public List<Integer> getSubtasks() {
        return this.subtasks;
    }

    public List<Integer> getRequiredTasks() {
        return this.requiredTasks;
    }

    public void setWorkspaceId(int workspaceId) {
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

    public void setTags(List<Integer> tags) {
        this.tags = tags;
    }

    public void setSubstasks(List<Integer> subtasks) {
        this.subtasks = subtasks;
    }

    public void setRequiredTasks(List<Integer> requiredTasks) {
        this.requiredTasks = requiredTasks;
    }
}
