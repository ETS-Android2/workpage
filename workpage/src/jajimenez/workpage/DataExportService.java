package jajimenez.workpage;

import java.io.File;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;

public class DataExportService extends IntentService {
    public static final int STATUS_NOT_RUNNING = 0;
    public static final int STATUS_RUNNING = 1;

    private static int status = STATUS_NOT_RUNNING;

    public DataExportService() {
        super("data_export_service");
        status = STATUS_RUNNING;
    }

    public static int getStatus() {
        return status;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String filePath = intent.getStringExtra("file_path");
        File file = new File(filePath);

        Context context = getApplicationContext();
        ApplicationLogic applicationLogic = new ApplicationLogic(context);

        // "result" will be "false" if the operation was
        // successful or "true" if there was any error.
        boolean result = applicationLogic.exportData(file);

        status = STATUS_NOT_RUNNING;

        Intent resultIntent = new Intent(ApplicationConstants.DATA_EXPORT_ACTION);
        resultIntent.putExtra(ApplicationConstants.DATA_EXPORT_RESULT, result);

        context.sendBroadcast(resultIntent);
    }
}
