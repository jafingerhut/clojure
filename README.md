# What is this?

This is a slightly modified version of Clojure forked just after
Clojure 1.10.0-beta4 was released, inspired by ClojureScript developer
extraordinaire Mike Fikes and a conversation on the Clojurians Slack
channel excerpted below.

To try it out, the instructions below will install it as long as you
have a suitable JDK Java Developer Kit, Maven build tool, and `clj`
Clojure CLI tool installed.  This should _not_ add the modified
version of Clojure into your `$HOME/.m2` directory, but it may cause
many JAR files, those needed by Maven to build Clojure, to be
downloaded and put there.

```bash
git clone https://github.com/jafingerhut/clojure
cd clojure
git checkout undefined-behavior
mvn -Dmaven.test.skip=true package
clj -Sdeps '{:deps {org.clojure/clojure {:local/root "target/clojure-1.10.0-master-SNAPSHOT.jar"}}}'
```

Then in the Clojure REPL:

```clojure
user=> (require '[clojure.set :as set])
nil
user=> (set/difference #{1 3 5} [2 4 6 8 10 12])
#{}
```


## Inspiration

A few things said on the Clojurians Slack channel `#clojurescript` on
2018-Oct-22:

```
mfikes [2:34 PM]
Yeah, your program is incorrect, so a dancing frog can jump out of the box, and that would fall within the scope of undefined behavior :slightly_smiling_face:

andy.fingerhut [2:35 PM]
Thankfully most of Clojure and ClojureScript's _actual_ undefined behavior is significantly less surprising than that :slightly_smiling_face:

mfikes [2:35 PM]
Hah!
Just once, I would like to see my program do this, and my life will be complete.
For those who are curious: https://www.youtube.com/watch?v=MsROL4Kf8QY (edited)
```


## More details

+ http://clojuredocs.org/clojure.set/difference
+ https://dev.clojure.org/jira/browse/CLJ-1953
+ https://dev.clojure.org/jira/browse/CLJ-2287
