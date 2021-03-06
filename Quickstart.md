---
title: Quick Start - Similar User Engine Template
---

## Overview

This engine template recommends users that are "similar" to other users.
Similarity is not defined by the user's attributes but by the user's previous actions. By default, it uses the 'view' action such that user A and B are considered similar if most users who view A also view B.

This template is ideal for recommending users to other users based on their recent actions.
Use the IDs of the recently viewed users of a customer as the *Query*,
the engine will predict other users that this customer may also like.

This approach works perfectly for customers who are **first-time visitors** or have not signed in.
Recommendations are made dynamically in *real-time* based on the most recent user preference you provide in the *Query*.
You can, therefore, recommend users to visitors without knowing a long history about them.

You can also use this template to build the popular feature of Facebook: **"People you may know"** quickly.
Help your customers find more users by providing them suggestions of users similar to them.

## Usage

### Event Data Requirements

By default, this template takes the following data from Event Server as Training Data:

- User *$set* events
- User *view* User events

### Input Query

- List of UserIDs, which are the targeted users
- N (number of users to be recommended)
- List of white-listed UserIds (optional)
- List of black-listed UserIds (optional)

The template also supports black-list and white-list. If a white-list is provided, the engine will include only those users in its recommendation.
Likewise, if a black-list is provided, the engine will exclude those users in its recommendation.

### Output PredictedResult

- a ranked list of recommended userIDs

## 1. Install and Run PredictionIO

<%= partial 'shared/quickstart/install' %>

## 2. Create a new Engine from an Engine Template

<%= partial 'shared/quickstart/create_engine', locals: { engine_name: 'MySimilarUser', template_name: 'Similar User Engine Template', template_repo: 'template-scala-parallel-similaruser' } %>

## 3. Generate an App ID and Access Key

<%= partial 'shared/quickstart/create_app' %>

## 4a. Collecting Data

Next, let's collect some training data for the app of this Engine. By default,
the Similar User Engine Template supports entity: **user** 
and event **view**. A user can view another user.

<%= partial 'shared/quickstart/install_sdk' %>

The following is sample code of using different SDKs to import events of setting users, and user-view-user events:

<div class="tabs">
  <div data-tab="Python SDK" data-lang="python">
```python
import predictionio

client = predictionio.EventClient(
  access_key=<ACCESS KEY>,
  url=<URL OF EVENTSERVER>,
  threads=5,
  qsize=500
)

# Create a new user

client.create_event(
  event="$set",
  entity_type="user",
  entity_id=<USER_ID>
)

# A user views another user

client.create_event(
  event="view",
  entity_type="user",
  entity_id=<USER ID>,
  target_entity_type="user",
  target_entity_id=<VIEWED USER ID>
)
```
  </div>
  <div data-tab="PHP SDK" data-lang="php">

```php
<?php
require_once("vendor/autoload.php");
use predictionio\EventClient;

$client = new EventClient(<ACCESS KEY>, <URL OF EVENTSERVER>);

// Create a new user
$client->createEvent(array(
  'event' => '$set',
  'entityType' => 'user',
  'entityId' => <USER ID>
));

// A user views another user
$client->createEvent(array(
   'event' => 'view',
   'entityType' => 'user',
   'entityId' => <USER ID>,
   'targetEntityType' => 'user',
   'targetEntityId' => <VIEWED USER ID>
));

?>
```
  </div>
  <div data-tab="Ruby SDK" data-lang="ruby">

```ruby
# Create a client object.
client = PredictionIO::EventClient.new(<ACCESS KEY>, <URL OF EVENTSERVER>)

# Create a new user
client.create_event(
  '$set',
  'user',
  <USER ID>
)

# A user views another user.
client.create_event(
  'view',
  'user',
  <USER ID>, {
    'targetEntityType' => 'viwedUser',
    'targetEntityId' => <VIEWED USER ID>
  }
)

```
  </div>
  <div data-tab="Java SDK" data-lang="java">

```java
import io.prediction.Event;
import io.prediction.EventClient;

import com.google.common.collect.ImmutableList;

EventClient client = new EventClient(<ACCESS KEY>, <URL OF EVENTSERVER>);

// Create a new user
Event userEvent = new Event()
  .event("$set")
  .entityType("user")
  .entityId(<USER_ID>);
client.createEvent(userEvent);

// A user views another user
Event viewEvent = new Event()
    .event("view")
    .entityType("user")
    .entityId(<USER_ID>)
    .targetEntityType("user")
    .targetEntityId(<VIEWED_USER_ID>);
client.createEvent(viewEvent);

```
  </div>
  <div data-tab="REST API" data-lang="json">
```
# Create a new user

curl -i -X POST <URL OF EVENTSERVER>/events.json?accessKey=<ACCESS KEY> \
-H "Content-Type: application/json" \
-d '{
  "event" : "$set",
  "entityType" : "user"
  "entityId" : <USER ID>,
  "eventTime" : <TIME OF THIS EVENT>
}'

# A user views another user

curl -i -X POST <URL OF EVENTSERVER>/events.json?accessKey=<ACCESS KEY> \
-H "Content-Type: application/json" \
-d '{
  "event" : "view",
  "entityType" : "user"
  "entityId" : <USER ID>,
  "targetEntityType" : "user",
  "targetEntityId" : <VIEWED USER ID>,
  "eventTime" : <TIME OF THIS EVENT>
}'

```
  </div>
</div>

