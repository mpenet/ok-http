(ns exoscale.ok-http.response
  (:require [exoscale.ex.http :as ex-http])
  (:import (okhttp3 Response)))

(defn headers
  [^Response response]
  (-> (reduce-kv (fn [m k v]
                   (if (= 1 (count v))
                     (assoc! m k (first v))
                     (assoc! m k v)))
                 (transient {})
                 (.toMultimap (.headers response)))
      persistent!))

(defn body
  [^Response response {:as _opts :keys [response-body-decoder]}]
  (-> response .body .byteStream (cond-> (= :string response-body-decoder) slurp)))

(def ok-status
  #{200 201 202 203 204 205 206 207 300 301 302 303 304 307 308})

(defn build
  [^Response response {:as opts :keys [throw-on-error]}]
  (let [status (.code response)
        response
        {:status status
         :headers (headers response)
         :body (body response opts)}]
    (if (and throw-on-error
             (not (contains? ok-status status)))
      (ex-http/response->ex-info! response)
      response)))
