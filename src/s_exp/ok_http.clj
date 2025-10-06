(ns s-exp.ok-http
  (:require [clojure.string :as str]
            [s-exp.ok-http.options :as options]
            [s-exp.ok-http.request :as request]
            [s-exp.ok-http.response :as response])
  (:refer-clojure :exclude [get])
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
     (.build b)))
  ([]
   (client {})))

(defonce default-client (delay (client {})))

(def request-options
  {:throw-on-error true
   :response-body-decoder :byte-stream})

(defn request
  "Performs an HTTP request using the provided OkHttp client and a request map.
  Returns a Ring-style response map containing keys such as :status, :headers, :body.

  Arguments:
  * `client` (okhttp3.OkHttpClient): Optional, if not provided the default client is used.
  * `request-map` (map): Map describing request parameters.

  Supported keys in `request-map`:
  * `:method` - HTTP method keyword (:get, :post, etc), defaults to :get
  * `:url` - Absolute URL string (required)
  * `:headers` - Map of header names to values
  * `:body` - Request body (string, bytes, stream, file, etc.)
  * `:query-params` - Map of query parameters to add to URL

  Options:
  * `:throw-on-error` - If true (default), throws exception on 4xx/5xx. If false, returns response.
  * `:response-body-decoder` - Decoding strategy for :body: `:byte-stream` (default, lazy raw bytes, must be consumed), `:string`, `:bytes`, `:input-stream` (eager, safe to read/copy).

  Returns:
  * Ring response map: {:status int, :headers map, :body value}

  Example:
    (request {:method :get :url \"https://httpbin.org/get\"})
    (request client {:method :post :url \"https://api.com\" :headers {\"Content-Type\" \"application/json\"} :body \"{...}\"})
  "
  ([request-map]
   (request @default-client request-map))
  ([^OkHttpClient client request-map]
   (let [opts (into request-options request-map)]
     (-> client
         (.newCall (request/build opts))
         (.execute)
         (response/build opts)))))

(defmacro def-http-method [method]
  (let [client (vary-meta 'client assoc :tag okhttp3.OkHttpClient)]
    `(defn ~(symbol method)
       ~(format "Performs a %s http request via `client`, using `request-map` as payload.
   Returns a ring response map

  Options:
  * `:throw-on-error` - defaults to true

  * `:response-body-decoder` - `:byte-stream` (default, ensure it's consumed!),
  `:string`, `:bytes`, `:input-stream` (safe, eager, copy)"
                (str/upper-case (name method)))
       ([~'request-map] (request @default-client (assoc ~'request-map :method ~method)))
       ([~client ~'request-map]
        (request ~client (assoc ~'request-map :method ~method))))))

(def-http-method :get)
(def-http-method :post)
(def-http-method :put)
(def-http-method :patch)
(def-http-method :options)
(def-http-method :trace)
(def-http-method :head)
(def-http-method :delete)
(def-http-method :connect)

(defn shutdown!
  "Closes all connections and releases resources for the provided OkHttpClient instance.
   This will:
   - Evict all connections from the internal connection pool
   - Shutdown the underlying executor service
   - Close the cache, if present
   Call this when you are done with an OkHttpClient to avoid resource leaks."
  [^OkHttpClient client]
  (-> client .connectionPool .evictAll)
  (-> client .dispatcher .executorService .shutdown)
  (when-let [cache (.cache client)]
    (.close cache))
  client)
