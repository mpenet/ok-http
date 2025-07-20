# Table of contents
-  [`exoscale.ok-http`](#exoscale.ok-http) 
    -  [`client`](#exoscale.ok-http/client) - Creates new <code>client</code> from <code>opts</code> or creates a new <code>client</code> from a <code>client</code>, copying it's settings and extra <code>opts</code>.
    -  [`client-options`](#exoscale.ok-http/client-options)
    -  [`request`](#exoscale.ok-http/request) - Performs a http request via <code>client</code>, using <code>request-map</code> as payload.
    -  [`request-options`](#exoscale.ok-http/request-options)

-----
# <a name="exoscale.ok-http">exoscale.ok-http</a>






## <a name="exoscale.ok-http/client">`client`</a><a name="exoscale.ok-http/client"></a>
``` clojure

(client client opts)
(client opts)
```

Creates new [[[`client`](#exoscale.ok-http/client)](#exoscale.ok-http/client)](#exoscale.ok-http/client) from `opts` or creates a new [[[`client`](#exoscale.ok-http/client)](#exoscale.ok-http/client)](#exoscale.ok-http/client) from a [[[`client`](#exoscale.ok-http/client)](#exoscale.ok-http/client)](#exoscale.ok-http/client),
  copying it's settings and extra `opts`. Returned client can be used with
  [`request`](#exoscale.ok-http/request).

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
  * `:add-interceptors`
<p><sub><a href="https://github.com/exoscale/ok-http/blob/main/src/exoscale/ok_http.clj#L13-L42">Source</a></sub></p>

## <a name="exoscale.ok-http/client-options">`client-options`</a><a name="exoscale.ok-http/client-options"></a>



<p><sub><a href="https://github.com/exoscale/ok-http/blob/main/src/exoscale/ok_http.clj#L11-L11">Source</a></sub></p>

## <a name="exoscale.ok-http/request">`request`</a><a name="exoscale.ok-http/request"></a>
``` clojure

(request client request-map)
```

Performs a http request via [`client`](#exoscale.ok-http/client), using `request-map` as payload.
   Returns a ring response map

  Options:
  * `:throw-on-error` - defaults to true

  * `:response-body-decoder` - `:byte-stream` (default, ensure it's consumed!),
  `:string`, `:bytes`, `:input-stream` (safe, eager, copy)
<p><sub><a href="https://github.com/exoscale/ok-http/blob/main/src/exoscale/ok_http.clj#L48-L63">Source</a></sub></p>

## <a name="exoscale.ok-http/request-options">`request-options`</a><a name="exoscale.ok-http/request-options"></a>



<p><sub><a href="https://github.com/exoscale/ok-http/blob/main/src/exoscale/ok_http.clj#L44-L46">Source</a></sub></p>
