# About

This is a basic doc store that supports an event sourcing storage model and GraphQL enabled API. 
Think of a cutdown, single node MongoDB that supports GraphQL and which keeps a full history of every change.

The inbuilt query model is quite limited, as this solution is aimed for applications built 
 using DDD and CQRS principles. The intention is that is as more complex query models are 
required other services will monitor the event stream(s) to build dedicated views 
using the appropriate technology.

## Getting started 

The doc-store requires a running [event-store](https://ianmorgan.github.io/event-store/). 

The API is document centric, 
with each document representing a [DDD Aggregate](https://martinfowler.com/bliki/DDD_Aggregate.html) . 

The first step is to register a schema associated with one or more documents. This is in the [GraphQL schema](http://graphql.org/learn/schema/)
format. The examples here are all based on the [Star Wars](https://github.com/apollographql/starwars-server/blob/master/data/swapiSchema.js) 
schema from the GraphQL demos.

Below is a cut down example from the full schema.

```yaml
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

Each 'type' is treated as a distinct document types. An 'interface' is a read only document. So in this case we now have 
a readonly 'Character' document, and updatable 'Human' and 'Droid' documents. An example 'Droid' 
document in JSON could be:

```json
{
   "id" : "2001",
   "name" : "R2 D2",
   "appearsIn" : ["NEWHOPE","EMPIRE","JEDI"]
}
```

```bash
curl -H "Content-Type: application/graphql" -X PUT  http://localhost:7002/schema/starwars -d @starwars.schema
``` 

## Storing data 

Each type in the schema has become a 'document', and can be modified. Think of this as an "out the box" mutation,
though its not using the formal GraphQL mutation syntax.

So to add a character 

```bash
curl -H "Content-Type: application/json" -X PUT  http://localhost:7002/docs/driod -d '{ "1d" : "2001",  name": "R2-D2","appearsIn": ["NEWHOPE","EMPIRE","JEDI"] }'
```

This is also available available to query as a regular JSON doc (so simple REST, but no GraphQL support)

 
```bash
curl -X GET http://localhost:7002/docs/driod/2001
```
