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


