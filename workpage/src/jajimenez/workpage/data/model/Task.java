package jajimenez.workpage.data.model;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

public class Task extends Entity {
    private long contextId;
    private String title;
    private String description;
    private Calendar start;
    private Calendar deadline;
    private boolean done;
    private Calendar doneTime;

    private List<TaskTag> tags;
    private List<Long> subtasks;
    private List<Long> requiredTasks;

    public Task() {
        super();
        
        contextId = -1;
        title = "";
        description = "";
        start = null;
        deadline = null;
        done = false;
        doneTime = null;

        tags = new LinkedList<TaskTag>();
        subtasks = new LinkedList<Long>();
        requiredTasks = new LinkedList<Long>();
    }

    public Task(long contextId, String title, String description,
        Calendar start, Calendar deadline, boolean done, Calendar doneTime,
        List<TaskTag> tags, List<Long> subtasks, List<Long> requiredTasks) {

        super();

        this.contextId = contextId;
        this.title = title;
        this.description = description;
        this.start = start;
        this.deadline = deadline;
        this.done = done;
        this.doneTime = doneTime;

        this.tags = tags;
        this.subtasks = subtasks;
        this.requiredTasks = requiredTasks;
    }

    public Task(long id, long contextId, String title, String description,
        Calendar start, Calendar deadline, boolean done, Calendar doneTime,
        List<TaskTag> tags, List<Long> subtasks, List<Long> requiredTasks) {

        super(id);

        this.contextId = contextId;
        this.title = title;
        this.description = description;
        this.start = start;
        this.deadline = deadline;
        this.done = done;
        this.doneTime = doneTime;

        this.tags = tags;
        this.subtasks = subtasks;
        this.requiredTasks = requiredTasks;
    }

    public long getContextId() {
        return contextId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getStart() {
        return start;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public boolean isDone() {
        return done;
    }

    public Calendar getDoneTime() {
        return doneTime;
    }

    public List<TaskTag> getTags() {
        return tags;
    }

    public List<Long> getSubtasks() {
        return subtasks;
    }

    public List<Long> getRequiredTasks() {
        return requiredTasks;
    }

    public void setContextId(long contextId) {
        this.contextId = contextId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setDoneTime(Calendar doneTime) {
        this.doneTime = doneTime;
    }

    public void setTags(List<TaskTag> tags) {
        this.tags = tags;
    }

    public void setSubstasks(List<Long> subtasks) {
        this.subtasks = subtasks;
    }

    public void setRequiredTasks(List<Long> requiredTasks) {
        this.requiredTasks = requiredTasks;
    }
}
