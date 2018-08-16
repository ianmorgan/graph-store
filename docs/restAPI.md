# REST API  

## Introduction 

All the types in the GraphQL schema are also available as  REST style API. So taking the 
basic [Star Wars](https://github.com/ianmorgan/graph-store/blob/master/src/schema/starwars.graphqls) schema, 
then Droid, Human and Character are all available via a REST API. Character is read-only as it is an Interface, 
whereas Droid and Human can be modified. As there is currently no support for mutations via the GraphQL API, the only 
ways to get data into the system is to either use the REST API or write directly to underlying event stream.

By convention internally these resources are referred to as 'docs' as they follow the same basic document metaphor
used by document centric databases such as MongoDB. 

Although the API has REST style URLs, the behaviour is closer to [JSON RPC](https://en.wikipedia.org/wiki/JSON-RPC), 
than full REST, with a very simple use of HTTP verbs and status codes. See [Standards](standards) for more detail. 

## Retrieving a Doc 

This is classic REST, e.g. 

[http://graphstore.app/docs/Droid/2001](http://graphstore.app/docs/Droid/2001)

or with curl:

```bash
curl -H "Accept: application/json" http://graphstore.app/docs/Droid/2001
```

## Updating a Doc 

The basic behavior is outlined below. Refer to [Updates and Validation](updatesAndValidations) for more detail.

Updates simply follow the basic REST form with data supplied in the body of the request as a JSON document. 

```bash
curl -H "Content-Type: application/json" -X POST  http://graphstore.app/docs/Droid/2001 -d '{  "name": "R2-D2","appearsIn": ["NEWHOPE","EMPIRE","JEDI"] }'
```

The service performs a basic schema check of the submitted JSON doc and confirms that structure and 
data types match the GraphQL schema, but it does not validate mandatory fields nor any relationship between 
types. 

To update, simply pass a fragment. If a field should be removed set its value to null. 

 ```bash
 curl -H "Content-Type: application/json" -X POST  http://graphstore.app/docs/Droid/2001 -d '{ "primaryFunction" : "Astromech", "appearsIn" : null }'
 ```

## Alternative to the basic update 

For ease of use some alternative forms are supported:

### Using a <FORM> submission 
 
In this case the JSON must be within a "payload" element of the <FORM> 

### DocType and ID in payload, not URL

Both  http://graphstore.app/docs/Droid & http://graphstore.app/docs URL forms are also supported. In this case
the JSON payload must contain the key "aggregateId" and "docType", e.g. 

```bash
curl -H "Content-Type: application/json" -X POST  http://graphstore.app/docs -d '{ "docType": "Droid", "aggregateId" : "2001", "name": "R2-D2" }'
```

These additional keys are obviously removed from the payload before writing to the event store.

### Validation Mode 

Add the validationMode param to change the default of "Skip"

