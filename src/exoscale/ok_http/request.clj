(ns exoscale.ok-http.request
  (:require [clojure.string :as str])
  (:import (java.io File InputStream)
           (okhttp3 Request$Builder
                    Headers
                    HttpUrl
                    HttpUrl$Builder
                    Request
                    Headers$Builder
                    RequestBody
                    MediaType)
           (okio ByteString Okio)))

(set! *warn-on-reflection* true)

(defprotocol ToBody
  (to-body [body opts]))

(def default-media-type (MediaType/parse "application/octet-stream"))

(extend-protocol ToBody
  byte/1
  (to-body [^bytes/1 body ^MediaType media-type]
    (RequestBody/create media-type body))

  java.io.InputStream
  (to-body [^InputStream body ^MediaType media-type]
    (RequestBody/create media-type
                        ^ByteString (-> (Okio/buffer (Okio/source body))
                                        .readByteString)))
  String
  (to-body [^String body ^MediaType media-type]
    (RequestBody/create media-type ^String body))

  File
  (to-body [^File body ^MediaType media-type]
    (RequestBody/create media-type ^File body))

  Object
  (to-body [body ^MediaType media-type]
    (to-body (str body) media-type))

  nil
  (to-body [_body ^MediaType _media-type]
    nil))

(defn ->headers
  ^Headers [headers]
  (let [b (Headers$Builder/new)]
    (run! (fn [[k v]]
            (.add b
                  (name k)
                  (name v)))
          headers)
    (.build b)))

(defn ->method
  [method]
  (case method
    :get "GET"
    :post "POST"
    :put "PUT"
    :delete "DELETE"
    :head "HEAD"
    :patch "PATCH"
    :options "OPTIONS"
    (-> method name str/upper-case)))

(def media-type
  (memoize
   (fn [content-type]
     (if content-type
       (MediaType/parse content-type)
       default-media-type))))

(defn add-query-parameters
  ^HttpUrl [^HttpUrl http-url query-params]
  (let [b (.newBuilder http-url)]
    (run! (fn [k v]
            (if (sequential? v)
              (run! #(.addQueryParameter b (name k) %) v)
              (.addQueryParameter b (name k) v)))
          query-params)
    (.build b)))

(defn build
  ^Request
  [{:as _request :keys [method headers url body query-params]}]
  (let [method (->method method)
        req (Request$Builder/new)
        headers' (->headers headers)
        ct (.get headers' "content-type")
        http-url (cond-> (HttpUrl/parse url)
                   query-params
                   (add-query-parameters query-params))]
    (-> (doto req
          (.method method (to-body body (media-type ct)))
          (.headers (->headers headers))
          (.url http-url))
        .build)))
