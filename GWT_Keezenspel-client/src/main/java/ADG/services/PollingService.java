package ADG.services;

import com.google.gwt.user.client.Timer;

public class PollingService {
    private Timer timer;

    public void startPolling(int intervalMillis, Runnable action) {
        /*
        only use the Runnable action when there are no input parameters needed or return value expected
        */
        if (timer == null) {
            timer = new Timer() {
                @Override
                public void run() {
                    action.run();
                }
            };
            timer.scheduleRepeating(intervalMillis);
        }
    }

    public void stopPolling() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
