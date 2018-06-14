# About

This is a basic doc store that supports an event sourcing storage model and GraphQL enabled API. 
Think of it as a cutdown, single node MongoDB that supports GraphQL and which keeps a full history 
of every change.

The inbuilt query model is quite limited, as this solution is aimed for applications built 
using DDD and CQRS principles. The intention is that is as more complex query models are 
required other services will monitor the event stream(s) to build dedicated views 
using the appropriate technology.

To get working quickly, follow the getting started [Getting Started](#getting-started) guide below. For more 
detail see [Data Access](daos), [Type Mappings](typeMappings) and [Standards](standards).

Work and new features are tracked in [Trello](https://trello.com/b/5lXXr7jc/doc-store) 



## Getting started 

The doc-store requires a running [event-store](https://ianmorgan.github.io/event-store/) and a JVM 
(at least version 8)

To run locally

```bash
./gradlew run
```

To run under Docker 

```bash
docker run -d -p 7002:7002 ianmorgan/doc-store
```

More detail on building and running under Docker is [here](docker).


The API is document centric, 
with each document representing a [DDD Aggregate](https://martinfowler.com/bliki/DDD_Aggregate.html). 

The first step is to register a schema associated with one or more documents. This is in the [GraphQL schema](http://graphql.org/learn/schema/)
format. The examples here are all based on the [Star Wars](https://github.com/apollographql/starwars-server/blob/master/data/swapiSchema.js) 
schema from the GraphQL demos.

Below is a cut down example from the full schema.

```
# The episodes in the Star Wars trilogy
enum Episode {
  NEWHOPE
  EMPIRE
  JEDI
}

# A character from the Star Wars universe
interface Character {
  id: ID!
  name: String!

  # The friends of the character, or an empty list if they have none
  friends: [Character]

  # The movies this character appears in
  appearsIn: [Episode]!
}

# A humanoid creature from the Star Wars universe
type Human implements Character {
  id: ID!
  name: String!
  friends: [Character]
  appearsIn: [Episode]!
  homePlanet: String
}

# An autonomous mechanical character in the Star Wars universe
type Droid implements Character {
  id: ID!
  name: String!
  friends: [Character]
  appearsIn: [Episode]!
  primaryFunction: String
}
```

This must be registered by sending to the 'schema' endpoint. Note that multiple schemas can be registered, so 
the name must be clearly identified in the URL 

```bash
curl -H "Content-Type: application/graphql" -X POST  http://localhost:7002/schema/starwars -d @starwars.schema
``` 

_Note the API doesn't worry about the semantics of HTTP verbs - its simply GET for basic operations 
or POST for anything that needs a body_


In the schema, each 'type' is treated as a distinct document type. An 'interface' is a read only document. So in this case we now have 
a read only 'Character' document, and updatable 'Human' and 'Droid' documents. An example 'Droid' 
document in JSON could be:

```json
{
   "id" : "2001",
   "name" : "R2-D2",
   "appearsIn" : ["NEWHOPE","EMPIRE","JEDI"],
   "primaryFunction" : "Astromech"
}
```


## Storing data 

Each type in the schema has become a 'document', and can be modified. Think of this as an "out the box" mutation,
though its not using the formal GraphQL mutation syntax.

So to add a new Droid Character. 

```bash
curl -H "Content-Type: application/json" -X POST  http://localhost:7002/docs/Droid -d '{ "id" : "2001",  "name": "R2-D2","appearsIn": ["NEWHOPE","EMPIRE","JEDI"] }'
```

The service performs a basic schema check of the submitted JSON doc and confirms that structure and 
data types match the GraphQL schema, but it does not validate mandatory fields nor any relationship between 
types. 

To update, simply pass a fragment. If a field should be removed set its value to null. 

 
 ```bash
 curl -H "Content-Type: application/json" -X POST  http://localhost:7002/docs/Droid -d '{ "id" : "2001", "primaryFunction" : "Astromech" }'
 ```

This is also available to query as a regular JSON doc (i.e. simple REST, no GraphQL support).

 
```bash
curl -X GET http://localhost:7002/docs/Driod/2001
```

returns 

```json
{
   "id" : "2001",
   "name" : "R2-D2",
   "appearsIn" : ["NEWHOPE","EMPIRE","JEDI"],
   "primaryFunction" : "Astromech"
}
```