# Scratch 

Docs in progress, often initial design thoughts that need to gestate. 

## Standard app flags 

Java apps use [commons CLI](http://commons.apache.org/proper/commons-cli)

Use GNU like syntax ,e.g.

```bash
du --human-readable --max-depth=1)
```

All options inject as command line args, don't use Java property syntax -Dxxx=aaa 

Common args are 

--seed   
Start with a pre seeded data set. This is generallt for testing and demo, production 
services would not start with seeded data. Some apps might support more than one set, in which case

--seed=<dataset>

--debug  
Start in debug mode with extended logging. For simplicity we only allow one set of debug settings


### AWS 

46.137.72.123

https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/SSL-on-an-instance.html

sudo service httpd start

ifconfig eth0 | grep inet | awk '{ print $2 }'

```text
gjgh
```

### GraphQI 

docker run -d --name graph-store -p 7002:7002   graph-store
docker run -d --name graphiql -p 4000:4000 -e API_URL=http://graph-store:7002  npalm/graphiql


### Link vs embedding 

There are 2 basic forms for holding nested data 

#### Embedded (Composition in UML speak)

This is the simplest form but only allows access by the root document 

{
  "id" : "123"
  "name" : "John",
  "address" : { 
    "street" : "Penny Lane",
    "suburb" : "Liverpool"
  }
}

#### Linked (Aggregation in UML speak)

This form is more "database" like. Address has its own ID, so can manipulated 
independently 
 
{
  "id" : "123"
  "name" : "John",
  "addressId" : "abc"
}

{
  "id" : "abc",
  "street" : "Penny Lane",
  "suburb" : "Liverpool"
}

But it means more work building graph style queries that return joins resolved a list 

{
  "id" : "123"
  "name" : "John",
  "address" : { 
    "street" : "Penny Lane",
    "suburb" : "Liverpool"
  }
}

By convention this can be setup as follows

type Address {
  id: ID!
  street: String!
  suburb: String!
}

type Person {
  id: ID!
  name: String!
  
  address : Address
}

The rules is really very simple, as Address has an ID field it is stored as its own Document 

So via REST 

/docs/Address/abc returns 

{
  "id" : "abc",
  "street" : "Penny Lane",
  "suburb" : "Liverpool"
}

/docs/Person/123 returns 
{
  "id" : "123"
  "name" : "John",
  "addressId" : "abc"
}

Note that the "Id" is appended by taking the name of the ID field in Address

via GraphQL it is 
{
  "id" : "123"
  "name" : "John",
  "address" : { 
    "id" : "999",
    "street" : "Penny Lane",
    "suburb" : "Liverpool"
  }
}

 
The embedded case looks like this 

type Address {
  street: String!
  suburb: String!
}

type Person {
  id: ID!
  name: String!
  address : Address
}

there are no REST endpoints for Address now

/docs/Person/123 returns 
{
  "id" : "123"
  "name" : "John",
  "address" : {
    "street" : "Penny Lane",
    "suburb" : "Liverpool"
  }
} 

which is actually the same as basic GraphQL form, though of course all the other benefits 
of GraphQL are available.

{
  "id" : "123"
  "name" : "John",
  "address" : {
    "street" : "Penny Lane",
    "suburb" : "Liverpool"
  }
} 


When to use which form? I would argue stick with Document database metaphor and keep to the embedded form 
if possible, otherwise its easy to fall into the relational database mindset and make everything a table. 
Remember there is an underlying assumption that if more advanced queries (maybe finding people by address) 
are needed they will be built in another technology (probably some form of database) by observing 
the underlying event stream 

??? is this good advice ???