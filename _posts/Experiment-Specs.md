# Experiment specs


## What are experiment specs?
An experiment spec is a template for creating experiments. It defines the experiment key, test groups (possible outcomes/results), product(s) the experiment belongs to, and an owner.

It is easiest to explain with a sample:

1. define the spec:

```
    public class ButtonColorSpec extends SpecDefinition {

    @Override
    protected ExperimentSpecBuilder customize(ExperimentSpecBuilder builder) {
        return builder.
               withTestGroups(asList("yellow", "blue")).
               withScopes(aScopeDefinitionForAllUserTypes("ourProduct"));
    }
```

2. Create an experiment using the spec definition:

```
    DateTime now = new DateTime();
    petriClient.insertExperiment(
           anExperimentSnapshot().
                   withKey(spec.class.getName()).
                   withGroups(<list built based on values of spec.getTestGroups()>)
           build());
```

3. Use Laboratory's safe conduct API:

```
    return laboratory.conductExperiment(ButtonColorSpec.class, "yellow");
```


## Deciding whether to use experiment specs

The above could also be achieved by the following steps:

1. Create an experiment without using a spec:

```
    DateTime now = new DateTime();
    petriClient.insertExperiment(
           anExperimentSnapshot().
                   withKey("ButtonColorSpec").
                   withGroups(<list of test groups>)
           build());
```

2. Use Laboratory's free text conduct API:

``` 
    return laboratory.conductExperiment("ButtonColorSpec"", "yellow");
```

#### The benefits of using specs

- Using **specs eliminates the possibility of typos**

  - There cannot be a mismatch between the key used to create an experiment and the key used when conducting it.
  - There can also be no mismatch between testgroup values used to create an experiment and the testgroup values the code refers to.

> Our best practice is to allow creating experiments based on existing specs, except for specific use cases where free text keys and/or values are absolutely necessary. This best practice emerged as a lesson learned from cases of hunting down experiments that did not seem to be working. 

- **Using specs enforces a good process**. A product manager and a developer have to sit together and define what makes sense for this experiment at the earliest possible stage of developing the feature.

- **Specs help define the [correct scope of the experiment](https://github.com/wix/petri/wiki/Registered-vs.-Non-Registered-Users)** which is very important for correctness. 



## How to manage experiment specs

The simplest way is to wire the SpecsSynchronizer into your app (see SpecsSynchronizerController for inspiration). Every time you trigger this all specs in your code will be scanned. It is up to you to consider what triggers spec scanning (probably deployment of a new server version).

You could also call the ExperimentSpecBuilder yourself:

```
    petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec(specKey).
                        withTestGroups(testGroupValues).
                build())
```

## Creating experiments without specs

By default, the ExperimentSnapshotBuilder creates an experiment that must have a matching spec.

Creating an experiment without a matching spec will result in an error from Petri Server.

If you want to override this you must call the ```withFromSpec(false)``` on the builder.