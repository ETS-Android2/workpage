package jajimenez.workpage.data.model;

public class TaskReminder extends Entity {
    private long minutes;

    public TaskReminder() {
        super();
        minutes = -1;
    }

    public TaskReminder(long minutes) {
        super();
        this.minutes = minutes;
    }

    public TaskReminder(long id, long minutes) {
        super(id);
        this.minutes = minutes;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }
}
