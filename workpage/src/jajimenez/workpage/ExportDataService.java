package jajimenez.workpage;

import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;

public class ExportDataService extends IntentService {
    public ExportDataService() {
        super("export_data_service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String filePath = intent.getStringExtra("file_path");
        File file = new File(filePath);

        ApplicationLogic applicationLogic = new ApplicationLogic(getApplicationContext());

        try {
            applicationLogic.exportData(file);
        }
        catch (IOException e) {
        }
    }
}
