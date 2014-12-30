package jajimenez.workpage;

import android.app.ListActivity;
import android.os.Bundle;

public class EditTaskTagsActivity extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task_tags);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);
    }
}
