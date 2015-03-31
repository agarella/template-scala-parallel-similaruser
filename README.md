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
-d '{ "similarUsers": ["su1", "su3", "su10", "su2", "su5", "su31", "su9"], "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "similarUsers": ["su1", "su3", "su10", "su2", "su5", "su31", "su9"],
  "num": 10
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "similarUsers": ["su1", "su3", "su10", "su2", "su5", "su31", "su9"],
  "num": 10,
  "whiteList": ["su21", "su26", "su40"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

```
curl -H "Content-Type: application/json" \
-d '{
  "similarUsers": ["su1", "su3", "su10", "su2", "su5", "su31", "su9"],
  "num": 10,
  "blackList": ["su21", "su26", "su40"]
}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```

unknown similarUser:

```
curl -H "Content-Type: application/json" \
-d '{ "similarUsers": ["unk1", "su3", "su10", "su2", "su5", "su31", "su9"], "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```


all unknown similarUsers:

```
curl -H "Content-Type: application/json" \
-d '{ "similarUsers": ["unk1", "unk2", "unk3", "unk4"], "num": 10}' \
http://localhost:8000/queries.json \
-w %{time_connect}:%{time_starttransfer}:%{time_total}
```
