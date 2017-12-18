package jajimenez.workpage;

import java.util.List;

import jajimenez.workpage.data.model.Task;

public interface TaskContainerFragment {
    void setEnabled(boolean enabled);
    void setTasks(List<Task> tasks);
}
