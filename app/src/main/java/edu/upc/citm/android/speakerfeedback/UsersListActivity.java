package edu.upc.citm.android.speakerfeedback;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class UsersListActivity extends AppCompatActivity {

    private List<UserInfo> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
    }

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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }
    }
}
