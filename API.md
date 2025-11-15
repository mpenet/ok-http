# Table of contents
-  [`s-exp.ok-http`](#s-exp.ok-http) 
    -  [`client`](#s-exp.ok-http/client) - Creates new <code>client</code> from <code>opts</code> or creates a new <code>client</code> from a <code>client</code>, copying it's settings and extra <code>opts</code>.
    -  [`client-options`](#s-exp.ok-http/client-options)
    -  [`def-http-method`](#s-exp.ok-http/def-http-method)
    -  [`default-client`](#s-exp.ok-http/default-client)
    -  [`request`](#s-exp.ok-http/request) - Performs an HTTP request using the provided OkHttp client and a request map.
    -  [`request-options`](#s-exp.ok-http/request-options)
    -  [`shutdown!`](#s-exp.ok-http/shutdown!) - Closes all connections and releases resources for the provided OkHttpClient instance.

-----
# <a name="s-exp.ok-http">s-exp.ok-http</a>






## <a name="s-exp.ok-http/client">`client`</a><a name="s-exp.ok-http/client"></a>
``` clojure

(client client opts)
(client opts)
(client)
```

Creates new [[[`client`](#s-exp.ok-http/client)](#s-exp.ok-http/client)](#s-exp.ok-http/client) from `opts` or creates a new [[[`client`](#s-exp.ok-http/client)](#s-exp.ok-http/client)](#s-exp.ok-http/client) from a [[[`client`](#s-exp.ok-http/client)](#s-exp.ok-http/client)](#s-exp.ok-http/client),
  copying it's settings and extra `opts`. Returned client can be used with
  [`request`](#s-exp.ok-http/request).

  Options:

  * `:call-timeout` (in ms)
  * `:read-timeout` (in ms)
  * `:write-timeout` (in ms)
  * `:connect-timeout` (in ms)
  * `:protocols` - one of "http/1.0", "http/1.1", "h2", "h2_prior_knowledge", "quic", "spdy/3.1", "h3"
  * `:dispatcher` : map of `:executor`, `:max-requests`, `:max-requests-per-host`
  * `:connection-pool` : map of `:max-idle-connections`, `:keepalive-duration` (in ms)
  * `:retry-on-connection-failure`
  * `:follow-redirects`
  * `:ssl-socket-factory`
  * `:follow-ssl-redirects`
  * `:add-network-interceptors`
  * `:brotli` : boolean
  * `:add-interceptors`
<p><sub><a href="https://github.com/mpenet/ok-http/blob/main/src/s_exp/ok_http.clj#L15-L47">Source</a></sub></p>

## <a name="s-exp.ok-http/client-options">`client-options`</a><a name="s-exp.ok-http/client-options"></a>



<p><sub><a href="https://github.com/mpenet/ok-http/blob/main/src/s_exp/ok_http.clj#L13-L13">Source</a></sub></p>

## <a name="s-exp.ok-http/def-http-method">`def-http-method`</a><a name="s-exp.ok-http/def-http-method"></a>
``` clojure

(def-http-method method)
```
Function.
<p><sub><a href="https://github.com/mpenet/ok-http/blob/main/src/s_exp/ok_http.clj#L90-L104">Source</a></sub></p>

## <a name="s-exp.ok-http/default-client">`default-client`</a><a name="s-exp.ok-http/default-client"></a>



<p><sub><a href="https://github.com/mpenet/ok-http/blob/main/src/s_exp/ok_http.clj#L49-L49">Source</a></sub></p>

## <a name="s-exp.ok-http/request">`request`</a><a name="s-exp.ok-http/request"></a>
``` clojure

(request request-map)
(request client request-map)
```

Performs an HTTP request using the provided OkHttp client and a request map.
  Returns a Ring-style response map containing keys such as :status, :headers, :body.

  Arguments:
  * [`client`](#s-exp.ok-http/client) (okhttp3.OkHttpClient): Optional, if not provided the default client is used.
  * `request-map` (map): Map describing request parameters.

  Supported keys in `request-map`:
  * `:method` - HTTP method keyword (:get, :post, etc), defaults to :get
  * `:url` - Absolute URL string (required)
  * `:headers` - Map of header names to values
  * `:body` - Request body (string, bytes, stream, file, etc.)
  * `:query-params` - Map of query parameters to add to URL

  Options:
  * `:throw-on-error` - If true (default), throws exception on 4xx/5xx. If false, returns response.
  * `:response-body-decoder` - Decoding strategy for :body: `:byte-stream` (default, lazy raw bytes, must be consumed), `:string`, `:bytes`, `:input-stream` (eager, safe to read/copy).

  Returns:
  * Ring response map: {:status int, :headers map, :body value}

  Example:
    (request {:method :get :url "https://httpbin.org/get"})
    (request client {:method :post :url "https://api.com" :headers {"Content-Type" "application/json"} :body "{...}"})
  
<p><sub><a href="https://github.com/mpenet/ok-http/blob/main/src/s_exp/ok_http.clj#L55-L88">Source</a></sub></p>

## <a name="s-exp.ok-http/request-options">`request-options`</a><a name="s-exp.ok-http/request-options"></a>



<p><sub><a href="https://github.com/mpenet/ok-http/blob/main/src/s_exp/ok_http.clj#L51-L53">Source</a></sub></p>

## <a name="s-exp.ok-http/shutdown!">`shutdown!`</a><a name="s-exp.ok-http/shutdown!"></a>
``` clojure

(shutdown! client)
```

Closes all connections and releases resources for the provided OkHttpClient instance.
   This will:
   - Evict all connections from the internal connection pool
   - Shutdown the underlying executor service
   - Close the cache, if present
   Call this when you are done with an OkHttpClient to avoid resource leaks.
<p><sub><a href="https://github.com/mpenet/ok-http/blob/main/src/s_exp/ok_http.clj#L116-L128">Source</a></sub></p>
