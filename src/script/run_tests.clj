(System/setProperty "clojure.test.generative.msec" "1000")
(require '[clojure.test.generative.runner :as runner])
(runner/-main "test")