The properties of the `user` can be set, unset, or delete by special events **$set**, **$unset** and **$delete**. Please refer to [Event API](/datacollection/eventapi/#note-about-properties) for more details of using these events.

## 4b. Import Sample Data

<%= partial 'shared/quickstart/import_sample_data' %>

A Python import script `import_eventserver.py` is provided to import sample data. It imports 50 users (with user ID "u1" to "u50").
Each user then randomly views 10 other users.

NOTE: You need to install Python SDK to run the import script. Please follow [Python SDK README](https://github.com/PredictionIO/PredictionIO-Python-SDK) to install.

First, make sure you are under the `MySimilarUser` directory. Execute the following to import the data (Replace the value of access_key 
parameter with your **Access Key**):

```
$ cd MySimilarUser
$ python data/import_eventserver.py --access_key 3mZWDzci2D5YsqAnqNnXH9SB6Rg3dsTBs8iHkK6X2i54IQsIZI1eEeQQyMfs7b3F
```

You should see the following output:

```
...
User u50 views User u6
User u50 views User u13
User u50 views User u5
User u50 views User u44
User u50 views User u33
User u50 views User u30
550 events are imported.
```

WARNING: If you see error **TypeError: __init__() got an unexpected keyword argument 'access_key'**,
please update the Python SDK to the latest version.

## 5. Deploy the Engine as a Service

<%= partial 'shared/quickstart/deploy_enginejson', locals: { engine_name: 'MySimilarUser' } %>

<%= partial 'shared/quickstart/deploy', locals: { engine_name: 'MySimilarUser' } %>

## 6. Use the Engine

Now, You can retrieve predicted results. To retrieve 4 users which are similar to user ID "u1".
You send this JSON `{ "users": ["u1"], "num": 4 }` to the deployed engine and it will return a JSON of the recommended users. Simply send
 a query by making a HTTP request or through the `EngineClient` of an SDK:

<div class="tabs">
  <div data-tab="Python SDK" data-lang="python">
```python
import predictionio
engine_client = predictionio.EngineClient(url="http://localhost:8000")
print engine_client.send_query({"users": ["u1"], "num": 4})
```
  </div>
  <div data-tab="PHP SDK" data-lang="php">
```php
<?php
require_once("vendor/autoload.php");
use predictionio\EngineClient;

$client = new EngineClient('http://localhost:8000');

$response = $client->sendQuery(array('users'=> array('i1'), 'num'=> 4));
print_r($response);

?>
```
  </div>
  <div data-tab="Ruby SDK" data-lang="ruby">

```ruby
# Create client object.
client = PredictionIO::EngineClient.new('http://localhost:8000')

# Query PredictionIO.
response = client.send_query('users' => ['i1'], 'num' => 4)

puts response
```
  </div>
  <div data-tab="Java SDK" data-lang="java">

```java
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import io.prediction.EngineClient;

// create client object
EngineClient engineClient = new EngineClient("http://localhost:8000");

// query

JsonObject response = engineClient.sendQuery(ImmutableMap.<String, Object>of(
        "users", ImmutableList.of("u1"),
        "num",  4
    ));
```
  </div>
  <div data-tab="REST API" data-lang="json">
```
$ curl -H "Content-Type: application/json" \
-d '{ "users": ["u1"], "num": 4 }' \
http://localhost:8000/queries.json

```
  </div>
</div>

The following is a sample JSON response:

```
{
  "userScores":[
    {"user":"u43","score":0.7071067811865475},
    {"user":"u21","score":0.7071067811865475},
    {"user":"u46","score":0.5773502691896258},
    {"user":"u8","score":0.5773502691896258}
  ]
}
```

*MySimilarUser* is now running.

<%= partial 'shared/quickstart/production' %>


## Advanced Query

### Recommend users which are similar to multiple users:

```
curl -H "Content-Type: application/json" \
-d '{ "users": ["u1", "u3"], "num": 10}' \
http://localhost:8000/queries.json

{"userScores":[{"user":"u12","score":1.1700499715209998},{"user":"u21","score":1.1153550716504106},{"user":"u43","score":1.1153550716504106},{"user":"u14","score":1.0773502691896257},{"user":"u39","score":1.0773502691896257},{"user":"u26","score":1.0773502691896257},{"user":"u44","score":1.0773502691896257},{"user":"u38","score":0.9553418012614798},{"user":"u36","score":0.9106836025229592},{"user":"u46","score":0.9106836025229592}]}
```

In addition, the Query support the following optional parameters `whiteList` and `blackList`.

### Recommend users in the whiteList:

```
curl -H "Content-Type: application/json" \
-d '{
  "users": ["u1", "u3"],
  "num": 10,
  "whiteList": ["u21", "u26", "u40"]
}' \
http://localhost:8000/queries.json

{"userScores":[{"user":"u21","score":1.1153550716504106},{"user":"u26","score":1.0773502691896257}]}
```

### Recommend users not in the blackList:

```
curl -H "Content-Type: application/json" \
-d '{
  "users": ["u1", "u3"],
  "num": 10,
  "blackList": ["u21", "u26", "u40"]
}' \
http://localhost:8000/queries.json

{"userScores":[{"user":"u39","score":1.0773502691896257},{"user":"u44","score":1.0773502691896257},{"user":"u14","score":1.0773502691896257},{"user":"u45","score":0.7886751345948129},{"user":"u47","score":0.7618016810571367},{"user":"u6","score":0.7618016810571367},{"user":"u28","score":0.7618016810571367},{"user":"u9","score":0.7618016810571367},{"user":"u29","score":0.6220084679281463},{"user":"u30","score":0.5386751345948129}]}
```

#### [Next: DASE Components Explained](/templates/similaruser/dase/)