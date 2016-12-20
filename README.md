----
**WARNING:** this library is currently under construction
----

# foot-inch-converter 

Simple library to convert between fractional and decimal foot-inch measurements.


## Usage

### **require** the library
```clojure
(ns my.namespace
   (:require [foot-inch-converter.core :as conv])
  )
```

### Convert feet and inches to meters

#### Decimal feet to meters

```clojure
(assert
   (= 123233
     (conv/feet->meters 1.0))
```
#### parse a feet-inches-fraction text string into meters
```clojure
(assert
   (= 0.509778 ;; meters
      (conv/parse-feet-inches->meters "1.23 4.56 3/4")
      ))
```

**parse-feet-inches->meters** takes a string with one to three numbers:
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

### Convert meters to feet and inches ...

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

### To whole inches with fractional inch information

```clojure
(assert
   (= [8 [1 10] [1 5] 0.23456]
      (conv/to-inches-fractional 
         (conv/parse-foot-inch "0 8.123456")
         10
      )
   )
)
```

**to-inches-fractional** takes two parameters:
 1. The foot-inch vector returned from **parse-foot-inch**
 2. (optional) The numerator of the desired inch fractions.  
 Defaults to 16, giving sixteenths of an inch.
 
The vector returned by **to-inches-fractional** has the following 4 elements:
 1. the whole number of inches
 2. A *lower fractional bound* for the fractional inch, represented as a 
 vector containing a numerator denominator pair.  The actual input length
 will be greater than or equal to this fraction. 
 3. An *upper fractional bound* for the fractional inch as a vector pair.
 The actual input length will be less than or equal to this fraction.
 4. An *interpolation factor* that indicates where the actual input
 length occurs between the lower and upper fractional bounds. Always between
 zero and one.  0.0 means that the actual length is exactly the lower bound
 whereas 0.99999 would mean that the actual length is extremely close
 to the upper bounds.  0.5 would mean that the actual length is exactly
 halfway between the lower and upper bounds.
 
### To whole feet and inches with fractional inch information

```clojure
(assert
   (= [2 8 [1 10] [1 5] 0.23456]
      (conv/to-feet-fractional 
         (conv/parse-foot-inch "0 32.123456")
         10
      )
   )
)
```

**to-feet-fractional** takes two parameters:
 1. The foot-inch vector returned from **parse-foot-inch**
 2. (optional) The numerator of the desired inch fractions.  
 Defaults to 16, giving sixteenths of an inch.


The vector returned by **to-feet-fractional** has the following 5 elements:
 1. The whole number of feet
 2. Whole inches
 3. lower fractional bound
 4. upper fractional bound
 5. interpolation factor
 
## Links

 * [TDD in ClojureScript](https://8thlight.com/blog/eric-smith/2016/10/05/a-testable-clojurescript-setup.html)
 * [doo:](https://github.com/bensu/doo) A library and Leiningen plugin to run cljs.test in many JS environments.
 
## License

Copyright © 2016-2017 Rodney Maxwell

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
