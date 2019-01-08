package oriol.jonathan.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.upc.citm.android.speakerfeedback.R;

public class RoomListActivity extends AppCompatActivity {

    List<Room> roomList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    void ShowToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        Intent intent = getIntent();
        boolean closeApp = intent.getBooleanExtra("EXIT", false);
        if(closeApp)
        {
            finish();
        }

        db.collection("rooms").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(e != null)
                {
                    Log.e("SpeakerFeedback", e.getMessage());
                    return;
                }

                for(DocumentSnapshot document : documentSnapshots)
                {
                    Room room = document.toObject(Room.class);
                    roomList.add(room);
                }
            }
        });

        ShowRoomDialog();

    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.room_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.CloseApp:
            {
                CloseApp();
                break;
            }
            case R.id.EnterRoom:
            {
                ShowRoomDialog();
                break;
            }
        }
        return true;
    }

    private void CloseApp()
    {
        Intent intent = new Intent(getApplicationContext(), RoomListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    private void ShowRoomDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.inputtextdialog, null);
        final EditText input = dialogView.findViewById(R.id.editText);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("Input a Room ID")
                .setView(dialogView)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String roomID = input.getText().toString();

                        //Search for this room, if exist proceed with the Password, else toast an error.

                        boolean exists = false;
                        Room desiredRoom = null;
                        for(Room room : roomList)
                        {
                            if(room.name.equals(roomID))
                            {
                                exists = true;
                                desiredRoom = room;
                                break;
                            }
                        }

                        if(exists)
                        {
                            if(desiredRoom != null && !desiredRoom.password.equals(""))
                            {
                                ShowPasswordDialog(desiredRoom);
                            }
                            else
                            {
                                ShowToast("Joined " + "\"" + roomID + "\"");

                                //Join the room
                                Intent intent = new Intent(RoomListActivity.this, PollListActivity.class);
                                intent.putExtra("roomName", roomID);
                                startActivity(intent);
                            }
                        }
                        else
                        {
                            ShowToast("Room " + "\"" + roomID + "\" does not exist");

                            ShowRoomDialog();
                        }
                    }
                })
                .setCancelable(false)
                .create().show();
    }

    private void ShowPasswordDialog(final Room room)
    {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.inputtextdialog, null);
        final EditText input = dialogView.findViewById(R.id.editText);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Password");

        new AlertDialog.Builder(this)
                .setTitle("Introduce the Password")
                .setView(dialogView)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        String password = input.getText().toString();

                        if(password.equals(room.password))
                        {
                            ShowToast("Password correct!\nJoined " + "\"" + room.name + "\"");

                            //Join the room
                            Intent intent = new Intent(RoomListActivity.this, PollListActivity.class);
                            intent.putExtra("roomName", room.name);
                            startActivity(intent);
                        }
                        else
                        {
                            ShowToast("Incorrect password");
                            ShowPasswordDialog(room);
                        }
                    }
                })
                .setCancelable(false)
                .create().show();
    }
}

