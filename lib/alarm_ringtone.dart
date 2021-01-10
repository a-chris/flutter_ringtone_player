class AlarmRingtone {
  final String name;
  final String uri;

  AlarmRingtone(this.name, this.uri);

  Map<String, dynamic> toMap() => {
        'name': name,
        'uri': uri,
      };

  static AlarmRingtone fromMap(Map<String, dynamic> map) => AlarmRingtone(map['name'], map['uri']);
}
