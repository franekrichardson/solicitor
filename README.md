# Solicitor

Solicitor is small library for feature flags and configuration. Feature flags
are a way of controlling the availaibility of features in your application. You
can learn more about the [idea of feature flags](http://code.flickr.net/2009/12/02/flipping-out/).

Your flag names are expected to be in the form of `foo/bar/baz`. The slashes
promote namespacing and lend themselves to clever use in backends.

# Features

* Boolean flags
* Percentage change (i.e. on for 10% of users)
* Multiple backends
    - Static (for testing and stubbing)
    - HTTP paths with support for multiple, random host pools (e.g. "foo/bar" fetches a value from a example.com/foo/bar)
    - [Typesafe config](https://github.com/typesafehub/config)


# Status

Solicitor is new and likely has some flaws and missing pieces. Here are some
known TODOs:

* Caching for the HTTP backend
* Ignore comment and empty lines for HTTP backend (documentation in the files is the idea)
* Periodic reload of Typesafe config

# Goals

Solicitor's goals are to enable runtime modification of features in a backend
application or service. You might also be able to use this for front end
decisions if your application can use Scala libaries in it's templates like the
[Play Framework](http://www.playframework.com/).

With the ability to adjust your features on the fly it becomes less necessary
for you to deploy new code for small changes. In addition to just enabling or
disabling a feature you can adjust values by fetching either numeric or string
values from the backend.  Examples:

* Adusting numeric values such as cache durations or concurrency
* Disabling performance features via booleans during problems
* Fetching lists of hosts to use for connecting to other backend services

These are powerful features that, if implemented, can significantly reduce
your time to resolution for production issues by avoiding the latency and
complexity of deployments.

# Types

The content of the response is expected to be
plain text. The following rules are used to interprest the strings into Scala types:

Values | Type | Notes
-------|------|------
true, false | Boolean | Case insensitive
numbers | Double | No special cases for integers
everything else | String! | n/a

## isEnabled and isDisabled

Note that any non boolean value will be considered false by default. Values like
1, 1.0 are not "truthy" in this implementation.

# Defaults

You can also provide defaults in the event that your backend fails to retrieve
a value.

# Backends

Solicitor has pluggable backends that allow you define different ways of retrieving
values.

## HTTP

The HTTP backend uses [Spray's HTTP Client](http://spray.io/documentation/spray-can/http-client/)
to make HTTP requests to url in the form of: `host + "/" + key`.
Therefore if your `host` is `www.example.com` then a request for
`foo/bar` will result in a GET request to `http://www.example.com/foo/bar`.

### Notes

The original idea behind this backend was to expose a simple directory of files.
More specifically, a Git repository of files containing simple values to enable
a combination of simple retrieval and simple auditing of the configuration
information. Git provides history and accountability and hooks can notify
third parties of value changes.

### Example

```scala        
import solicitor.Client
import solicitor.backend.HTTP

val solicitor = new Client(
  background = new HTTP(hosts = Seq("example.com", 80))
)
```

## Static

### Example

```
import solicitor.Client
import solicitor.backend.Static

val solicitor = new Client(
  backend = new Static(Map(
    "foo" -> 1,
    "bar" -> true
    "baz" -> "gorch"
  ))
)
```

# Using Solicitor

Here are some examples of common use cases of Solicitor.

## Instantiation 

```scala
import solicitor.Client
import solicitor.backend.Static

val solicitor = new Client(
  backend = new Static(Map(
    "foo" -> "true",
    "bar" -> "0.5"
  ))
)
```

## Simple Feature Activation

```scala
if(solicitor.isEnabled("foo")) {
  // Do something!
}

if(solicitor.isDisabled("foo")) {
  // Do something!
}
```

## Percentage Chance of Activation

```scala
if(solicitor.decideEnabled("bar")) {
  // Should happen about 50% of the time!
}
```

## Raw Value Methods

```scala
val t1 = solicitor.getString("baz") // String!

// These methods default to None if unparseable. See Types above.
val t2 = solicitor.getBoolean("bar") // Option[Boolean]
val t3 = solicitor.getDouble("foo") // Option[Double]

// With Defaults
val t4 = solicitor.getBoolean("bar", Some(true)) // Option[Boolean]
val t5 = solicitor.getDouble("foo", Some(100)) // Option[Double]

```

