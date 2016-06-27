package jajimenez.workpage;

import java.util.Calendar;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

public class TaskReminderAlarmService extends IntentService {
    public TaskReminderAlarmService() {
        super("task_reminder_alarm_service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int reminderId = intent.getIntExtra("task_reminder_id", -1);
        String action = intent.getAction();

        if (action.equals(ApplicationConstants.TASK_REMINDER_SNOOZE_ACTION)) {
            Intent recIntent = new Intent(context, TaskReminderAlarmReceiver.class);
            recIntent.putExtra("task_reminder_id", reminderId);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, reminderId, recIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar nextTime = Calendar.getInstance();
            nextTime.add(Calendar.MINUTE, 5); // 5 minutes from now.

            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime.getTimeInMillis(), alarmIntent);
        }

        notificationManager.cancel(reminderId);
    }
}
