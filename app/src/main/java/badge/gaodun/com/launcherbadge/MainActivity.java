package badge.gaodun.com.launcherbadge;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private int count = 0;
    private Handler handler = new Handler();
    private Runnable runnable = new MyRunnable();

    public class MyRunnable implements Runnable {
        @Override
        public void run() {
            handler.postDelayed(runnable, 10000);

            count++;
            // 发送未读消息数目广播
            BadgeUtil.setBadgeCount(getApplicationContext(), count, R.mipmap.ic_launcher_round);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handler == null) {
                    handler = new Handler();
                }
                if (runnable == null) {
                    runnable = new MyRunnable();
                }
                handler.post(runnable);
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;

                if (handler != null) {
                    // 发送重置/清除未读消息数目广播
                    BadgeUtil.resetBadgeCount(getApplicationContext(), R.mipmap.ic_launcher);
                    handler.removeCallbacksAndMessages(null);
                    runnable = null;
                    handler = null;
                }
            }
        });
    }
}
