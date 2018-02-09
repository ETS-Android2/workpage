package jajimenez.workpage.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.data.model.TaskTag;

public class JsonDataTool {
    // 1. Data Model to JSON

    // 1.1. Contexts

    public JSONObject getContextJson(TaskContext context) {
        JSONObject obj = new JSONObject();

        if (context != null) {
            try {
                obj.put("name", context.getName());
                obj.put("order", context.getOrder());
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return obj;
    }

    // Returns the JSON for the tags of a given task context
    public JSONArray getContextTagsJson(List<TaskTag> tags) {
        JSONArray array = new JSONArray();

        if (tags != null) {
            for (TaskTag tag : tags) {
                array.put(getContextTagJson(tag));
            }
        }

        return array;
    }

    private JSONObject getContextTagJson(TaskTag tag) {
        JSONObject obj = new JSONObject();

        if (tag != null) {
            try {
                obj.put("name", tag.getName());
                obj.put("color", tag.getColor());
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return obj;
    }

    // Returns the JSON for the tasks of a given task context
    public JSONArray getContextTasksJson(List<Task> tasks) {
        JSONArray array = new JSONArray();

        if (tasks != null) {
            for (Task task : tasks) {
                array.put(getTaskJson(task));
            }
        }

        return array;
    }


    // 1.2. Tasks

    private JSONObject getTaskJson(Task task) {
        JSONObject obj = new JSONObject();

        if (task != null) {
            try {
                obj.put("title", task.getTitle());
                obj.put("description", task.getDescription());

                Calendar single = task.getSingle();
                Calendar start = task.getStart();
                Calendar end = task.getEnd();

                if (single != null) {
                    obj.put("single", single.getTimeInMillis());
                    obj.put("ignore_single_time", task.getIgnoreSingleTime());

                    TimeZone singleTimeZone = single.getTimeZone();

                    if (singleTimeZone != null) {
                        String singleTimeZoneId = singleTimeZone.getID();

                        if (singleTimeZoneId != null && !singleTimeZoneId.equals("")) {
                            obj.put("single_time_zone_code", singleTimeZoneId);
                        }
                    }

                    TaskReminder singleReminder = task.getSingleReminder();
                    if (singleReminder != null) obj.put("single_reminder_min", singleReminder.getMinutes());
                }

                if (start != null) {
                    obj.put("start", start.getTimeInMillis());
                    obj.put("ignore_start_time", task.getIgnoreStartTime());

                    TimeZone startTimeZone = start.getTimeZone();

                    if (startTimeZone != null) {
                        String startTimeZoneId = startTimeZone.getID();

                        if (startTimeZoneId != null && !startTimeZoneId.equals("")) {
                            obj.put("start_time_zone_code", startTimeZoneId);
                        }
                    }

                    TaskReminder startReminder = task.getStartReminder();
                    if (startReminder != null) obj.put("start_reminder_min", startReminder.getMinutes());
                }

                if (end != null) {
                    obj.put("end", end.getTimeInMillis());
                    obj.put("ignore_end_time", task.getIgnoreEndTime());

                    TimeZone endTimeZone = end.getTimeZone();

                    if (endTimeZone != null) {
                        String endTimeZoneId = endTimeZone.getID();

                        if (endTimeZoneId != null && !endTimeZoneId.equals("")) {
                            obj.put("end_time_zone_code", endTimeZoneId);
                        }
                    }

                    TaskReminder endReminder = task.getEndReminder();
                    if (endReminder != null) obj.put("end_reminder_min", endReminder.getMinutes());
                }

                obj.put("done", task.isDone());

                List<TaskTag> tags = task.getTags();
                if (tags != null && tags.size() > 0) obj.put("tags", getTaskTagsJson(tags));
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return obj;
    }

    // Returns the JSON for the tags of a given task
    private JSONArray getTaskTagsJson(List<TaskTag> tags) {
        JSONArray array = new JSONArray();

        if (tags != null) {
            for (TaskTag tag : tags) {
                array.put(tag.getName());
            }
        }

        return array;
    }



    // 2. JSON to Data Model

    // 2.1. Contexts
    public TaskContext getContext(JSONObject obj) {
        TaskContext context = new TaskContext();

        if (obj != null) {
            try {
                if (obj.has("name")) context.setName(obj.getString("name"));
                if (obj.has("order")) context.setOrder(obj.getLong("order"));

            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return context;
    }

    // Returns the tags of a given context given its JSON tag array
    public List<TaskTag> getContextTags(JSONArray array) {
        List<TaskTag> tags = new LinkedList<>();

        if (array != null) {
            try {
                int count = array.length();

                for (int i = 0; i < count; i++) {
                    TaskTag tag = getContextTag(array.getJSONObject(i));
                    tags.add(tag);
                }
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return tags;
    }

    private TaskTag getContextTag(JSONObject obj) {
        TaskTag tag = new TaskTag();

        if (obj != null) {
            try {
                if (obj.has("name")) tag.setName(obj.getString("name"));
                if (obj.has("color")) tag.setColor(obj.getString("color"));
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return tag;
    }

    // Returns the tasks of a given context given its JSON task array
    public List<Task> getContextTasks(JSONArray array) {
        List<Task> tasks = new LinkedList<>();

        if (array != null) {
            try {
                int count = array.length();

                for (int i = 0; i < count; i++) {
                    Task task = getTask(array.getJSONObject(i));
                    tasks.add(task);
                }
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return tasks;
    }

    // 2.1. Tasks

    private Task getTask(JSONObject obj) {
        Task task = new Task();

        if (obj != null) {
            try {
                if (obj.has("title")) task.setTitle(obj.getString("title"));
                if (obj.has("description")) task.setDescription(obj.getString("description"));

                if (obj.has("single")) {
                    Calendar single = Calendar.getInstance();
                    single.setTimeInMillis(obj.getLong("single"));

                    if (obj.has("ignore_single_time")) {
                        task.setIgnoreSingleTime(obj.getBoolean("ignore_single_time"));
                    }

                    if (obj.has("single_time_zone_code")) {
                        TimeZone singleTimeZone = TimeZone.getTimeZone(obj.getString("single_time_zone_code"));
                        single.setTimeZone(singleTimeZone);
                    }

                    if (obj.has("single_reminder_min")) {
                        TaskReminder singleReminder = new TaskReminder();
                        singleReminder.setMinutes(obj.getLong("single_reminder_min"));

                        task.setSingleReminder(singleReminder);
                    }

                    task.setSingle(single);
                }

                if (obj.has("start")) {
                    Calendar start = Calendar.getInstance();
                    start.setTimeInMillis(obj.getLong("start"));

                    if (obj.has("ignore_start_time")) {
                        task.setIgnoreStartTime(obj.getBoolean("ignore_start_time"));
                    }

                    if (obj.has("start_time_zone_code")) {
                        TimeZone startTimeZone = TimeZone.getTimeZone(obj.getString("start_time_zone_code"));
                        start.setTimeZone(startTimeZone);
                    }

                    if (obj.has("start_reminder_min")) {
                        TaskReminder startReminder = new TaskReminder();
                        startReminder.setMinutes(obj.getLong("start_reminder_min"));

                        task.setStartReminder(startReminder);
                    }

                    task.setStart(start);
                }

                if (obj.has("end")) {
                    Calendar end = Calendar.getInstance();
                    end.setTimeInMillis(obj.getLong("end"));

                    if (obj.has("ignore_end_time")) {
                        task.setIgnoreEndTime(obj.getBoolean("ignore_end_time"));
                    }

                    if (obj.has("end_time_zone_code")) {
                        TimeZone endTimeZone = TimeZone.getTimeZone(obj.getString("end_time_zone_code"));
                        end.setTimeZone(endTimeZone);
                    }

                    if (obj.has("end_reminder_min")) {
                        TaskReminder endReminder = new TaskReminder();
                        endReminder.setMinutes(obj.getLong("end_reminder_min"));

                        task.setEndReminder(endReminder);
                    }

                    task.setEnd(end);
                }

                if (obj.has("tags")) {
                    List<TaskTag> tags = getTaskTags(obj.getJSONArray("tags"));
                    if (tags.size() > 0) task.setTags(tags);
                }
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return task;
    }

    // Returns the tags of a given task given its JSON tag array
    private List<TaskTag> getTaskTags(JSONArray array) {
        List<TaskTag> tags = new LinkedList<>();

        if (array != null) {
            try {
                int count = array.length();

                for (int i = 0; i < count; i++) {
                    TaskTag tag = new TaskTag();
                    tag.setName(array.getString(i));

                    // Note: It's not needed to set the color of the tag as the full tag
                    // (including its color) must be in the tag list of the context.

                    tags.add(tag);
                }
            } catch (JSONException e) {
                // Nothing to do
            }
        }

        return tags;
    }
}
