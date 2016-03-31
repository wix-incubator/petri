### Terminology 
- Experiment : A method of modifying system’s behaviour based on context.
- Feature Toggle / Feature Flag is a stateless experiment.
- A/B test is a stateful experiment where we keep track of the participants.

##### Multiple experiments can be created with the same key - for example running 2 experiments of the same type:
- 1 experiment on 50% of users in Japan & 1 experiment on 20% of users in USA
- 1 FT for all company employees & 1 A/B test for the rest of the world

##### For creating/editing experiments see [here](https://github.com/wix/petri/wiki/Creating-a-Petri-BackOffice-app)