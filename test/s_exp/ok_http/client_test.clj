(ns s-exp.ok-http.client-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [exoscale.ex :as ex]
            exoscale.ex.test
            [s-exp.ok-http :as client]
            [s-exp.ok-http.mocks :as mocks])
  (:import
   (java.net SocketTimeoutException)
   (java.util.logging Logger Level)
   (okhttp3 OkHttpClient)))

(.setLevel (Logger/getLogger (.getName OkHttpClient)) Level/FINE)

(def large-file "https://ash-speed.hetzner.com/1GB.bin")

(def ^:dynamic *client* nil)
(def ^:dynamic *client-opts* {})
(def ^:dynamic request nil)

(use-fixtures :once
  (fn [t]
    (binding [*client* (client/client *client-opts*)
              request (fn [req] (client/request *client* req))]
      (t))))

(deftest test-simple-requests-roundrip
  (is (= 200 (:status (request {:method :get :url "http://google.com"}))))

  (is (= 200 (:status (request {:method :get :url "http://google.com"}))))

  (is (= 200 (:status (request {:method :get :url "http://google.com"}))))

  (is (= 200 (:status (request {:method :get :url "http://google.com"
                                :headers
                                {:foo "bar"
                                 "bar" 1
                                 "bars" [1 2 ""]}}))))

  (is (= 200 (:status (request {:method :get :url "http://google.com" :body "test"}))))

  (is (thrown-ex-info-type? :exoscale.ex/not-found
                            (request {:method :get :url "http://google.com/404"}))
      "errors are mapped correctly for GET")

  (is (thrown-ex-info-type? :exoscale.ex/unsupported
                            (request {:method :post :url "http://google.com"
                                      :body ""}))
      "errors are mapped correctly for POST")

  (is (= 405 (:status (request {:method :post :url "http://google.com"
                                :body ""
                                :throw-on-error false})))
      "disabling the throw opts"))

(deftest test-get-body
  (mocks/with-server 1234 (constantly {:status 200
                                       :body "Some value"})
    (let [{:keys [status body]}
          (request {:method :get
                    :url "http://localhost:1234"
                    :body nil
                    :response-body-decoder :string})]
      (is (= 200 status))
      (is (= "Some value" body)))

    (let [{:keys [status body]}
          (client/get *client*
                      {:url "http://localhost:1234"
                       :body nil
                       :response-body-decoder :string})]
      (is (= 200 status))
      (is (= "Some value" body))))

  (mocks/with-server 1234 (fn [_]
                            {:status 200
                             :body "Some value"})
    (let [{:keys [status body]}
          (request {:method :get
                    :url "http://localhost:1234"
                    :body "asdf"
                    :response-body-decoder :string})]
      (is (= 200 status))
      (is (= "Some value" body)))))

(deftest test-timeout
  (binding [*client-opts* {:read-timeout 1}]
    (is (thrown? SocketTimeoutException
                 (client/request (client/client *client-opts*)
                                 {:method :get :url "http://google.com"})))))

(deftest test-body-handler
  (mocks/with-server 1234 (constantly {:status 200
                                       :body "Some value"})
    (let [{:keys [status body]} (request {:method :get
                                          :url "http://localhost:1234"
                                          :response-body-decoder :string})]
      (is (= 200 status))
      (is (= "Some value" body)))))

(deftest test-post-body
  (mocks/with-server 1234 (fn [{:keys [body]}] {:status 200 :body body})
    (let [{:keys [status body]}
          (request {:method :post
                    :url "http://localhost:1234"
                    :body "abc"
                    :response-body-decoder :string})]
      (is (= 200 status))
      (is (= "abc" body)))

    (let [{:keys [status body]}
          (request {:method :post
                    :url "http://localhost:1234"
                    :response-body-decoder :string})]
      (is (= 200 status))
      (is (= "" body)))

    (let [{:keys [status body]}
          (request {:method :post
                    :url "http://localhost:1234"
                    :response-body-decoder :byte-stream})]
      (is (= 200 status))
      (is (= "" (slurp body))))))

;; #_(deftest test-post-form-params
;;     (mocks/with-server 1234 (fn [{:keys [body]}] {:status 200 :body body})
;;       (let [{:keys [status body]}
;;             (request {:method :post
;;                       :url "http://localhost:1234"}
;;                      :response-body-decoder :string)]
;;         (is (= 200 status))
;;         (is (= "a=1" body)))

;;       (let [{:keys [status body]}
;;             (request {:method :post
;;                       :url "http://localhost:1234"
;;                       :s-exp.ok-http.response/body-handler :string
;;                       :form-params {}})]
;;         (is (= 200 status))
;;         (is (= "" body)))))

#_(deftest test-post-form-params+body
    (mocks/with-server 1234 (fn [{:keys [body]}] {:status 200 :body body})
      (let [{:keys [status body]}
            (request {:method :post
                      :url "http://localhost:1234"
                      :s-exp.ok-http.response/body-handler :string
                      :form-params {:a 1}
                      :body "a"})]
        (is (= 200 status))
        (is (= "a" body) "body always takes over if specified"))

      (let [{:keys [status body]}
            (request {:method :post
                      :url "http://localhost:1234"
                      :s-exp.ok-http.response/body-handler :string
                      :form-params {}
                      :body "a"})]
        (is (= 200 status))
        (is (= "a" body)))))

(deftest test-error-handling
  (mocks/with-server 1234 (constantly {:status 400
                                       :body "Invalid"})
    (ex/try+
      (request {:method :get
                :url "http://localhost:1234"
                :response-body-decoder :string})
      (catch :exoscale.ex/incorrect {{:keys [status body]} :response}
        (is (= status 400))
        (is (= body "Invalid"))))))

(deftest test-response-body-read-timeout
  (binding [*client-opts* {:read-timeout 2}]
    (is (thrown? java.io.IOException
                 (request {:method :get
                           :url large-file
                           :response-body-decoder :string}))
        "when we try to realize we will get the actual exception"))
  (binding [*client-opts* {:read-timeout 10}]
    (is (thrown? java.io.IOException
                 (-> (request {:method :get
                               :url large-file})
                     :body
                     slurp))
        "input stream will just close in that case, HttpReadTimeoutException will be in cause, IOE is root"))
  (mocks/with-server 1234 (constantly {:status 200
                                       :body "ok"})
    (binding [*client-opts* {:read-timeout 3000}]
      (is (seq (slurp (:body (request {:method :get
                                       :url "http://localhost:1234"}))))
          "we got content before timeout"))))
