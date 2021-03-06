package oriol.jonathan.speakerfeedback;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.upc.citm.android.speakerfeedback.R;

public class PollListActivity extends AppCompatActivity {

    //Variables

    public static final int REGISTER_USER = 0;

    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static DocumentReference roomRef;

    private TextView userCountView;
    private RecyclerView polls_view;
    private Adapter adapter;
    private String userId;
    private String roomName = "testroom";
    private List<Poll> polls = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    ListenerRegistration votesRegistration;

    private Map<String, Poll> pollsMap = new HashMap<>();

    private boolean activePoll = false;

    private static final int MAX_OPTIONS = 10;
    private static final int option_view_ids[] = { R.id.option1View, R.id.option2View, R.id.option3View, R.id.option4View, R.id.option5View };
    private static final int bar_view_ids[]    = { R.id.bar1View, R.id.bar2View, R.id.bar3View, R.id.bar4View, R.id.bar5View };
    private static final int count_view_ids[]  = { R.id.awnser1View, R.id.awnser2View, R.id.awnser3View, R.id.awnser4View, R.id.awnser5View };

    //Classes

    class ViewHolder extends RecyclerView.ViewHolder {

        private CardView card_view;
        private TextView label_view;
        private TextView question_view;
        private TextView[] option_views;
        private View[] bar_views;
        private TextView[] count_views;

