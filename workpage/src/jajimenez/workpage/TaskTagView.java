package jajimenez.workpage;

import android.util.AttributeSet;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;

public class TaskTagView extends LinearLayout {
    private TextView tagNameTextView;
    private ImageButton removeImageButton;

    public TaskTagView(Context context) {
        super(context);
        init(context);
    }

    public TaskTagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TaskTagView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.task_tag, this);

        tagNameTextView = (TextView) findViewById(R.id.taskTag_name);
        removeImageButton = (ImageButton) findViewById(R.id.taskTag_remove);
    }

    public void setText(String text) {
        tagNameTextView.setText(text);
    }

    public void setOnRemoveIconClickListener(View.OnClickListener listener) {
        removeImageButton.setOnClickListener(listener);
    }
}
