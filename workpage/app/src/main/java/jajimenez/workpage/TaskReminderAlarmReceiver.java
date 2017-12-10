package jajimenez.workpage;

import java.util.Calendar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.Task;

public class TaskReminderAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ApplicationLogic applicationLogic = new ApplicationLogic(context);
        TextTool textTool = new TextTool();

        // The Reminder ID contains the task ID and the reminder type.
        int reminderId = intent.getIntExtra("task_reminder_id", -1);
        String reminderIdStr = String.valueOf(reminderId);
        int length = reminderIdStr.length();

        long taskId = Long.parseLong(reminderIdStr.substring(0, length - 1));
        Task task = applicationLogic.getTask(taskId);
        if (task == null) return;

        String reminderType = reminderIdStr.substring(length - 1);

        // Text for the notification.
        String title = task.getTitle();
        String text = null;

        Calendar calendar = null;

        if (reminderType.equals("0")) {
            // Type is "Single"
            task.setSingleReminder(null);

            calendar = task.getSingle();
            text = textTool.getTaskDateText(context, task, false, TextTool.SINGLE, true);
        }
        else if (reminderType.equals("1")) {
            // Type is "Start"
            task.setStartReminder(null);

            calendar = task.getStart();
            text = textTool.getTaskDateText(context, task, true, TextTool.START, true);
        }
        else if (reminderType.equals("2")) {
            // Type is "End"
            task.setEndReminder(null);

            calendar = task.getEnd();
            text = textTool.getTaskDateText(context, task, true, TextTool.END, true);
        }
        else {
            return;
        }

        if (calendar == null) return;

        applicationLogic.saveTask(task);
        launchNotification(context, taskId, reminderId, title, text);
    }

    private void launchNotification(Context context, long taskId, int reminderId, String title, String text) {
        // Define the notification buttons
        Intent taskIntent = new Intent(context, TaskActivity.class);
        taskIntent.putExtra("task_id", taskId);

        Intent dismissIntent = new Intent(context, TaskReminderAlarmService.class);
        dismissIntent.putExtra("task_reminder_id", reminderId);
        dismissIntent.setAction(ApplicationConstants.TASK_REMINDER_DISMISS_ACTION);
        PendingIntent dismissPendingIntent = PendingIntent.getService(context, reminderId, dismissIntent, 0);

        Intent snoozeIntent = new Intent(context, TaskReminderAlarmService.class);
        snoozeIntent.putExtra("task_reminder_id", reminderId);
        snoozeIntent.setAction(ApplicationConstants.TASK_REMINDER_SNOOZE_ACTION);
        PendingIntent snoozePendingIntent = PendingIntent.getService(context, reminderId, snoozeIntent, 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TaskActivity.class);
        stackBuilder.addNextIntent(taskIntent);
        PendingIntent taskPendingIntent = stackBuilder.getPendingIntent((int) taskId, PendingIntent.FLAG_CANCEL_CURRENT);

        // Create a notification channel (only when API level is equals or greater than 26)
        createNotificationChannel(context);

        // Configure the notification
        Builder builder = new Builder(context, "workpage");
        builder.setSmallIcon(R.mipmap.notification);
        builder.setContentTitle(title);
        builder.setTicker(title);
        builder.setContentText(text);
        builder.setContentIntent(taskPendingIntent);
        builder.setOngoing(true);
        builder.addAction(R.drawable.delete, context.getString(R.string.dismiss), dismissPendingIntent);
        builder.addAction(R.drawable.snooze, context.getString(R.string.snooze), snoozePendingIntent);

        // Launch the notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(reminderId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String id = "workpage";
        String name = context.getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String description = context.getString(R.string.workpage_reminders);

        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);

        manager.createNotificationChannel(channel);
    }
}
