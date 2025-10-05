# ok-http

Very minimal http client based on ok-http


## Usage

RING1 style api

```clj
(require '[s-exp.ok-http :as http])

;; create a new client instance
(def client (http/client {}))

(http/request client {:method :get
                      :url "https://httpbin.org/get"})
```

## Examples

### 1. Simple GET request
```clojure
(http/request {:method :get
               :url "https://httpbin.org/get"})
```

### 2. Using a custom client
```clojure
(def my-client (http/client {:read-timeout 1000}))
(http/request my-client {:method :get :url "https://httpbin.org/get"})
```

### 3. POST with JSON body & headers
```clojure
(http/request {:method :post
               :url "https://httpbin.org/post"
               :headers {"Content-Type" "application/json"}
               :body "{\"foo\": \"bar\"}"})
```

### 4. Add query parameters
```clojure
(http/request {:method :get
               :url "https://httpbin.org/get"
               :query-params {:foo "bar" :baz [1 2]}})
```

### 5. Response body decoder
```clojure
;; default (:byte-stream) returns InputStream (MUST consume fully)
(def resp (http/request {:url "https://httpbin.org/get"}))
(slurp (:body resp))

;; To force eager string decoding:
(def resp (http/request {:url "https://httpbin.org/get"
                         :response-body-decoder :string}))
(:body resp) ;=> ...contents as string
```

### 6. Error handling
```clojure
;; throw on 4xx/5xx by default:
(try
  (http/request {:url "https://httpbin.org/status/404"})
  (catch Exception e
    (println "Error:" (.getMessage e))))

;; to return the response (even if error status):
(http/request {:url "https://httpbin.org/status/404"
               :throw-on-error false})
```

## Documentation

[API docs](API.md)

## Installation

ok-http is [available on Clojars](https://clojars.org/com.s-exp/ok-http).

Add this to your dependencies:

[![Clojars Project](https://img.shields.io/clojars/v/com.s-exp/ok-http.svg?include_prereleases)](https://clojars.org/com.s-exp/ok-http)

## License

Copyright © 2025 [S-Exp](https://s-exp.com)

Distributed under the Eclipse Public License version 1.0.
