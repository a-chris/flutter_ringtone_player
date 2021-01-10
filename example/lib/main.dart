import 'package:flutter/material.dart';
import 'package:flutter_ringtone_player/alarm_notification_meta.dart';
import 'package:flutter_ringtone_player/alarm_ringtone.dart';
import 'package:flutter_ringtone_player/flutter_ringtone_player.dart';

final notificationMeta = AlarmNotificationMeta(
    'io.inway.ringtone.player_example.MainActivity', 'ic_alarm_notification',
    contentTitle: 'Alarm', contentText: 'Alarm is active', subText: 'Subtext');

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Ringtone player'),
        ),
        body: Home(),
      ),
    );
  }
}

class Home extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        children: <Widget>[
          Padding(
            padding: EdgeInsets.all(8),
            child: RaisedButton(
              child: const Text('playAlarm'),
              onPressed: () {
                FlutterRingtonePlayer.playAlarm(alarmNotificationMeta: notificationMeta);
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(8),
            child: RaisedButton(
              child: const Text('playAlarm with custom ringtone'),
              onPressed: () async {
                final ringtonesList = await FlutterRingtonePlayer.getAlarmRingtonesList();
                final ringtonesWidgets = ringtonesList
                    .map((ringtone) => SimpleDialogOption(
                          onPressed: () => Navigator.pop(context, ringtone),
                          child: Text(ringtone.name),
                        ))
                    .toList();
                final ringtone = await showDialog<AlarmRingtone>(
                    context: context,
                    builder: (BuildContext context) {
                      return SimpleDialog(
                        title: const Text('Choose the alarm ringtone'),
                        children: ringtonesWidgets,
                      );
                    });
                FlutterRingtonePlayer.playAlarm(
                    ringtoneUri: ringtone.uri, alarmNotificationMeta: notificationMeta);
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(8),
            child: RaisedButton(
              child: const Text('playAlarm asAlarm: false'),
              onPressed: () {
                FlutterRingtonePlayer.playAlarm(asAlarm: false);
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(8),
            child: RaisedButton(
              child: const Text('playNotification'),
              onPressed: () {
                FlutterRingtonePlayer.playNotification();
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(8),
            child: RaisedButton(
              child: const Text('playRingtone'),
              onPressed: () {
                FlutterRingtonePlayer.playRingtone();
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(8),
            child: RaisedButton(
              child: const Text('play'),
              onPressed: () {
                FlutterRingtonePlayer.play(
                  android: AndroidSounds.notification,
                  ios: IosSounds.glass,
                  looping: true,
                  volume: 1.0,
                );
              },
            ),
          ),
          Padding(
            padding: EdgeInsets.all(8),
            child: RaisedButton(
              child: const Text('stop'),
              onPressed: () {
                FlutterRingtonePlayer.stop();
              },
            ),
          ),
        ],
      ),
    );
  }
}
