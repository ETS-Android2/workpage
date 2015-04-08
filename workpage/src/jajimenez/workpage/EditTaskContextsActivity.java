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
import android.view.ActionMode;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;

public class EditTaskContextsActivity extends ListActivity {
    private Menu menu;
    private ListView listView;
    private TextView emptyTextView;
    private ActionBar actionBar;
    private ActionMode actionMode;

    private ApplicationLogic applicationLogic;
    private TaskContext currentContext;
    private List<TaskContext> contexts;

    private Bundle savedInstanceState;
    private boolean interfaceReady;

    private EditTaskContextDialogFragment.OnTaskContextSavedListener saveTaskContextListener;
    private DeleteTaskContextDialogFragment.OnDeleteListener deleteTaskContextListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.edit_task_contexts);

        listView = getListView();
        emptyTextView = (TextView) findViewById(android.R.id.empty);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionMode = null;

        createContextualActionBar();
        interfaceReady = false;

        saveTaskContextListener = new EditTaskContextDialogFragment.OnTaskContextSavedListener() {
            public void onTaskContextSaved() {
                // Close the contextual action bar.
                if (EditTaskContextsActivity.this.actionMode != null) EditTaskContextsActivity.this.actionMode.finish();

                // Update the list view.
                EditTaskContextsActivity.this.updateInterface();
            }
        };

        deleteTaskContextListener = new DeleteTaskContextDialogFragment.OnDeleteListener() {
            public void onDelete() {
                // Close the context action bar.
                if (EditTaskContextsActivity.this.actionMode != null) EditTaskContextsActivity.this.actionMode.finish();

                // Update the list view.
                EditTaskContextsActivity.this.updateInterface();
            }
        };

        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState != null) {
            EditTaskContextDialogFragment editTaskContextFragment = (EditTaskContextDialogFragment) (getFragmentManager()).findFragmentByTag("edit_task_context");
            if (editTaskContextFragment != null) editTaskContextFragment.setOnTaskContextSavedListener(saveTaskContextListener);

            DeleteTaskContextDialogFragment deleteFragment = (DeleteTaskContextDialogFragment) (getFragmentManager()).findFragmentByTag("delete_task_context");
            if (deleteFragment != null) deleteFragment.setOnDeleteListener(deleteTaskContextListener);
        }

        applicationLogic = new ApplicationLogic(this);
        currentContext = applicationLogic.getCurrentTaskContext();
        contexts = new LinkedList<TaskContext>();
    }

    private void createContextualActionBar() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                EditTaskContextsActivity.this.actionMode = mode;

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.edit_task_contexts_contextual, menu);

                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                EditTaskContextsActivity.this.actionMode = null;
            }

            // Returns "true" if this callback handled the event, "false"
            // if the standard "MenuItem" invocation should continue.
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                boolean eventHandled = false;
                final List<TaskContext> selectedContexts = EditTaskContextsActivity.this.getSelectedTaskContexts();

                switch (item.getItemId()) {
                    case R.id.editTaskContextsContextualMenu_edit:
                        // Show an edition dialog.
                        EditTaskContextDialogFragment editFragment = new EditTaskContextDialogFragment(selectedContexts.get(0), EditTaskContextsActivity.this.contexts);
                        editFragment.setOnTaskContextSavedListener(EditTaskContextsActivity.this.saveTaskContextListener);
                        editFragment.show(getFragmentManager(), "edit_task_context");

                        eventHandled = true;
                        break;

                    case R.id.editTaskContextsContextualMenu_delete:
                        // Show a deletion confirmation dialog.
                        DeleteTaskContextDialogFragment deleteFragment = new DeleteTaskContextDialogFragment(selectedContexts);
                        deleteFragment.setOnDeleteListener(EditTaskContextsActivity.this.deleteTaskContextListener);
                        deleteFragment.show(getFragmentManager(), "delete_task_context");

                        eventHandled = true;
                        break;
                }

                return eventHandled;
            }

            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectedContextCount = EditTaskContextsActivity.this.listView.getCheckedItemCount();
                mode.setTitle(EditTaskContextsActivity.this.getString(R.string.selected, selectedContextCount));

                MenuItem editItem = (mode.getMenu()).findItem(R.id.editTaskContextsContextualMenu_edit);
                Drawable editItemIcon = editItem.getIcon();

                if (selectedContextCount == 1) {
                    editItem.setEnabled(true);
                    editItemIcon.setAlpha(255);
                }
                else {
                    editItem.setEnabled(false);
                    editItemIcon.setAlpha(127);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_task_contexts, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInterface();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        List<Integer> selectedItems = getSelectedItems();
        int selectedItemCount = selectedItems.size();
        int[] selected = new int[selectedItemCount];

        for (int i = 0; i < selectedItemCount; i++) selected[i] = selectedItems.get(i);
        outState.putIntArray("selected_items", selected);

        if (actionMode != null) actionMode.finish();

        super.onSaveInstanceState(outState);
    }

    private void updateInterface() {
        // Show contexts.
        (new LoadTaskContextsDBTask()).execute();
    }

    private void updateTaskContextListInterface(List<TaskContext> contexts) {
        if (contexts == null) contexts = new LinkedList<TaskContext>();

        this.contexts = contexts;
        TaskContextAdapter adapter = new TaskContextAdapter(this, R.layout.task_context_list_item, contexts);
        setListAdapter(adapter);

        if (savedInstanceState != null) {
            int[] selectedItems = savedInstanceState.getIntArray("selected_items");

            if (selectedItems != null) {
                for (int position : selectedItems) listView.setItemChecked(position, true);
                savedInstanceState.remove("selected_items");
            }
        }
    }

    private List<Integer> getSelectedItems() {
        List<Integer> selectedItems = new LinkedList<Integer>();
        
        SparseBooleanArray itemSelectedStates = listView.getCheckedItemPositions();
        int itemCount = listView.getCount();

        for (int i = 0; i < itemCount; i++) {
            if (itemSelectedStates.get(i)) {
                // The item with position "i" is selected.
                selectedItems.add(i);
            }
        }

        return selectedItems;
    }

    private List<TaskContext> getSelectedTaskContexts() {
        List<TaskContext> selectedContexts = new LinkedList<TaskContext>();

        TaskContextAdapter adapter = (TaskContextAdapter) getListAdapter();
        List<Integer> selectedItems = getSelectedItems();
        
        for (int position : selectedItems) {
            TaskContext context = adapter.getItem(position);
            selectedContexts.add(context);
        }

        return selectedContexts;
    }

    public void onNewTaskContextItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        TaskContext newContext = new TaskContext();
        //newTag.setContextId(currentTaskContext.getId());

        // Show an edition dialog.
        EditTaskContextDialogFragment editFragment = new EditTaskContextDialogFragment(newContext, EditTaskContextsActivity.this.contexts);
        editFragment.setOnTaskContextSavedListener(saveTaskContextListener);
        editFragment.show(getFragmentManager(), "edit_task_context");
    }

    private class LoadTaskContextsDBTask extends AsyncTask<Void, Void, List<TaskContext>> {
        protected void onPreExecute() {
            EditTaskContextsActivity.this.interfaceReady = false;

            EditTaskContextsActivity.this.setProgressBarIndeterminateVisibility(true);
            EditTaskContextsActivity.this.listView.setEnabled(false);
        }

        protected List<TaskContext> doInBackground(Void... parameters) {
            return EditTaskContextsActivity.this.applicationLogic.getAllTaskContexts();
        }

        protected void onPostExecute(List<TaskContext> contexts) {
            EditTaskContextsActivity.this.updateTaskContextListInterface(contexts);

            EditTaskContextsActivity.this.listView.setEnabled(true);
            EditTaskContextsActivity.this.setProgressBarIndeterminateVisibility(false);

            EditTaskContextsActivity.this.interfaceReady = true;
        }
    }
}
