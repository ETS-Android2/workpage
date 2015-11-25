package jajimenez.workpage.data.model;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

public class Task extends Entity {
    private long contextId;
    private String title;
    private String description;
    private Calendar when;
    private boolean ignoreWhenTime;
    private Calendar start;
    private boolean ignoreStartTime;
    private Calendar deadline;
    private boolean ignoreDeadlineTime;
    private boolean done;
    private List<TaskTag> tags;

    public Task() {
        super();
        
        contextId = -1;
        title = "";
        description = "";
        when = null;
        ignoreWhenTime = false;
        start = null;
        ignoreStartTime = false;
        deadline = null;
        ignoreDeadlineTime = false;
        done = false;
        tags = new LinkedList<TaskTag>();
    }

    public Task(long contextId,
        String title, String description,
        Calendar when, boolean ignoreWhenTime,
        Calendar start, boolean ignoreStartTime,
        Calendar deadline, boolean ignoreDeadlineTime,
        boolean done, List<TaskTag> tags) {

        super();

        this.contextId = contextId;
        this.title = title;
        this.description = description;
        this.when = when;
        this.ignoreWhenTime = ignoreWhenTime;
        this.start = start;
        this.ignoreStartTime = ignoreStartTime;
        this.deadline = deadline;
        this.ignoreDeadlineTime = ignoreDeadlineTime;
        this.done = done;
        this.tags = tags;
    }

    public Task(long id, long contextId,
        String title, String description,
        Calendar when, boolean ignoreWhenTime,
        Calendar start, boolean ignoreStartTime,
        Calendar deadline, boolean ignoreDeadlineTime,
        boolean done, List<TaskTag> tags) {

        super(id);

        this.contextId = contextId;
        this.title = title;
        this.description = description;
        this.when = when;
        this.ignoreWhenTime = ignoreWhenTime;
        this.start = start;
        this.ignoreStartTime = ignoreStartTime;
        this.deadline = deadline;
        this.ignoreDeadlineTime = ignoreDeadlineTime;
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

    public Calendar getWhen() {
        return when;
    }

    public boolean getIgnoreWhenTime() {
        return ignoreWhenTime;
    }

    public Calendar getStart() {
        return start;
    }

    public boolean getIgnoreStartTime() {
        return ignoreStartTime;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public boolean getIgnoreDeadlineTime() {
        return ignoreDeadlineTime;
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

    public void setWhen(Calendar when) {
        this.when = when;
    }

    public void setIgnoreWhenTime(boolean ignore) {
        this.ignoreWhenTime = ignore;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public void setIgnoreStartTime(boolean ignore) {
        this.ignoreStartTime = ignore;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    public void setIgnoreDeadlineTime(boolean ignore) {
        this.ignoreDeadlineTime = ignore;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setTags(List<TaskTag> tags) {
        this.tags = tags;
    }
}
