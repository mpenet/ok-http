(ns s-exp.ok-http.options
  (:import (java.time Duration)
           (java.util.concurrent TimeUnit)
           (javax.net.ssl HostnameVerifier)
           (okhttp3 OkHttpClient$Builder
                    Dispatcher
                    ConnectionPool
                    EventListener
                    EventListener$Factory
                    Authenticator
                    Protocol)))

(defmulti set-option! (fn [^OkHttpClient$Builder _b k _v] k))

(defmethod set-option! :connection-pool
  [^OkHttpClient$Builder b _ connection-pool]
  (.setConnectionPool b (cond
                          (instance? ConnectionPool connection-pool)
                          connection-pool
                          (map? connection-pool)
                          (let [{:keys [max-idle-connections
                                        keepalive-duration]}
                                connection-pool]
                            (ConnectionPool. (int max-idle-connections)
                                             keepalive-duration
                                             TimeUnit/MILLISECONDS)))))

(defmethod set-option! :ssl-socket-factory
  [^OkHttpClient$Builder b _ [ssl-socket-factory trust-manager]]
  (.sslSocketFactory b ssl-socket-factory trust-manager))

(defmethod set-option! :add-interceptors
  [^OkHttpClient$Builder b _ interceptors]
  (doseq [ix interceptors]
    (set-option! b :add-interceptor ix))
  b)

(defmethod set-option! :add-interceptor
  [^OkHttpClient$Builder b _ interceptor]
  (.addInterceptor b interceptor))

(defmethod set-option! :add-network-interceptors
  [^OkHttpClient$Builder b _ interceptors]
  (doseq [ix interceptors]
    (set-option! b :add-network-interceptor ix))
  b)

(defmethod set-option! :add-network-interceptor
  [^OkHttpClient$Builder b _ interceptor]
  (.addNetworkInterceptor b interceptor))

(defmethod set-option! :follow-redirects
  [^OkHttpClient$Builder b _ v]
  (.followRedirects b v))

(defmethod set-option! :follow-ssl-redirects
  [^OkHttpClient$Builder b _ v]
  (.followSslRedirects b v))

(defmethod set-option! :retry-on-connection-failure
  [^OkHttpClient$Builder b _ v]
  (.retryOnConnectionFailure b v))

(defmethod set-option! :event-listener
  [^OkHttpClient$Builder b _ ^EventListener v]
  (.eventListener b v))

(defmethod set-option! :event-listener-factory
  [^OkHttpClient$Builder b _ ^EventListener$Factory v]
  (.eventListenerFactory b v))

(defmethod set-option! :hostname-verifier
  [^OkHttpClient$Builder b _ ^HostnameVerifier v]
  (.hostNameVerifier b v))

(defmethod set-option! :dispatcher
  [^OkHttpClient$Builder b _ dispatcher]
  (.dispatcher b
               (cond
                 (instance? Dispatcher dispatcher)
                 dispatcher
                 (map? dispatcher)
                 (let [{:as _dispatcher
                        :keys [executor
                               max-requests
                               max-requests-per-host]} dispatcher
                       d ^Dispatcher (Dispatcher. executor)]
                   (when max-requests
                     (.setMaxRequests d (int max-requests)))
                   (when max-requests-per-host
                     (.setMaxRequestsPerHost d (int max-requests-per-host)))
                   d))))

(defmethod set-option! :protocols
  [^OkHttpClient$Builder b _ v]
  (.protocols b (map #(Protocol/get %) v)))

(defmethod set-option! :authenticator
  [^OkHttpClient$Builder b _ ^Authenticator v]
  (.authenticator b v))

(defmethod set-option! :cache
  [^OkHttpClient$Builder b _ c]
  (.cache b c))

(defmethod set-option! :write-timeout
  [^OkHttpClient$Builder b _ v]
  (.writeTimeout b (Duration/ofMillis v)))

(defmethod set-option! :read-timeout
  [^OkHttpClient$Builder b _ v]
  (.readTimeout b (Duration/ofMillis v)))

(defmethod set-option! :connect-timeout
  [^OkHttpClient$Builder b _ v]
  (.connectTimeout b (Duration/ofMillis v)))

(defmethod set-option! :call-timeout
  [^OkHttpClient$Builder b _ v]
  (.callTimeout b (Duration/ofMillis v)))

(defmethod set-option! :default
  [^OkHttpClient$Builder b _k _v]
  b)

(defn set-options!
  ^OkHttpClient$Builder
  [^OkHttpClient$Builder builder opts]
  (reduce
   (fn [builder [k v]]
     (set-option! builder k v))
   builder
   opts))
