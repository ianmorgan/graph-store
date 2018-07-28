# External Data  

## Overview 

Some resources may be held in external systems, typically accessed via a REST 
style API.

These are referred to as 'external Dao's and must at a minimum implement 
the [ReaderDao](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/graphstore/dal/ReaderDao.java).

Currently only one implementation is supported, [ConfigurableRestDocDao](https://github.com/ianmorgan/graph-store/blob/master/src/main/java/ianmorgan/graphstore/dal/ConfigurableRestDocDao.kt
). As the name suggest a configuration can be injected in change URL mapping and data mappings. Future 
enhancements may allow the use of custom implementations.

## Setting up a new external DAO 

### Update the GraphQL schema 

The type information must be added to the schema. Below is a snippet with the extra data 
to include starships. See 'starwars_ex.graphqls' for a full example. 



```json
type Query {
  starship(id: ID!): Starship
}

type Starship {
    id: ID!
    name: String!
    manufacturer : String
    model : String
    lengthInMetres : String
    costInCredits : String
}
```

### Register the DAO 

Information is stored in events. Two are required. 

#### Register Event 

The register event is enough for the application to know that the doc 'Starship' is 
now access as an external read only resource, in this case using the 'ConfigurableRestDocDao'

```json
{"aggregateId":"Starship",
 "creator":"graph-store",
 "payload":{"implementingClass":"ConfigurableRestDocDao"},
  "id":"8c3db09c-3043-46b3-972d-ae3237304ea8",
  "type":"ExternalDaoRegistered",
  "timestamp":1532798376422}
```

#### Configure Event(s)

These setup any configuration required. By convention all external DAOs should just accept a Map of available options, 
in this case:

* baseUrl - the root for building a REST query
* resultMapperScript - a groovy script that maps the external attributes to those needed by the GraphQL schema 


```json
{"aggregateId":"Starship",
 "creator":"graph-store",
 "payload":{"baseUrl":"https://swapi.co/api/starships/",
            "resultMapperScript":"import ianmorgan.graphstore.mapper.MapperHelper;\n\ndef helper = new MapperHelper(raw)\nhelper.copyIfExists('name')\nhelper.copyIfExists('manufacturer')\nhelper.copyIfExists('model')\nhelper.copyIfExists('length','lengthInMetres')\nhelper.copyIfExists('cost_in_credits','costInCredits')\nreturn helper.output() "},
 "id":"ba6490d2-d421-4a11-b49b-563d72ef59e9",
 "type":"ExternalDaoConfigured",
 "timestamp":1532798376423}
```

The 'ExternalDaoRegistry' class has helper methods for generating these events. 

## Future 

The intention is to support custom implementations in the future. This would likely require the 
introduction of a DI framework such as Google Guava or Spring.  

