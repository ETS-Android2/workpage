package jajimenez.workpage.data.model;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

public class Task extends Entity {
    private long contextId;
    private String title;
    private String description;

    private Calendar single;
    private boolean ignoreSingleTime;
    private TaskReminder singleReminder;

    private Calendar start;
    private boolean ignoreStartTime;
    private TaskReminder startReminder;

    private Calendar end;
    private boolean ignoreEndTime;
    private TaskReminder endReminder;

    private boolean done;
    private List<TaskTag> tags;

    public Task() {
        super();
        
        contextId = -1;
        title = "";
        description = "";

        single = null;
        ignoreSingleTime = false;
        singleReminder = null;

        start = null;
        ignoreStartTime = false;
        startReminder = null;

        end = null;
        ignoreEndTime = false;
        endReminder = null;

        done = false;
        tags = new LinkedList<>();
    }

    public Task(long contextId,
        String title, String description,
        Calendar single, boolean ignoreSingleTime, TaskReminder singleReminder,
        Calendar start, boolean ignoreStartTime, TaskReminder startReminder,
        Calendar end, boolean ignoreEndTime, TaskReminder endReminder,
        boolean done, List<TaskTag> tags) {

        super();

        this.contextId = contextId;
        this.title = title;
        this.description = description;

        this.single = single;
        this.ignoreSingleTime = ignoreSingleTime;
        this.singleReminder = singleReminder;

        this.start = start;
        this.ignoreStartTime = ignoreStartTime;
        this.startReminder = startReminder;

        this.end = end;
        this.ignoreEndTime = ignoreEndTime;
        this.endReminder = endReminder;

        this.done = done;
        this.tags = tags;
    }

    public Task(long id, long contextId,
        String title, String description,
        Calendar single, boolean ignoreSingleTime, TaskReminder singleReminder,
        Calendar start, boolean ignoreStartTime, TaskReminder startReminder,
        Calendar end, boolean ignoreEndTime, TaskReminder endReminder,
        boolean done, List<TaskTag> tags) {

        super(id);

        this.contextId = contextId;
        this.title = title;
        this.description = description;

        this.single = single;
        this.ignoreSingleTime = ignoreSingleTime;
        this.singleReminder = singleReminder;

        this.start = start;
        this.ignoreStartTime = ignoreStartTime;
        this.startReminder = startReminder;

        this.end = end;
        this.ignoreEndTime = ignoreEndTime;
        this.endReminder = endReminder;

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

    public Calendar getSingle() {
        return single;
    }

    public boolean getIgnoreSingleTime() {
        return ignoreSingleTime;
    }

    public TaskReminder getSingleReminder() {
        return singleReminder;
    }

    public Calendar getStart() {
        return start;
    }

    public boolean getIgnoreStartTime() {
        return ignoreStartTime;
    }

    public TaskReminder getStartReminder() {
        return startReminder;
    }

    public Calendar getEnd() {
        return end;
    }

    public boolean getIgnoreEndTime() {
        return ignoreEndTime;
    }

    public TaskReminder getEndReminder() {
        return endReminder;
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

    public void setSingle(Calendar single) {
        this.single = single;
    }

    public void setIgnoreSingleTime(boolean ignore) {
        this.ignoreSingleTime = ignore;
    }

    public void setSingleReminder(TaskReminder reminder) {
        this.singleReminder = reminder;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public void setIgnoreStartTime(boolean ignore) {
        this.ignoreStartTime = ignore;
    }

    public void setStartReminder(TaskReminder reminder) {
        this.startReminder = reminder;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }

    public void setIgnoreEndTime(boolean ignore) {
        this.ignoreEndTime = ignore;
    }

    public void setEndReminder(TaskReminder reminder) {
        this.endReminder = reminder;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setTags(List<TaskTag> tags) {
        this.tags = tags;
    }
}
