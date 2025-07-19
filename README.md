# ok-http

Very minimal http client based on ok-http


## Usage

RING1 style api

``` clj
(require '[exoscale.ok-http :as c])

;; creates a new client instance
(def c (c/client {}))

(c/request c {:method :get
              :url "http://google.com/"})
```


## Documentation

[API docs](API.md)

## Installation

ok-http is [available on Clojars](https://clojars.org/com.exoscale/ok-http).

Add this to your dependencies:

[![Clojars Project](https://img.shields.io/clojars/v/com.exoscale/ok-http.svg?include_prereleases)](https://clojars.org/com.exoscale/ok-http)

## License

Copyright © 2025 [Exoscale](https://exoscale.com)

Distributed under the Eclipse Public License version 1.0.
