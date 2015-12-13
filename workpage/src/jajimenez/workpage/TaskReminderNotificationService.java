package jajimenez.workpage;

import java.util.Calendar;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Task;

public class TaskReminderNotificationService extends IntentService {
    public TaskReminderNotificationService() {
        super("task_reminder_notification_service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        ApplicationLogic applicationLogic = new ApplicationLogic(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // The Reminder ID contains the task ID and the reminder type.
        int reminderId = intent.getIntExtra("reminder_id", -1);
        String reminderIdStr = String.valueOf(reminderId);
        int length = reminderIdStr.length();

        long taskId = Long.parseLong(reminderIdStr.substring(0, length - 1));
        Task task = applicationLogic.getTask(taskId);
        if (task == null) return;

        String reminderType = reminderIdStr.substring(length - 1);

        Intent recIntent = new Intent(context, TaskReminderAlarmReceiver.class);
        recIntent.putExtra("reminder_id", reminderId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, reminderId, recIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        String action = intent.getAction();

        if (action.equals(ApplicationConstants.TASK_REMINDER_DISMISS_ACTION)) {
            alarmManager.cancel(alarmIntent);

            if (reminderType.equals("0")) {
                // Type is "When".
                task.setWhenReminder(null);
            }
            else if (reminderType.equals("1")) {
                // Type is "Start".
                task.setStartReminder(null);
            }
            else if (reminderType.equals("2")) {
                // Type is "Deadline".
                task.setDeadlineReminder(null);
            }

            applicationLogic.saveTask(task);
        }
        else if (action.equals(ApplicationConstants.TASK_REMINDER_SNOOZE_ACTION)) {
            Calendar nextTime = Calendar.getInstance();
            nextTime.add(Calendar.MINUTE, 5); // 5 minutes from now.

            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime.getTimeInMillis(), alarmIntent);
        }

        notificationManager.cancel(reminderId);
    }
}
