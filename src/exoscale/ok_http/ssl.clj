(ns exoscale.ok-http.ssl
  (:require [clojure.java.io :as io])
  (:import (java.io File)
           (java.io FileInputStream)
           (java.io InputStream)
           (java.security.cert CertificateFactory)
           (java.security.cert X509Certificate)
           (javax.net.ssl SSLContext)))

(defn ssl-socket-factory
  [tls])

(defn trust-manager
  [tls])

(defn ^"[Ljava.security.cert.X509Certificate;"
  read-ca-certs
  "Read all available certificate from the default UNIX location Adds any
   provided certificate as CA.

   Yields an array of X509Certificate, ready to be used to build a
   `trustManager` for an `SslContext`"
  [files]
  (let [etc-certs (-> (io/file "/etc/ssl/certs") (.listFiles) seq)
        fact (CertificateFactory/getInstance "X509")
        files' (concat etc-certs files)]
    (into-array X509Certificate
                (eduction (comp
                           (map io/file)
                           (filter File/.isFile)
                           (map FileInputStream/new)
                           (mapcat #(CertificateFactory/.generateCertificates fact)))
                          files'))))

(defn ssl-context
  [{:as tls :keys [authorities]}]
  (let [^SSLContext ssl-ctx (SSLContext/getInstance "TLS")]
    (.init ssl-context)))

;; (defn ssl-context
;;   [{:as tls :keys [authorities cert pkey]}]
;;   ;; tls in our format ex:
;;   {:authorities ["/etc/host-certificate/ssl/ca.pem"
;;                  "/etc/host-certificate/ssl/root.pem"]
;;    :cert "/etc/host-certificate/ssl/cert.pem"
;;    :pkey "/etc/host-certificate/ssl/key.pkcs8"}
;;   (let [ca-cert (read-ca-certs authorities)
;;         ssl-context (SSLContext/getInstance "TLS")]
;;     (.init ssl-context )))
