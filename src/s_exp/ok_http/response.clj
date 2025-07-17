(ns s-exp.ok-http.response
  (:require [exoscale.ex.http :as ex-http]
            [s-exp.ok-http.headers :as h])
  (:import (java.io ByteArrayInputStream)
           (okhttp3 Response)))

(set! *warn-on-reflection* true)

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
      (ex-http/response->ex-info! response)
      response)))
