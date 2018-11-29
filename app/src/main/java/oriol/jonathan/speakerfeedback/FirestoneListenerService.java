package oriol.jonathan.speakerfeedback;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import edu.upc.citm.android.speakerfeedback.R;

public class FirestoneListenerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SpeakerFeedback","FirestoneListenerService.onCreate");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SpeakerFeedback","FirestoneListenerService.onStartCommand");
        createForegrowndNotification();

        return START_NOT_STICKY;
    }

    private void createForegrowndNotification() {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle(String.format("Connectat a 'testroom'"))
                .setSmallIcon(R.drawable.ic_message_black_24dp)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);
    }

    @Override
    public void onDestroy() {
        Log.i("SpeakerFeedback","FirestoneListenerService.onDestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
