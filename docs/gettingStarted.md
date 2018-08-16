# Getting started 

## Running 

To run locally with an in memory event-store and default star wars schema

```bash
./gradlew run
```

To run with [command line options](commandLineOptions)

```bash
.\gradlew run -PappArgs="['-h']"
```


To run under Docker 

```bash
docker run -d -p 7002:7002 ianmorgan/graph-store
```

_For a more production like deployment, it is better to use a docker compose file. There is 
an example on [Github](https://github.com/ianmorgan/docker-stacks/tree/master/doc-store-starwars). 
More detail on building and running under Docker is [here](docker)._

## What URL? 

By default the service starts on port 7002, so running natively the base url is below (this is assumed in the 
examples below)

<code>http://localhost:7002</code> 

The convention under Docker compose will be

<code>http://graph-store:7002</code>

but depending upon how docker is setup it may also be available on localhost 

The running test instance is at 

<code>https://graphstore.app</code>

## Setting up a Schema

The first step is to register a schema associated with one or more documents. This is in the [GraphQL schema](http://graphql.org/learn/schema/)
format. The examples here are all based on the [Star Wars](https://github.com/apollographql/starwars-server/blob/master/data/swapiSchema.js) 
schema from the GraphQL demos.

By default the service will start with a version of the star wars schema, and this can be viewed with 

```bash
curl -X GET http://localhost:7002/schema
```

Below is a cut down copy of the schema. 

 
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

## Understanding how GraphQL schemas are used 

The graph store allows both a traditional REST API and a GraphQL API to operate over the same schema. 

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

Generally each document can be considered similar to a [DDD Aggregate](https://martinfowler.com/bliki/DDD_Aggregate.html).
Its not an exacting mapping, see [Relationship to DDD](relationshipToDDD). 

## Storing data 

Each type in the schema has become a 'document', and can be modified. This is done by a traditional REST style operation
(future enhancements might support GraphQL mutations).

So to add a new Droid Character. 

```bash
curl -H "Content-Type: application/json" -X POST  http://localhost:7002/docs/Droid -d '{ "id" : "2001",  "name": "R2-D2","appearsIn": ["NEWHOPE","EMPIRE","JEDI"] }'
```

The service performs a basic schema check of the submitted JSON doc and confirms that structure and 
data types match the GraphQL schema, but it does not validate mandatory fields nor any relationship between 
fields. 

To update, simply pass a fragment. If a field should be removed, set its value to null. 

 
 ```bash
 curl -H "Content-Type: application/json" -X POST  http://localhost:7002/docs/Droid -d '{ "id" : "2001", "primaryFunction" : "Astromech" }'
 ```

This is also available to query as a regular JSON doc (i.e. simple REST, no GraphQL support).

 
```bash
curl -X GET http://localhost:7002/docs/Droid/2001
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

## Querying with GraphQL 

Of course, the whole point of GraphQL is to make issuing queries easier. 

```bash
curl -X POST -H "Content-Type: application/json" http://localhost:7002/graphql -d '{droid(id: "2001"){name,primaryFunction,appearsIn,friends{name},starships{name,manufacturer}}}'
``` 

Any valid GraphQL query can be passed. 