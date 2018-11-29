package oriol.jonathan.speakerfeedback;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.upc.citm.android.speakerfeedback.R;

public class UsersListActivity extends AppCompatActivity {

    private UserAdapter adapter;
    private RecyclerView usersRView;

    private List<UserInfo> userList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        usersRView = findViewById(R.id.usersRView);

        userList = new ArrayList<UserInfo>();

        usersRView.setLayoutManager(new GridLayoutManager(this,3));

        adapter = new UserAdapter();
        usersRView.setAdapter(adapter);

        String roomId = getIntent().getStringExtra("roomId");
        db.collection("users").whereEqualTo("room", roomId).addSnapshotListener(this, usersListener);
    }

    private EventListener<QuerySnapshot> usersListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
            userList.clear();
            for (DocumentSnapshot doc : documentSnapshots) {
                String name = "<unknown name>";
                if (doc.contains("name")) {
                    name = doc.getString("name");
                }
                userList.add(new UserInfo(name));
            }
            adapter.notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView userInfo;

        public ViewHolder(View itemView)
        {
            super(itemView);
            this.userInfo = itemView.findViewById(R.id.userInfo);
        }
    }

    class UserAdapter extends RecyclerView.Adapter<ViewHolder>
    {
        @Override public int getItemCount(){return userList.size();}

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View itemView = getLayoutInflater().inflate(R.layout.userinfo, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            UserInfo info = userList.get(position);
            holder.userInfo.setText(info.user);
        }
    }
}
