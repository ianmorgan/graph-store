# About


Setup [DDD style aggregates](https://martinfowler.com/bliki/DDD_Aggregate.html) from a GraphQL schema. 
Store aggregate state to an event-store using REST, and query using both GraphQL 
and traditional REST. More advanced applications can use [CQRS](https://www.martinfowler.com/bliki/CQRS.html)
principle to build custom data integrations and views by simply reducing 
the underlying event stream. 

This solution is aimed at bring the more traditional CRUD style rapid development stacks 
(Ruby on Rails et al) into a more modern environment 

There is an <a href="https://hackernoon.com/graphql-and-ddd-the-missing-link-4e992a26b711">article</a> on
            why GraphQL is nice implementation tool for DDD. And this
            <a href="https://www.infoq.com/presentations/graphql-sdl">presentation</a> walks through a real world
            project that used GraphQL as the contract between UI and backend developers.

The [Online Store Evolution](onlineStoreEvolution) walks through a basic example that gradually 
builds additional behaviour around a simple schema. 

The inbuilt query model is quite basic. The intention is that as more complex query models are 
required other services will monitor the event stream(s) and aggregate data to build dedicated views 
using the appropriate technology.

To get working quickly, follow the getting started [Getting Started](gettingStarted) guide. 

For more detail: 

* [Command Line Options](commandLineOptions)
* [Data Access](daos)
* [Docker](docker)
* [External Data](externalData)
* [(GraphQL)Fetchers](fetchers)
* [Type Mappings](typeMappings) 
* [REST API](restAPI)
* [Standards](standards)
* [Updates and Validations](updatesAndValidations)


Work and new features are tracked in [Trello](https://trello.com/b/5lXXr7jc/graph-store). 

