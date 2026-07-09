
# For selenium testing purposes

- ApplicationAutomatedTest
Does not have a main method. This is necessary to run the Selenium tests.
- Application
However does have a main method so that it can be run manually.

The GameInitializer initializes the game with either a real CardDeck or a mocked CardDeck.
The mocked CardDeck gives each player always all 13 cards which can be played an unlimited number
of times.

# Failing tests
- try running the server Application manually
- let the front-end application compile