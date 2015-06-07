package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Window;
import android.content.Intent;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;

import jajimenez.workpage.logic.ApplicationLogic;

public class FileBrowserActivity extends ListActivity {
    private Menu menu;
    private ListView listView;
    private TextView emptyTextView;
    private ActionBar actionBar;

    private ApplicationLogic applicationLogic;
    private File currentFile;

    private boolean interfaceReady;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.file_browser);

        listView = getListView();
        emptyTextView = (TextView) findViewById(android.R.id.empty);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        if (mode != null && mode.equals("export")) setTitle(R.string.export_data);
        else if (mode != null && mode.equals("import")) setTitle(R.string.import_data);

        interfaceReady = false;

        applicationLogic = new ApplicationLogic(this);
        currentFile = new File("/");
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInterface();
    }

    private void updateInterface() {
        // Show files.
        (new LoadFilesTask()).execute();
    }

    private void updateFileListInterface(List<File> subfiles) {
        if (subfiles == null) subfiles = new LinkedList<File>();

        FileAdapter adapter = new FileAdapter(this, R.layout.file_list_item, subfiles);
        setListAdapter(adapter);
    }

    private class LoadFilesTask extends AsyncTask<Void, Void, List<File>> {
        protected void onPreExecute() {
            FileBrowserActivity.this.interfaceReady = false;

            FileBrowserActivity.this.setProgressBarIndeterminateVisibility(true);
            FileBrowserActivity.this.listView.setEnabled(false);
        }

        protected List<File> doInBackground(Void... parameters) {
            List<File> subfiles = new LinkedList<File>();

            File[] sf = FileBrowserActivity.this.currentFile.listFiles();
            for (File f : sf) subfiles.add(f);

            return subfiles;
        }

        protected void onPostExecute(List<File> subfiles) {
            FileBrowserActivity.this.updateFileListInterface(subfiles);

            FileBrowserActivity.this.listView.setEnabled(true);
            FileBrowserActivity.this.setProgressBarIndeterminateVisibility(false);

            FileBrowserActivity.this.interfaceReady = true;
        }
    }
}
