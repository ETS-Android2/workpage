package jajimenez.workpage;

import java.io.File;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;

public class ImportDataService extends IntentService {
    public static final int STATUS_NOT_RUNNING = 0;
    public static final int STATUS_RUNNING = 1;

    private static int status = STATUS_NOT_RUNNING;

    public ImportDataService() {
        super("import_data_service");
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

        int result = applicationLogic.importData(file);
        status = STATUS_NOT_RUNNING;

        Intent resultIntent = new Intent(ApplicationConstants.DATA_IMPORT_ACTION);
        resultIntent.putExtra(ApplicationConstants.DATA_IMPORT_RESULT, result);

        context.sendBroadcast(resultIntent);
    }
}
