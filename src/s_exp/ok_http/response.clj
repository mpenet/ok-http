(ns s-exp.ok-http.response
  (:require [s-exp.ok-http.headers :as h])
  (:import (java.io ByteArrayInputStream)
           (okhttp3 Response)))

(set! *warn-on-reflection* true)

(defmulti response->ex-info!
  "Throws the matching exception for status HTTP response.
  Clients are expected to have already handled success status codes."
  :status)

(defn- ex!
  [type message data]
  (throw (ex-info message
                  (assoc data
                         :type type
                         :response data))))

(defmacro def-response->ex [status type message]
  `(defmethod response->ex-info! ~status
     [response#]
     (ex! ~type ~message response#)))

(def-response->ex :default :s-exp.ok-http.response/fault "HTTP Response")
(def-response->ex 400 :s-exp.ok-http.response/incorrect "Bad Request")
(def-response->ex 401 :s-exp.ok-http.response/forbidden "Unauthorized")
(def-response->ex 403 :s-exp.ok-http.response/forbidden "Forbidden")
(def-response->ex 404 :s-exp.ok-http.response/not-found "Not Found")
(def-response->ex 405 :s-exp.ok-http.response/unsupported "Method Not Allowed")
(def-response->ex 409 :s-exp.ok-http.response/conflict "Conflict")
(def-response->ex 429 :s-exp.ok-http.response/busy "Too Many Requests")
(def-response->ex 500 :s-exp.ok-http.response/fault "Internal Server Response")
(def-response->ex 501 :s-exp.ok-http.response/unsupported "Not Implemented")
(def-response->ex 503 :s-exp.ok-http.response/busy "Service Unavailable")
(def-response->ex 502 :s-exp.ok-http.response/unavailable "Bad Gateway")
(def-response->ex 504 :s-exp.ok-http.response/unavailable "Gateway Timeout")

(defn body
  [^Response response {:as _opts :keys [response-body-decoder]}]
  (let [body (.body response)]
    (case response-body-decoder
      :string (-> body .byteStream slurp)
      :bytes (.bytes body)
      ;; must be closed or we leak
      :byte-stream (.byteStream body)
      ;; leak proof, but it's an eager copy
      :input-stream (-> body .bytes (ByteArrayInputStream.)))))

(def ok-status
  #{200 201 202 203 204 205 206 207 300 301 302 303 304 307 308})

(defn build
  [^Response response {:as opts :keys [throw-on-error]}]
  (let [status (.code response)
        response
        {:status status
         :headers (h/response->map response)
         :body (body response opts)}]
    (if (and throw-on-error
             (not (contains? ok-status status)))
      (response->ex-info! response)
      response)))
