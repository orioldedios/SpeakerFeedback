package oriol.jonathan.speakerfeedback;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import edu.upc.citm.android.speakerfeedback.R;

public class NewPollActivity extends AppCompatActivity
{
    private EditText editQuestion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_poll);

        editQuestion = findViewById(R.id.editQuestionLabel);
    }

    public void onAddClick(View view)
    {
        String question = editQuestion.getText().toString();
        Intent data = new Intent();
        data.putExtra("question", question);
        setResult(RESULT_OK, data);
        finish();
    }
}
