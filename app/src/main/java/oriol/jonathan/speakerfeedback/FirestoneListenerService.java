package oriol.jonathan.speakerfeedback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import edu.upc.citm.android.speakerfeedback.R;

public class FirestoneListenerService extends Service {

    private String roomName;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SpeakerFeedback","FirestoneListenerService.onCreate");

        PollListActivity.roomRef.collection("polls").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("SpeakerFeedback", "Error accessing polls");
                    return;
                }

                int i = 2;
                for (DocumentSnapshot doc : documentSnapshots) {
                    Poll poll = doc.toObject(Poll.class);

                    if (poll.isOpen() == false)
                        continue;

                    Log.d("SpeakerFeedback", poll.toString());
                    createPollModifiedNotification(poll, i);
                    i++;
                }

            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        roomName = intent.getStringExtra("room");

        Log.i("SpeakerFeedback","FirestoneListenerService.onStartCommand");
        createForegrowndNotification();

        return START_NOT_STICKY;
    }

    private void createForegrowndNotification() {

        Intent intent = new Intent(this,PollListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(String.format("Connectat a '" + roomName + "'"))
                .setSmallIcon(R.drawable.ic_message_black_24dp)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
    }

    private void createPollModifiedNotification(Poll poll, int id) {
        Intent intent = new Intent(this, PollListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(String.format(poll.getQuestion()))
                .setSmallIcon(R.drawable.ic_message_black_24dp)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .build();

        NotificationManagerCompat notificationmanagerCompat = NotificationManagerCompat.from(this);
        notificationmanagerCompat.notify(id, notification);
    }

    @Override
    public void onDestroy() {
        Log.i("SpeakerFeedback","FirestoneListenerService.onDestroy");

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancelAll();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
