package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Window;
import android.content.Intent;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Button;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.drawable.Drawable;
import java.io.File;

import jajimenez.workpage.logic.ApplicationLogic;

public class FileBrowserActivity extends ListActivity {
    private Menu menu;
    private ListView listView;
    private LinearLayout fileLinearLayout;
    private EditText fileNameEditText;
    private Button saveButton;
    private MenuItem goUpMenuItem;

    private ApplicationLogic applicationLogic;
    private File initialFile;
    private File currentFile;

    private boolean interfaceReady;
    private String mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.file_browser);

        listView = getListView();
        fileLinearLayout = (LinearLayout) findViewById(R.id.fileBrowser_file);
        fileNameEditText = (EditText) findViewById(R.id.fileBrowser_file_name);
        saveButton = (Button) findViewById(R.id.fileBrowser_save);
        goUpMenuItem = null;

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");

        if (mode != null && mode.equals("export")) {
            setTitle(R.string.export_data);
            fileLinearLayout.setVisibility(View.VISIBLE);
        }
        else if (mode != null && mode.equals("import")) {
            setTitle(R.string.import_data);
        }

        fileNameEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            public void afterTextChanged(Editable s) {
                String text = (s.toString()).trim();
                boolean enabled = (text.length() > 0);

                FileBrowserActivity.this.saveButton.setEnabled(enabled);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        saveButton.setEnabled(false);
        interfaceReady = false;

        applicationLogic = new ApplicationLogic(this);
        initialFile = Environment.getExternalStorageDirectory();
        currentFile = initialFile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file_browser, menu);

        goUpMenuItem = menu.findItem(R.id.fileBrowserMenu_goUp);
        goUpMenuItem.setEnabled(false);
        
        Drawable goUpItemIcon = goUpMenuItem.getIcon();
        goUpItemIcon.setAlpha(127);

        return true;
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

        if (goUpMenuItem != null) {
            Drawable goUpItemIcon = goUpMenuItem.getIcon();

            if (currentFile.equals(initialFile)) {
                goUpMenuItem.setEnabled(false);
                goUpItemIcon.setAlpha(127);
            }
            else {
                goUpMenuItem.setEnabled(true);
                goUpItemIcon.setAlpha(255);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        File selectedFile = (File) l.getItemAtPosition(position);

        if (selectedFile.isDirectory()) {
            currentFile = selectedFile;
            updateInterface();
        }
        else {
            if (mode != null && mode.equals("export")) {
                fileNameEditText.setText(selectedFile.getName());

                // ToDo: Export data.
            }
            else if (mode != null && mode.equals("import")) {
                // ToDo: Import data.
            }
        }
    }

    public void onGoUpItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        if (currentFile != null) {
            File parent = currentFile.getParentFile();

            if (parent != null) {
                currentFile = parent;
                updateInterface();
            }
        }
    }

    public void onSaveButtonClicked(View view) {
        // ToDo
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

            if (sf != null) {
                for (File f : sf) {
                    if (f.canRead()) subfiles.add(f);
                }
            }

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
