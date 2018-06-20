# doc-store
[![Travis CI](https://img.shields.io/travis/ianmorgan/doc-store/master.svg)](https://travis-ci.org/ianmorgan/doc-store)
[![License](https://img.shields.io/github/license/ianmorgan/doc-store.svg)](https://github.com/ianmorgan/doc-store/blob/master/LICENSE)


Setup [DDD style aggregates](https://martinfowler.com/bliki/DDD_Aggregate.html) from a GraphQL schema. Store 
aggregate state to an event-store using REST, and query using both GraphQL 
and traditional REST. More advanced applications can use [CQRS](https://www.martinfowler.com/bliki/CQRS.html) 
principle to build custom data integrations and views by simply reducing 
the underlying event stream.
 
 

* data is stored using an event sourcing model in an 'event-store'.
* a [GraphQL](https://graphql.org/) API is provided for querying 
* Schema information can be attached to drive GraphQL 
API and to provide some level of validation when saving data.
* like most document style stores, the ability to store both complete 
documents and partial updates is supported. 
* by keeping to simple, generic events this is deliberately aimed at problem 
domains where an aggregate can simply be modelled as a document. The architectural benefits of event sourcing are 
retained, for example another system could observe the events, but there is no attempt to define state as a set of 
explicit domain events (AccountCreated, AccountApproved etc). 

For more details try the [docs](https://ianmorgan.github.io/doc-store/) and also [Trello](https://trello.com/b/5lXXr7jc/doc-store) 