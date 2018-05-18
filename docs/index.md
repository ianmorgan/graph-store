## About

This is a basic event store that should be sufficient for many event sourcing scenarios. Events are stored and retrieved 
using a simple REST style API. It will also support a simple subscription style push model,
 when I get around to it.
 
## Event Types 
 
### SimpleEvent  
The most basic event is as follows:

```json 
{
  "id" : "4778a3ef-a920-4323-bc34-b87aa0bffb41",
  "type" : "SimpleEvent",
  "timestamp": 1509618174218,
  "creator": "test"
}
```


* __id__ - A random (type 3) UUID
* __type__ - A unique name for the type of event. By convention in Java class name format. Limited to 255 chars
* __timestamp__ - Unix style timestamp indicating the time the event was created. This is roughly analogous to the common use of update/createdTimestamp columns on database tables. _Don't rely upon this for ordering, as it set by the application at the time of creating the event and therefore will not necessarily be well ordered_.   
* __creator__ - The name / indentifier of the user or system that created the event, e.g. 'john.smith@example.com'. Limited to 255 chars 

Normally additional fields are added. The following can be combined as necessary.

### PayloadEvent 

This includes the 'payload' key, which holds any additional data captured with the event. 

```json
{
  "id" : "bed6a10c-ab5a-48bc-9129-60842fe10fd9",
  "type" : "PayloadEvent",
  "timestamp": 1509618174218,
  "creator": "test",
  "payload" : {
    "some": "data",
    "array": [
      1,
      2,
      3
    ],
    "can be nested" : { "more" : "data"},
    "message": "payload should be no more less than 10K minified JSON"
  }
}
```

The content of the 'payload' is entirely application specific but it should not exceed 10k (this is actually a 
soft limit, the service will start to log warnings if exceeded). The hard limit is around 18K, and is determined by column size 
limitations in MySQL.

### AggregateEvent 

Often it is necessary to easily find the events for a particular entity or aggregate (using [DDD](https://martinfowler.com/bliki/DDD_Aggregate.html) 
terminology). This is done by adding an 'aggregateId' key.

```json
{
  "id" : "db857426-4be7-4c1a-99df-10b2ed13dd02",
  "type" : "AggregateEvent",
  "timestamp": 1509618174218,
  "creator": "test",
  "aggregateId" : "123"
}
``` 

The 'aggregateId' can be any string up to 255 characters 

### SessionEvent 

This allows a set of events to be related some underlying session or transaction, which is typically useful 
 when investigating problems or attempting to recreate the original user input. Note that this is NOT the same as 
batching saves (see below).

```json
{
  "id" : "08ec6bfa-b167-43f3-bd26-f2498fa2e291",
  "type" : "SessionEvent",
  "timestamp": 1509618174218,
  "creator": "test",
  "sessionId" : "session#564ghsdgd5bncfz"
}
```

The 'sessionId' can be any string up to 255 characters.


## Storing and Retrieving Events 

### Saving Events 

Simply POST a JSON array containing one or more events. All the events are committed as a single batch (so if there 
is a problem with single one of them, the entire collection is rejected)

```bash
curl -H "Content-Type: application/json" -X POST -d '[{ "id" : "4778a3ef-a920-4323-bc34-b87aa0bffb41", "type" : "SimpleEvent", "timestamp": 1509618174218,"creator": "test"}]' http://localhost:7001/events
```

Success returns a 200 status code.

Any failure returns a 500 status code.

_TODO_ expand on use of status codes and error messages.

### Retrieving events 

Call GET /events with the following additional parameters.

```bash
curl -H "Content-Type: application/json" -X GET  http://localhost:7001/events
```

The return is a JSON array of the events under the 'payload' key. (_TODO_ expand on use of standard keys in the response)

```json
{
  "payload": {
    "events": [
      {
        "id": "4778a3ef-a920-4323-bc34-b87aa0bffb41",
        "type": "SessionEvent",
        "timestamp": 1509618174218,
        "creator": "test",
        "sessionId": "session#564ghsdgd5bncfz"
      }
    ]
  }
}
```

Supported parameters are:

* __pageSize__ - Set a limit on the number of events to return. If this is specified the 'paging' key is also included in the payload - see below
* __lastId__ - Typically combined with the 'pageSize' to retrieve from a position within the event stream. Note that this exclusive, i.e. the query will return the matching events directly after this event id
* __type__ - Comma separated list of event types (the 'type' key) to filter on
* __aggregateId__ - Comma separated list of aggregateIds  to filter on 
* __sessionId__ - Comma separated list of sessionIds to filter on 


The additional 'paging' key under the 'payload' holds information useful for constructing the next query 

```json
{
  "more" : true,
  "size" : 10,
  "lastId" : "4778a3ef-a920-4323-bc34-b87aa0bffb41"
}
```

* __more__ - true indicates that a subsequent query should be made as there _maybe_ more data
* __size__ - the number of events returned (the same as the array size)
* __lastId__ - the id of the last event returned (the last event in the array)