        ViewHolder(View itemView) {
            super(itemView);
            card_view     = itemView.findViewById(R.id.cardView);
            label_view    = itemView.findViewById(R.id.labelView);
            question_view = itemView.findViewById(R.id.questionView);

            option_views = new TextView[MAX_OPTIONS];
            for (int i = 0; i < option_view_ids.length; i++) {
                option_views[i] = itemView.findViewById(option_view_ids[i]);
            }
            bar_views = new View[MAX_OPTIONS];
            for (int i = 0; i < bar_view_ids.length; i++) {
                bar_views[i] = itemView.findViewById(bar_view_ids[i]);
            }
            count_views = new TextView[MAX_OPTIONS];
            for (int i = 0; i < count_view_ids.length; i++) {
                count_views[i] = itemView.findViewById(count_view_ids[i]);
            }

            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    int index = getAdapterPosition();
                    onPollClick(index);
                }
            });
        }

        void setOptionVisibility(int i, int visibility) {
            option_views[i].setVisibility(visibility);
            bar_views[i].setVisibility(visibility);
            count_views[i].setVisibility(visibility);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.poll, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Poll poll = polls.get(position);
            if (position == 0) {
                holder.label_view.setVisibility(View.VISIBLE);
                if (poll.isOpen()) {
                    holder.label_view.setText("Active");
                } else {
                    holder.label_view.setText("Previous");
                }
            } else {
                if (!poll.isOpen() && polls.get(position-1).isOpen()) {
                    holder.label_view.setVisibility(View.VISIBLE);
                    holder.label_view.setText("Previous");
                } else {
                    holder.label_view.setVisibility(View.GONE);
                }
            }

            float elevation = poll.isOpen() ? 10.0f : 0.0f;
            int bg_color = getResources().getColor(poll.isOpen() ? android.R.color.white : R.color.cardview_dark_background);
            holder.card_view.setCardElevation(elevation);
            holder.card_view.setCardBackgroundColor(bg_color);

            int activeColor = Color.rgb(0, 0, 0);
            int passiveColor = Color.rgb(100, 100, 100);

            holder.question_view.setText(poll.getQuestion());
            holder.question_view.setTextColor(poll.isOpen() ? activeColor : passiveColor);

            List<String> options = poll.getOptions();
            for (int i = 0; i < option_view_ids.length; i++) {
                if (i < options.size()) {
                    holder.option_views[i].setText(options.get(i));
                    holder.option_views[i].setTextColor(poll.isOpen() ? activeColor : passiveColor);
                    holder.setOptionVisibility(i, View.VISIBLE);
                } else {
                    holder.setOptionVisibility(i, View.GONE);
                }
                holder.bar_views[i].setAlpha(poll.isOpen() ? 1.0f : 0.25f);
            }
            List<Integer> results = poll.getResults();
            for (int i = 0; i < options.size(); i++) {
                Integer res = null;
                if (results != null && i < results.size()) {
                    res = results.get(i);
                }
                ViewGroup.LayoutParams params = holder.bar_views[i].getLayoutParams();
                params.width = 4;
                int visibility = View.GONE;
                if (res != null) {
                    visibility = View.VISIBLE;
                    params.width += 16 * (int) res;
                    holder.count_views[i].setText(String.format("%d", results.get(i)));
                }
                holder.bar_views[i].setVisibility(visibility);
                holder.count_views[i].setVisibility(visibility);
                holder.bar_views[i].setLayoutParams(params);
            }
        }

        @Override
        public int getItemCount() {
            return polls.size();
        }
    }

    private void startFirestoreListenerService()
    {
        Intent intent = new Intent(this,FirestoneListenerService.class);
        intent.putExtra("room",roomName);
        startService(intent);
    }

    private void  stopFirestoreListenerService()
    {
        Intent intent = new Intent(this,FirestoneListenerService.class);
        stopService(intent);
    }

    //Listeners

    private EventListener<DocumentSnapshot> roomListener = new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error al rebre rooms/testroom", e);
                return;
            }
            if(!documentSnapshot.exists())
                return;

            String name = documentSnapshot.getString("name");
            setTitle(name);

            Room room = documentSnapshot.toObject(Room.class);
            if(room.open == false)
            {
                //This room has been closed by the Speaker, end the activity
                finish();
            }
        }
    };

    private EventListener<QuerySnapshot> userListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            if (e != null) {
                Log.e("SpeakerFeedback", "Error al rebre usuaris dins un room", e);
                return;
            }
            userCountView.setText("Users connected: " + Integer.toString(documentSnapshots.size()));
        }
    };

    private EventListener<QuerySnapshot> pollsListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

            if (e != null) {
                Log.e("SpeakerFeedback", "Error accessing polls");
                return;
            }

            polls.clear();
            activePoll = false;
            for (DocumentSnapshot doc : documentSnapshots)
            {
                Poll poll = doc.toObject(Poll.class);

                if(poll != null)
                {
                    polls.add(poll);
                    ids.add(doc.getId());
                    pollsMap.put(doc.getId(), poll);
                    if (poll.isOpen()) {
                        activePoll = true;
                    }
                }
            }
            Log.i("SpeakerFeedback", String.format("He carregat %d polls.", polls.size()));
            adapter.notifyDataSetChanged();
            if (activePoll) {
                addVotesListener();
            } else {
                removeVotesListener();
            }
        }
    };

    private EventListener<QuerySnapshot> votesListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

            if (e != null) {
                Log.e("SpeakerFeedback", "Error accessing votes");
                return;
            }

            // Reset votes for the open Poll
            for (Poll poll : polls) {
                if (poll.isOpen()) {
                    poll.resetVotes();
                }
            }

            // Accumulate votes
            for (DocumentSnapshot doc : documentSnapshots) {
                if (!doc.contains("pollid")) {
                    Log.e("SpeakerFeedback", "Vote is missing 'pollId'");
                    return;
                }

                String pollId = doc.getString("pollid");
                Long vote = doc.getLong("option");
                if (vote == null) {
                    Log.e("SpeakerFeedback", "Vote is missing 'option'");
                    return;
                }

                Poll poll = pollsMap.get(pollId);
                if (poll == null) {
                    Log.e("SpeakerFeedback", "Vote for non-existing poll");
                } else if (!poll.isOpen()) {
                    Log.e("SpeakerFeedback", "Vote for an already closed poll");
                } else {
                    poll.addVote((int)(long)vote);
                }
            }
            adapter.notifyDataSetChanged();
        }
    };

    //Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userCountView = findViewById(R.id.usersCountView);

        polls_view = findViewById(R.id.polls_view);
        polls_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        polls_view.setAdapter(adapter);

        polls = new ArrayList<Poll>();

        getOrRegisterUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
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
        }

        return true;
    }

    private void CloseApp()
    {
        //Save the opened room, in order to open it directly the next time the app is opened.
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        prefs.edit().putString("roomOpened", roomName).apply();

        stopFirestoreListenerService();

        Intent intent = new Intent(getApplicationContext(), RoomListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    @Override
    protected void onDestroy()
    {
        stopFirestoreListenerService();
        leaveRoom();
        super.onDestroy();
    }

    private void enterRoom()
    {
        db.collection("users").document(userId).update("room",roomName);
        startFirestoreListenerService();
    }

    private void leaveRoom()
    {
        db.collection("users").document(userId).update("room", "");
    }

    private void removeVotesListener()
    {
        if (votesRegistration != null) {

            votesRegistration.remove();
        }
    }

    private void addVotesListener()
    {
        votesRegistration = roomRef.collection("polls")
                .addSnapshotListener(this, votesListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String roomName2 = intent.getStringExtra("roomName");
        if(roomName2 == null)
            roomName2 = "testroom";

        roomName = roomName2;
        roomRef = db.collection("rooms").document(roomName);

        //SetUp listeners
        roomRef.addSnapshotListener(roomListener);
        db.collection("users").whereEqualTo("room", roomName)
                .addSnapshotListener(this, userListener);
        roomRef.collection("polls").orderBy("start", Query.Direction.DESCENDING)
                .addSnapshotListener(this, pollsListener);

        if(userId != null)
        {
            enterRoom();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private void createNewPoll(String question) {
        Poll poll = new Poll(question);
        roomRef.collection("polls").add(poll).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PollListActivity.this, "Couldn't add poll", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getOrRegisterUser() {
        // Busquem a les preferències de l'app l'ID de l'usuari per saber si ja s'havia registrat
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        userId = prefs.getString("userId", null);
        if (userId == null) {
            // Hem de registrar l'usuari, demanem el nom
            Intent intent = new Intent(this, RegisterUserActivity.class);
            startActivityForResult(intent, REGISTER_USER);
            Toast.makeText(this, "Encara t'has de registrar", Toast.LENGTH_SHORT).show();
        } else {
            // Ja està registrat, mostrem el id al Log
            Log.i("SpeakerFeedback", "userId = " + userId);
            db.collection("users").document(userId).update("last_active", new Date());
            prefs.edit().putBoolean("alreadyLogged", true).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_USER:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    registerUser(name);
                } else {
                    Toast.makeText(this, "Has de registrar un nom", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerUser(String name) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", name);
        fields.put("last_active", new Date());
        db.collection("users").add(fields).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // Toast.makeText(PollListActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                // textview.setText(documentReference.getId());
                userId = documentReference.getId();
                SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
                prefs.edit()
                        .putString("userId", userId)
                        .commit();
                Log.i("SpeakerFeedback", "New user: userId = " + userId);
                enterRoom();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SpeakerFeedback", "Error creant objecte", e);
                Toast.makeText(PollListActivity.this,
                        "No s'ha pogut registrar l'usuari, intenta-ho més tard", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void onStatusBarClick(View view)
    {
        Intent intent = new Intent(this, UsersListActivity.class);
        intent.putExtra("roomId", roomName);
        startActivity(intent);
    }

    public void onPollClick(final int index)
    {
        //If the poll is opened, open the question and the answers in order to vote.
        Poll poll = polls.get(index);
        if(poll.isOpen())
        {
            //Keep a copy list, in order to not add the 'Close Poll' option to the original list.
            final List<String> options = new ArrayList<>(poll.getOptions());
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
            for(String option : options)
            {
                arrayAdapter.add(option);
            }

            new AlertDialog.Builder(this)
                    .setTitle(poll.getQuestion())
                    .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int option)
                        {
                            votePoll(index, option);
                        }
                    })
                    .create().show();
        }
    }

    public void closePoll(int index)
    {
        /*String id = ids.get(index);
        Poll poll = polls.get(index);

        //Close the poll, to not let the user vote again
        poll.setOpen(false);

        //Assign the right poll to the document and Log results
        db.collection("rooms").document("testroom").collection("polls").document(id).set(poll)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("SpeakerFeedback", "Poll saved");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SpeakerFeedback", "Poll NOT saved", e);
            }
        });

        //Delete the listener in order to don't react again to the clicks, votes already closed.
        removeVotesListener();*/
    }

    public void votePoll(int index, int option)
    {
        Poll poll = polls.get(index);
        String id = ids.get(index);

        if(poll.lastVote != -1)
            poll.undoLastVote();

        poll.addVote(option);

        //Update the RecyclerView
        adapter.notifyDataSetChanged();

        //Send the votes to the database

        /*Map<String, Object> map = new HashMap<>();
        map.put("pollid", index);
        map.put("option", option);*/

        roomRef.collection("polls").document(id).set(poll)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("SpeakerFeedback", "Poll saved");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("SpeakerFeedback", "Poll NOT saved", e);
            }
        });

        //TODO: UPDATE THE POLLS. POLLS CLASS ARE BAD DEFINED.
        /*QuerySnapshot snapshot = roomRef.collection("polls").get().getResult();
        for(DocumentSnapshot doc : snapshot)
        {
            Poll oldPoll = doc.toObject(Poll.class);

            if(oldPoll.getQuestion().equals(poll.getQuestion()))
            {
                DocumentReference document = doc.getDocumentReference(doc.getId());
                document.set(poll);

                break;
            }
        }*/
    }
}
