package jajimenez.workpage.logic;

import java.util.List;
import java.util.Calendar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;

import jajimenez.workpage.R;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class CsvExporter {
    private Context appContext;

    public CsvExporter(Context appContext) {
        this.appContext = appContext;
    }

    public void export(TaskContext context, List<Task> tasks, boolean fieldNames, boolean unixTime,
        boolean id, boolean description, boolean tags, File to) throws Exception {

        if (appContext == null) throw new Exception("Context is null.");
        if (context == null) throw new Exception("Task context is null.");
        if (to == null) throw new Exception("File is null.");

        ApplicationLogic logic = new ApplicationLogic(appContext);

        List<TaskTag> contextTags = logic.getAllTaskTags(context);
        int contextTagCount = contextTags.size();

        FileOutputStream stream = new FileOutputStream(to);
        OutputStreamWriter writer = new OutputStreamWriter(stream);

        if (fieldNames) {
            if (id) {
                writer.write(getCsvValue(appContext.getString(R.string.field_id)));
                writer.write(",");
            }

            writer.write(getCsvValue(appContext.getString(R.string.field_title)));
            writer.write(",");

            if (description) {
                writer.write(getCsvValue(appContext.getString(R.string.field_description)));
                writer.write(",");
            }

            writer.write(getCsvValue(appContext.getString(R.string.field_when)));
            writer.write(",");

            writer.write(getCsvValue(appContext.getString(R.string.field_start)));
            writer.write(",");

            writer.write(getCsvValue(appContext.getString(R.string.field_deadline)));
            writer.write(",");

            writer.write(getCsvValue(appContext.getString(R.string.field_state)));

            if (tags) {
                writer.write(",");

                for (int i = 0; i < contextTagCount; i++) {
                    TaskTag tag = contextTags.get(i);

                    writer.write(getCsvValue(tag.getName()));
                    if (i < (contextTagCount - 1)) writer.write(",");
                }
            }

            writer.write("\n");
        }

        int taskCount = 0;
        if (tasks != null) taskCount = tasks.size();

        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);

            if (id) {
                writer.write(getCsvValue(String.valueOf(task.getId())));
                writer.write(",");
            }

            writer.write(getCsvValue(task.getTitle()));
            writer.write(",");

            if (description) {
                writer.write(getCsvValue(task.getDescription()));
                writer.write(",");
            }

            Calendar when = task.getWhen();
            Calendar start = task.getStart();
            Calendar deadline = task.getDeadline();

            if (unixTime) {
                if (when != null) writer.write(getCsvValue(String.valueOf(when.getTimeInMillis())));
                writer.write(",");

                if (start != null) writer.write(getCsvValue(String.valueOf(start.getTimeInMillis())));
                writer.write(",");

                if (deadline != null) writer.write(getCsvValue(String.valueOf(deadline.getTimeInMillis())));
                writer.write(",");
            }
            else {
                TextTool tool = new TextTool();

                if (when != null) writer.write(getCsvValue(tool.getTaskDateText(appContext, task, false, TextTool.WHEN)));
                writer.write(",");

                if (start != null) writer.write(getCsvValue(tool.getTaskDateText(appContext, task, false, TextTool.START)));
                writer.write(",");

                if (deadline != null) writer.write(getCsvValue(tool.getTaskDateText(appContext, task, false, TextTool.DEADLINE)));
                writer.write(",");
            }

            if (task.isDone()) writer.write(getCsvValue(appContext.getString(R.string.value_closed)));
            else writer.write(getCsvValue(appContext.getString(R.string.value_open)));

            if (tags) {
                writer.write(",");
                List<TaskTag> taskTags = task.getTags();
                
                for (int j = 0; j < contextTagCount; j++) {
                    TaskTag contextTag = contextTags.get(j);

                    if (taskTags.contains(contextTag)) writer.write(getCsvValue(appContext.getString(R.string.value_yes)));
                    else writer.write(getCsvValue(appContext.getString(R.string.value_no)));

                    if (j < (contextTagCount - 1)) writer.write(",");
                }
            }

            if (i < (taskCount - 1)) writer.write("\n");
        }

        writer.close();
    }

    private String getCsvValue(String value) {
        if (value == null) value = "";

        StringBuilder builder = new StringBuilder();
        builder.append('"');
        builder.append(value.replace("\"", "\"\""));
        builder.append('"');

        return builder.toString();
    }
}
