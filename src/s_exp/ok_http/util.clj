(ns s-exp.ok-http.util
  (:import (clojure.lang Keyword Symbol)))

(defprotocol AnyStr
  (any->str [x]))

(extend-protocol AnyStr
  String
  (any->str [x] x)

  Keyword
  (any->str [x] (name x))

  Symbol
  (any->str [x] (name x))

  Object
  (any->str [x] (str x))

  nil
  (any->str [_] ""))

