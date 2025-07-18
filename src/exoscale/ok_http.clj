(ns exoscale.ok-http
  (:require [exoscale.ok-http.request :as request]
            [exoscale.ok-http.response :as response])
  (:import (java.time Duration)
           (okhttp3 OkHttpClient
                    OkHttpClient$Builder
                    Dispatcher
                    Protocol)))

(set! *warn-on-reflection* true)

(defmulti set-client-option! (fn [^OkHttpClient$Builder _b k _v] k))

(defmethod set-client-option! :ssl-socket-factory
  [^OkHttpClient$Builder b _ [ssl-socket-factory trust-manager]]
  (.sslSocketFactory b ssl-socket-factory trust-manager))

(defmethod set-client-option! :add-interceptors
  [^OkHttpClient$Builder b _ interceptors]
  (doseq [ix interceptors]
    (set-client-option! b :add-interceptor ix))
  b)

(defmethod set-client-option! :add-interceptor
  [^OkHttpClient$Builder b _ interceptor]
  (.addInterceptor b interceptor))

(defmethod set-client-option! :add-network-interceptors
  [^OkHttpClient$Builder b _ interceptors]
  (doseq [ix interceptors]
    (set-client-option! b :add-network-interceptor ix))
  b)

(defmethod set-client-option! :add-network-interceptor
  [^OkHttpClient$Builder b _ interceptor]
  (.addNetworkInterceptor b interceptor))

(defmethod set-client-option! :follow-redirects
  [^OkHttpClient$Builder b _ v]
  (.followRedirects b v))

(defmethod set-client-option! :follow-ssl-redirects
  [^OkHttpClient$Builder b _ v]
  (.followSslRedirects b v))

(defmethod set-client-option! :retry-on-connection-failure
  [^OkHttpClient$Builder b _ v]
  (.retryOnConnectionFailure b v))

(defmethod set-client-option! :dispatcher
  [^OkHttpClient$Builder b _ {:as _dispatcher
                              :keys [executor
                                     max-requests
                                     max-requests-per-host]}]
  (set-client-option! b :dispatcher*
                      (let [d ^Dispatcher (Dispatcher. executor)]
                        (when max-requests
                          (.setMaxRequests d (int max-requests)))
                        (when max-requests-per-host
                          (.setMaxRequestsPerHost d (int max-requests-per-host)))
                        d)))

(defmethod set-client-option! :dispatcher*
  [^OkHttpClient$Builder b _ v]
  (.dispatcher b v))

(defmethod set-client-option! :protocols
  [^OkHttpClient$Builder b _ v]
  (.protocols b (map Protocol/get v)))

(defmethod set-client-option! :write-timeout
  [^OkHttpClient$Builder b _ v]
  (.writeTimeout b (Duration/ofMillis v)))

(defmethod set-client-option! :read-timeout
  [^OkHttpClient$Builder b _ v]
  (.readTimeout b (Duration/ofMillis v)))

(defmethod set-client-option! :connect-timeout
  [^OkHttpClient$Builder b _ v]
  (.connectTimeout b (Duration/ofMillis v)))

(defmethod set-client-option! :call-timeout
  [^OkHttpClient$Builder b _ v]
  (.callTimeout b (Duration/ofMillis v)))

(defmethod set-client-option! :default
  [^OkHttpClient$Builder b _k _v]
  b)

(defn set-client-options!
  ^OkHttpClient$Builder
  [^OkHttpClient$Builder builder opts]
  (reduce
   (fn [builder [k v]]
     (set-client-option! builder k v))
   builder
   opts))

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
  * `:retry-on-connection-failure`
  * `:follow-redirects`
  * `:ssl-socket-factory`
  * `:follow-ssl-redirects`
  * `:add-network-interceptors`
  * `:add-interceptors`"
  ([^OkHttpClient client opts]
   (let [b ^OkHttpClient$Builder (.newBuilder client)]
     (-> b
         (set-client-options! opts)
         (.build))))
  ([opts]
   (let [^OkHttpClient$Builder b
         (-> (OkHttpClient$Builder/new)
             (set-client-options! (into client-options opts)))]
     (.build b))))

(def request-options {:throw-on-error true})

(defn request
  "Performs a http request via `client`, using `request-map` as payload.
   Returns a ring response map"
  [^OkHttpClient client request-map]
  (let [opts (into request-options request-map)]
    (-> client
        (.newCall (request/build opts))
        (.execute)
        (response/build opts))))
