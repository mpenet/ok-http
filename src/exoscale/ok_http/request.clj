(ns exoscale.ok-http.request
  (:require [clojure.string :as str]
            [exoscale.ok-http.headers :as h]
            [exoscale.ok-http.util :as u])
  (:import (java.io File InputStream)
           (okhttp3 Request$Builder
                    HttpUrl
                    Request
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

  RequestBody
  (to-body [^RequestBody b ^MediaType _media-type]
    b)

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

(defn request-method
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

(defn body-for-method
  [method body]
  (case method
    (:post :put :patch :proppatch :report)
    (or body (RequestBody/create (byte-array 0) nil))
    (:head :get) nil
    body))

(def media-type
  (memoize
   (fn [content-type]
     (if content-type
       (MediaType/parse content-type)
       default-media-type))))

(defn add-query-parameters
  ^HttpUrl [^HttpUrl http-url query-params]
  (let [b (.newBuilder http-url)]
    (run! (fn [[k v]]
            (let [k (u/any->str k)]
              (if (sequential? v)
                (run! #(.addQueryParameter b
                                           k
                                           (u/any->str %))
                      v)
                (.addQueryParameter b
                                    k
                                    (u/any->str v)))))

          query-params)
    (.build b)))

(defn build
  ^Request
  [{:as _request
    :keys [method headers url body query-params]
    :or {method :get}}]
  (let [method' (request-method method)
        req (Request$Builder/new)
        headers (h/map->headers headers)
        ct (.get headers "content-type")
        http-url (cond-> (HttpUrl/parse url)
                   (seq query-params)
                   (add-query-parameters query-params))
        body (body-for-method method body)]
    (-> (doto req
          (.method method' (to-body body (media-type ct)))
          (.headers headers)
          (.url http-url))
        .build)))
