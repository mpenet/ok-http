(ns exoscale.ok-http
  (:require [exoscale.ok-http.options :as options]
            [exoscale.ok-http.request :as request]
            [exoscale.ok-http.response :as response])
  (:import
   (okhttp3 OkHttpClient
            OkHttpClient$Builder)))

(set! *warn-on-reflection* true)

(def client-options {})

(defn client
  "Creates new `client` from `opts` or creates a new `client` from a `client`,
  copying it's settings and extra `opts`. Returned client can be used with
  `request`.

  Options:

  * `:call-timeout` (in ms)
  * `:read-timeout` (in ms)
  * `:write-timeout` (in ms)
  * `:connect-timeout` (in ms)
  * `:protocols` - one of \"http/1.0\", \"http/1.1\", \"h2\", \"h2_prior_knowledge\", \"quic\", \"spdy/3.1\", \"h3\"
  * `:dispatcher` : map of `:executor`, `:max-requests`, `:max-requests-per-host`
  * `:connection-pool` : map of `:max-idle-connections`, `:keepalive-duration` (in ms)
  * `:retry-on-connection-failure`
  * `:follow-redirects`
  * `:ssl-socket-factory`
  * `:follow-ssl-redirects`
  * `:add-network-interceptors`
  * `:add-interceptors`"
  ([^OkHttpClient client opts]
   (let [b ^OkHttpClient$Builder (.newBuilder client)]
     (-> b
         (options/set-options! opts)
         (.build))))
  ([opts]
   (let [^OkHttpClient$Builder b
         (-> (OkHttpClient$Builder/new)
             (options/set-options! (into client-options opts)))]
     (.build b))))

(def request-options
  {:throw-on-error true
   :response-body-decoder :byte-stream})

(defn request
  "Performs a http request via `client`, using `request-map` as payload.
   Returns a ring response map

  Options:
  * `:throw-on-error` - defaults to true

  * `:response-body-decoder` - `:byte-stream` (default, ensure it's consumed!),
  `:string`, `:bytes`, `:input-stream` (safe, eager, copy)"

  [^OkHttpClient client request-map]
  (let [opts (into request-options request-map)]
    (-> client
        (.newCall (request/build opts))
        (.execute)
        (response/build opts))))
