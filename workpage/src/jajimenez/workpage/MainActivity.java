package jajimenez.workpage;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Workspace;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    public void onAboutItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onSwitchWorkspaceSelected(MenuItem item) {
        ApplicationLogic applicationLogic = new ApplicationLogic(this);

        List<Workspace> workspaces = applicationLogic.getAllWorspaces();
        int workspaceCount = workspaces.size();
        String[] workspaceNames = new String[workspaceCount];
        for (int i = 0; i < workspaceCount; i++) workspaceNames[i] = workspaces.get(i).getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.switch_workspace);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setSingleChoiceItems(workspaceNames, 0, new DialogInterface.OnClickListener() {
            // ToDo
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
