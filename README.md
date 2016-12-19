----
**WARNING:** this library is currently under construction
----

# foot-inch-converter 

Simple library to convert between fractional and decimal foot-inch measurements.


## Usage

### First, **require** the library
```clojure
(require '[foot-inch-converter.core :as conv])
```

### parse a foot-inch text string
```clojure
(def conv-vec (conv/parse-foot-inch "1.23 4.56 3/4"))
(assert (= conv-vec [1.23 4.56 3.0 4.0]))
```

**conv/parse-foot-inch** takes a string with one to three numbers:
 1. **feet** (required) as either a whole or decimal number
 2. **inches** (optional) as either a whole or decimal number
 3. **fraction** (optional) as two whole numbers separated by a forward slash character

Examples of input strings:

|Input |Comment |
| ------|--------------------|
|"1.233" |decimal feet |
|"0 3.445" |decimal inches |
|"5 6 3/4" |feet inches fraction |
|"0 0 3/64" |fraction of an inch |


The returned foot-inch vector contains 4 parsed floating point numbers:
 0. feet
 1. nil or inches
 2. nil or inch fraction numerator
 3. nil or inch fraction denominator 

### Convert the foot-inch vector

#### To decimal feet

```clojure
(assert
   (= 1.5
     (conv/to-feet (conv/parse-foot-inch "1 6"))
   ))
```

#### To decimal inches

```clojure
(assert
   (= 18.0
     (conv/to-inches (conv/parse-foot-inch "1 6"))
   ))
```


## Links

 * [TDD in ClojureScript](https://8thlight.com/blog/eric-smith/2016/10/05/a-testable-clojurescript-setup.html)
 * [doo:](https://github.com/bensu/doo) A library and Leiningen plugin to run cljs.test in many JS environments.
 
## License

Copyright © 2016-2017 Rodney Maxwell

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
