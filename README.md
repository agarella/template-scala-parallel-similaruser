# Similar Product Template

## Documentation

Please refer to http://docs.prediction.io/templates/similaruser/quickstart/

## Versions

### v0.1.0

- initial version

## Development Notes

### import sample data

```
$ python data/import_eventserver.py --access_key <your_access_key>
```

### sample query

normal:

```
curl -H "Content-Type: application/json" \
-d '{ "userItems": ["i1", "i3", "i10", "i2", "i5", "i31", "i9"], "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "userItems": ["i1", "i3", "i10", "i2", "i5", "i31", "i9"],
  "num": 10,
  "categories" : ["c4", "c3"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "userItems": ["i1", "i3", "i10", "i2", "i5", "i31", "i9"],
  "num": 10,
  "whiteList": ["i21", "i26", "i40"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "userItems": ["i1", "i3", "i10", "i2", "i5", "i31", "i9"],
  "num": 10,
  "blackList": ["i21", "i26", "i40"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

unknown userItem:

```
curl -H "Content-Type: application/json" \
-d '{ "userItems": ["unk1", "i3", "i10", "i2", "i5", "i31", "i9"], "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```


all unknown userItems:

```
curl -H "Content-Type: application/json" \
-d '{ "userItems": ["unk1", "unk2", "unk3", "unk4"], "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```
