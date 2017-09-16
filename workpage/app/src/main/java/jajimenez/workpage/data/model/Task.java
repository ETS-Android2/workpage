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
    //private String whenTimeZoneCode;
    private TaskReminder whenReminder;

    private Calendar start;
    private boolean ignoreStartTime;
    //private String startTimeZoneCode;
    private TaskReminder startReminder;

    private Calendar deadline;
    private boolean ignoreDeadlineTime;
    //private String deadlineTimeZoneCode;
    private TaskReminder deadlineReminder;

    private boolean done;
    private List<TaskTag> tags;

    public Task() {
        super();
        
        contextId = -1;
        title = "";
        description = "";

        when = null;
        ignoreWhenTime = false;
        //whenTimeZoneCode = null;
        whenReminder = null;

        start = null;
        ignoreStartTime = false;
        //startTimeZoneCode = null;
        startReminder = null;

        deadline = null;
        ignoreDeadlineTime = false;
        //deadlineTimeZoneCode = null;
        deadlineReminder = null;

        done = false;
        tags = new LinkedList<TaskTag>();
    }

    public Task(long contextId,
        String title, String description,
        Calendar when, boolean ignoreWhenTime, TaskReminder whenReminder,
        Calendar start, boolean ignoreStartTime, TaskReminder startReminder,
        Calendar deadline, boolean ignoreDeadlineTime, TaskReminder deadlineReminder,
        boolean done, List<TaskTag> tags) {

        super();

        this.contextId = contextId;
        this.title = title;
        this.description = description;

        this.when = when;
        this.ignoreWhenTime = ignoreWhenTime;
        //this.whenTimeZoneCode = whenTimeZoneCode;
        this.whenReminder = whenReminder;

        this.start = start;
        this.ignoreStartTime = ignoreStartTime;
        //this.startTimeZoneCode = startTimeZoneCode;
        this.startReminder = startReminder;

        this.deadline = deadline;
        this.ignoreDeadlineTime = ignoreDeadlineTime;
        //this.deadlineTimeZoneCode = deadlineTimeZoneCode;
        this.deadlineReminder = deadlineReminder;

        this.done = done;
        this.tags = tags;
    }

    public Task(long id, long contextId,
        String title, String description,
        Calendar when, boolean ignoreWhenTime, /*String whenTimeZoneCode,*/ TaskReminder whenReminder,
        Calendar start, boolean ignoreStartTime, /*String startTimeZoneCode,*/ TaskReminder startReminder,
        Calendar deadline, boolean ignoreDeadlineTime, /*String deadlineTimeZoneCode,*/ TaskReminder deadlineReminder,
        boolean done, List<TaskTag> tags) {

        super(id);

        this.contextId = contextId;
        this.title = title;
        this.description = description;

        this.when = when;
        this.ignoreWhenTime = ignoreWhenTime;
        //this.whenTimeZoneCode = whenTimeZoneCode;
        this.whenReminder = whenReminder;

        this.start = start;
        this.ignoreStartTime = ignoreStartTime;
        //this.startTimeZoneCode = startTimeZoneCode;
        this.startReminder = startReminder;

        this.deadline = deadline;
        this.ignoreDeadlineTime = ignoreDeadlineTime;
        //this.deadlineTimeZoneCode = deadlineTimeZoneCode;
        this.deadlineReminder = deadlineReminder;

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

    /*public String getWhenTimeZoneCode() {
        return whenTimeZoneCode;
    }*/

    public TaskReminder getWhenReminder() {
        return whenReminder;
    }

    public Calendar getStart() {
        return start;
    }

    public boolean getIgnoreStartTime() {
        return ignoreStartTime;
    }

    /*public String getStartTimeZoneCode() {
        return startTimeZoneCode;
    }*/

    public TaskReminder getStartReminder() {
        return startReminder;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public boolean getIgnoreDeadlineTime() {
        return ignoreDeadlineTime;
    }

    /*public String getDeadlineTimeZoneCode() {
        return deadlineTimeZoneCode;
    }*/

    public TaskReminder getDeadlineReminder() {
        return deadlineReminder;
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

    /*public void setWhenTimeZoneCode(String code) {
        this.whenTimeZoneCode = code;
    }*/

    public void setWhenReminder(TaskReminder reminder) {
        this.whenReminder = reminder;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public void setIgnoreStartTime(boolean ignore) {
        this.ignoreStartTime = ignore;
    }

    /*public void setStartTimeZoneCode(String code) {
        this.startTimeZoneCode = code;
    }*/

    public void setStartReminder(TaskReminder reminder) {
        this.startReminder = reminder;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    public void setIgnoreDeadlineTime(boolean ignore) {
        this.ignoreDeadlineTime = ignore;
    }

    /*public void setDeadlineTimeZoneCode(String code) {
        this.deadlineTimeZoneCode = code;
    }*/

    public void setDeadlineReminder(TaskReminder reminder) {
        this.deadlineReminder = reminder;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void setTags(List<TaskTag> tags) {
        this.tags = tags;
    }
}