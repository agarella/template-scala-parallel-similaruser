"""
Import sample data for similar product engine
"""

import predictionio
import argparse
import random

SEED = 3

def import_events(client):
  random.seed(SEED)
  count = 0
  print client.get_status()
  print "Importing data..."

  # generate 10 users, with user ids u1,u2,....,u10
  user_ids = ["u%s" % i for i in range(1, 11)]
  for user_id in user_ids:
    print "Set user", user_id
    client.create_event(
      event="$set",
      entity_type="user",
      entity_id=user_id
    )
    count += 1

  # generate 50 similarUsers, with similarUser ids i1,i2,....,i50
  similar_user_ids = ["i%s" % i for i in range(1, 51)]
  for similar_user_id in similar_user_ids:
    print "Set similarUser", similar_user_id
    client.create_event(
      event="$set",
      entity_type="similarUser",
      entity_id=similar_user_id
    )
    count += 1

  # each user randomly viewed 10 similarUsers
  for user_id in user_ids:
    for viewed_user in random.sample(similar_user_ids, 10):
      print "User", user_id ,"views similarUser", viewed_user
      client.create_event(
        event="view",
        entity_type="user",
        entity_id=user_id,
        target_entity_type="similarUser",
        target_entity_id=viewed_user
      )
      count += 1

  print "%s events are imported." % count

if __name__ == '__main__':
  parser = argparse.ArgumentParser(
    description="Import sample data for similar product engine")
  parser.add_argument('--access_key', default='invald_access_key')
  parser.add_argument('--url', default="http://localhost:7070")

  args = parser.parse_args()
  print args

  client = predictionio.EventClient(
    access_key=args.access_key,
    url=args.url,
    threads=5,
    qsize=500)
  import_events(client)
