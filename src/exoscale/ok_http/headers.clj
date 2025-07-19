(ns exoscale.ok-http.headers
  (:import (okhttp3 Headers
                    Headers$Builder
                    Response)))

(defn- header-val
  ^String [x]
  (cond
    (string? x) x
    (ident? x) (name x)
    :else (str x)))

(defn map->headers
  ^Headers [headers]
  (let [b (Headers$Builder/new)]
    (run! (fn [[k v]]
            (.add b
                  (header-val k)
                  (header-val v)))
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
