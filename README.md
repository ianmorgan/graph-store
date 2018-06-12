# doc-store

Provides a simple document style persistence API with the 
following features: 

* data is stored using an event sourcing model in an 'event-store'.
* GraphQL API is provided for querying 
* Schema information can be attached to drive GraphQL 
API and to provide some level of validation when saving data.
* like most document style stores, the ability to store both complete 
documents and partial updates is supported. 
* by keeping to simple, generic events this is deliberately aimed at problem 
domains where an aggregate can simply be modelled as a document. The architectural benefits of event sourcing are 
retained, for example another system could observe the events, but there is no attempt to define state as a set of 
explicit domain events (AccountCreated, AccountApproved etc). 

For more details try the [docs](https://ianmorgan.github.io/doc-store/) and also [Trello](https://trello.com/b/5lXXr7jc/doc-store) 