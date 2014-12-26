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
    private List<TaskTag> tags;

    public Task() {
        super();
        
        contextId = -1;
        title = "";
        description = "";
        start = null;
        deadline = null;
        done = false;
        tags = new LinkedList<TaskTag>();
    }

    public Task(long contextId, String title, String description,
        Calendar start, Calendar deadline, boolean done, List<TaskTag> tags) {

        super();

        this.contextId = contextId;
        this.title = title;
        this.description = description;
        this.start = start;
        this.deadline = deadline;
        this.done = done;
        this.tags = tags;
    }

    public Task(long id, long contextId, String title, String description,
        Calendar start, Calendar deadline, boolean done, List<TaskTag> tags) {

        super(id);

        this.contextId = contextId;
        this.title = title;
        this.description = description;
        this.start = start;
        this.deadline = deadline;
        this.done = done;
        this.tags = tags;
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

    public List<TaskTag> getTags() {
        return tags;
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

    public void setTags(List<TaskTag> tags) {
        this.tags = tags;
    }
}
