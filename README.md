# searchthecity.me

RESTful API for [magiccards.info](http://magiccards.info).

Supports the same [syntax](http://magiccards.info/syntax.html).

## TODO

1. allow more than 20 results per page

## Example

`http://api.searchthecity.me/query?q=goblins`

    { "cards" : [ { "attrs" : { "cmc" : "1",
                "loyalty" : null,
                "manacost" : "R",
                "power" : "1",
                "subtype" : "Goblin Warrior",
                "toughness" : "1",
                "type" : "Creature"
              },
            "flavor-text" : "",
            "img" : "http://magiccards.info/scans/en/ch/51.jpg",
            "name" : "Goblins of the Flarg",
            "rules-text" : "Mountainwalk\n\nWhen you control a Dwarf, sacrifice Goblins of the Flarg.",
            "url" : "http://magiccards.info/ch/en/51.html"
          },
          { "attrs" : { "cmc" : "2",
                "loyalty" : null,
                "manacost" : "BR",
                "power" : "1",
                "subtype" : "Goblin",
                "toughness" : "1",
                "type" : "Creature"
              },
            "flavor-text" : "Even the other Goblin races shun the Marsh Goblins, thanks to certain unwholesome customs they practice.",
            "img" : "http://magiccards.info/scans/en/dk/118.jpg",
            "name" : "Marsh Goblins",
            "rules-text" : "Swampwalk",
            "url" : "http://magiccards.info/dk/en/118.html"
          },
          { "attrs" : { "cmc" : "2",
                "loyalty" : null,
                "manacost" : "{R/W}{R/W}",
                "power" : null,
                "subtype" : null,
                "toughness" : null,
                "type" : "Enchantment"
              },
            "flavor-text" : "A river even the merrow dare not cross.",
            "img" : "http://magiccards.info/scans/en/eve/145.jpg",
            "name" : "Rise of the Hobgoblins",
            "rules-text" : "When Rise of the Hobgoblins enters the battlefield, you may pay {X}. If you do, put X 1/1 red and white Goblin Soldier creature tokens onto the battlefield.\n\n{R/W}: Red creatures and white creatures you control gain first strike until end of turn.",
            "url" : "http://magiccards.info/eve/en/145.html"
          },
          { "attrs" : { "cmc" : "2",
                "loyalty" : null,
                "manacost" : "RG",
                "power" : "2",
                "subtype" : "Goblin",
                "toughness" : "2",
                "type" : "Creature"
              },
            "flavor-text" : "Larger and more cunning than most Goblins, Scarwood Goblins are thankfully found only in isolated pockets.",
            "img" : "http://magiccards.info/scans/en/dk/119.jpg",
            "name" : "Scarwood Goblins",
            "rules-text" : "",
            "url" : "http://magiccards.info/dk/en/119.html"
          },
          { "attrs" : { "cmc" : "3",
                "loyalty" : null,
                "manacost" : "2R",
                "power" : "0",
                "subtype" : "Goblin Warrior",
                "toughness" : "3",
                "type" : "Creature"
              },
            "flavor-text" : "Tired of waiting for a dragon to eat them, some hardy goblins struck out to become meals for the unknown.",
            "img" : "http://magiccards.info/scans/en/cfx/76.jpg",
            "name" : "Wandering Goblins",
            "rules-text" : "Domain — {3}: Wandering Goblins gets +1/+0 until end of turn for each basic land type among lands you control.",
            "url" : "http://magiccards.info/cfx/en/76.html"
          }
        ],
      "pagination" : { "cards-per-page" : 20,
          "current-page" : 1,
          "total-cards" : 5,
          "total-pages" : 1
        }
    }

## Limitations

- only searches for English cards

## Running locally

You'll need [Leiningen 2][lein]

    $ cd searchthecity/
    $ lein ring server
      # open a browser to http://localhost:3000 (lein ring server should also do it for you though)

[lein]: https://github.com/technomancy/leiningen

## Deploying

Unfortunately I'm not an expert on deployment strategies for Java/Clojure web apps. 
Would love to have some input on how to propery serve Clojure web apps, especially the "setting up Jetty/Tomcat properly and making it work with Nginx" part.

But running `lein ring uberwar` and sticking the resulting .war file onto a server running Tomcat worked for me. 


## License

MIT (see `LICENSE` file)

Copyright © 2013 John Louis Del Rosario
