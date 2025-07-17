(ns s-exp.ok-http.headers
  (:require [s-exp.ok-http.util :as u])
  (:import (okhttp3 Headers
                    Headers$Builder
                    Response)))

(defn map->headers
  ^Headers [headers]
  (let [b (Headers$Builder/new)]
    (run! (fn [[k v]]
            (.add b
                  (u/any->str k)
                  (u/any->str v)))
          headers)
    (.build b)))

(defn response->map
  [^Response response]
  (-> (reduce-kv (fn [m k v]
                   (if (= 1 (count v))
                     (assoc! m k (first v))
                     (assoc! m k v)))
                 (transient {})
                 (.toMultimap (.headers response)))
      persistent!))
