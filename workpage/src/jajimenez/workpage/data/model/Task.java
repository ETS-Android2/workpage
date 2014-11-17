package jajimenez.workpage.data.model;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

public class Task extends Entity {
    private long taskContextId;
    private String title;
    private String description;
    private Calendar startDateTime;
    private Calendar endDateTime;
    private boolean done;
    private Calendar doneDateTime;

    private List<Long> tags;
    private List<Long> subtasks;
    private List<Long> requiredTasks;

    public Task() {
        super();
        
        taskContextId = -1;
        title = "";
        description = "";
        startDateTime = null;
        endDateTime = null;
        done = false;
        doneDateTime = null;

        tags = new LinkedList<Long>();
        subtasks = new LinkedList<Long>();
        requiredTasks = new LinkedList<Long>();
    }

    public Task(long taskContextId, String title, String description,
        Calendar startDateTime, Calendar endDateTime, boolean done, Calendar doneDateTime,
        List<Long> tags, List<Long> subtasks, List<Long> requiredTasks) {

        super();

        this.taskContextId = taskContextId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.done = done;
        this.doneDateTime = doneDateTime;

        this.tags = tags;
        this.subtasks = subtasks;
        this.requiredTasks = requiredTasks;
    }

    public Task(long id, long taskContextId, String title, String description,
        Calendar startDateTime, Calendar endDateTime, boolean done, Calendar doneDateTime,
        List<Long> tags, List<Long> subtasks, List<Long> requiredTasks) {

        super(id);

        this.taskContextId = taskContextId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.done = done;
        this.doneDateTime = doneDateTime;

        this.tags = tags;
        this.subtasks = subtasks;
        this.requiredTasks = requiredTasks;
    }

    public long getTaskContextId() {
        return taskContextId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getStartDateTime() {
        return startDateTime;
    }

    public Calendar getEndDateTime() {
        return endDateTime;
    }

    public boolean isDone() {
        return done;
    }

    public Calendar getDoneDateTime() {
        return doneDateTime;
    }

    public List<Long> getTags() {
        return tags;
    }

    public List<Long> getSubtasks() {
        return subtasks;
    }

    public List<Long> getRequiredTasks() {
        return requiredTasks;
    }

    public void setTaskContextId(long taskContextId) {
        this.taskContextId = taskContextId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDateTime(Calendar startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(Calendar endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setDoneDateTime(Calendar doneDateTime) {
        this.doneDateTime = doneDateTime;
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
