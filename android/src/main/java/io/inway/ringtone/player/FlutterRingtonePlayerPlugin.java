package io.inway.ringtone.player;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FlutterRingtonePlayerPlugin
 */
public class FlutterRingtonePlayerPlugin implements MethodCallHandler {
    private final Context context;

    public FlutterRingtonePlayerPlugin(Context context) {
        this.context = context;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_ringtone_player");
        channel.setMethodCallHandler(new FlutterRingtonePlayerPlugin(registrar.context()));
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        try {
            final String methodName = call.method;

            if (methodName.equals("play")) {
                if (!isServiceRunning()) {
                    final RingtoneMeta meta = createRingtoneMeta(call);
                    startRingtone(meta);
                }
                result.success(null);
            } else if (methodName.equals("stop")) {
                stopRingtone();
                result.success(null);
            } else if (methodName.equals("getAlarmRingtonesList")) {
                ArrayList<AlarmRingtone> ringtones = getAlarmRingtonesList();

                List<Object> ringtonesList = new ArrayList<>();
                for (AlarmRingtone ringtone : ringtones) {
                    ringtonesList.add(ringtone.toMap());
                }
                result.success(ringtonesList);
            }
        } catch (Exception e) {
            result.error("Exception", e.getMessage(), null);
        }
    }

    private RingtoneMeta createRingtoneMeta(MethodCall call) {
        if (!call.hasArgument("android")) {
            throw new IllegalArgumentException("android argument is missing");
        }

        final RingtoneMeta meta = new RingtoneMeta();
        meta.setKind(getMethodCallArgument(call, "android", Integer.class));
        meta.setLooping(getMethodCallArgument(call, "looping", Boolean.class));
        meta.setAsAlarm(getMethodCallArgument(call, "asAlarm", Boolean.class));
        meta.setRingtoneUri(getMethodCallArgument(call, "ringtoneUri", String.class));
        final Double volume = getMethodCallArgument(call, "volume", Double.class);
        if (volume != null) {
            meta.setVolume(volume.floatValue());
        }

        if (meta.getAsAlarm()) {
            final String alarmNotificationMetaKey = "alarmNotificationMeta";

            if (call.hasArgument(alarmNotificationMetaKey)) {
                final Map<String, Object> notificationMetaValues = getMethodCallArgument(call, alarmNotificationMetaKey, Map.class);
                final AlarmNotificationMeta notificationMeta = new AlarmNotificationMeta(notificationMetaValues);
                meta.setAlarmNotificationMeta(notificationMeta);
            } else {
                throw new IllegalArgumentException("if asAlarm=true you have to deliver '" + alarmNotificationMetaKey + "'");
            }
        }

        return meta;
    }

    private ArrayList<AlarmRingtone> getAlarmRingtonesList() {
        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();

        ArrayList<AlarmRingtone> ringtones = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String uri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

            ringtones.add(new AlarmRingtone(name, uri + "/" + id));
        }

        return ringtones;
    }

    private void startRingtone(RingtoneMeta meta) {
        final Intent intent = createServiceIntent();
        intent.putExtra(FlutterRingtonePlayerService.RINGTONE_META_INTENT_EXTRA_KEY, meta);

        if (meta.getAsAlarm()) {
            ContextCompat.startForegroundService(context, intent);
        } else {
            context.startService(intent);
        }
    }

    private void stopRingtone() {
        final Intent intent = createServiceIntent();
        context.stopService(intent);
    }

    private <ArgumentType> ArgumentType getMethodCallArgument(MethodCall call, String key, Class<ArgumentType> argumentTypeClass) {
        return call.argument(key);
    }

    private Intent createServiceIntent() {
        return new Intent(context, FlutterRingtonePlayerService.class);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FlutterRingtonePlayerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
