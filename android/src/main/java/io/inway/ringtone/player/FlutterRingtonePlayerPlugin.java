package io.inway.ringtone.player;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
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
public class FlutterRingtonePlayerPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private Context context;

    /**
     * Old plugin registration.
     */
    @SuppressWarnings("deprecation")
    public static void registerWith(Registrar registrar) {
        final FlutterRingtonePlayerPlugin plugin = new FlutterRingtonePlayerPlugin();
        plugin.onAttachedToEngine(registrar.context(), registrar.messenger());

    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger());
    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
        context = applicationContext;
        channel = new MethodChannel(messenger, "flutter_ringtone_player");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        context = null;
        channel.setMethodCallHandler(null);
        channel = null;
    }

    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        try {
            final String methodName = call.method;

            switch (methodName) {
                case "play":
                    if (!isServiceRunning()) {
                        final RingtoneMeta meta = createRingtoneMeta(call);
                        startRingtone(meta);
                    }
                    result.success(null);
                    break;
                case "stop":
                    stopRingtone();
                    result.success(null);
                    break;
                case "getAlarmRingtonesList":
                    ArrayList<AlarmRingtone> ringtones = getAlarmRingtonesList();

                    List<Object> ringtonesList = new ArrayList<>();
                    for (AlarmRingtone ringtone : ringtones) {
                        ringtonesList.add(ringtone.toMap());
                    }
                    result.success(ringtonesList);
                    break;
                case "getDefaultAlarmRingtone":
                    AlarmRingtone defaultAlarmRingtone = getDefaultAlarmRingtone();
                    result.success(defaultAlarmRingtone.toMap());
                    break;
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

    private AlarmRingtone getDefaultAlarmRingtone() {
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        String uriString = uri.toString().substring(0, uri.toString().indexOf("?"));
        return new AlarmRingtone(ringtone.getTitle(context), uriString);
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
