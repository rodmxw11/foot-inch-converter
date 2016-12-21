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
(assert (=
   (conv/feet->meters 1.375) ;; convert 1.375 feet
   0.4191                    ;; to meters
 ))
```

#### Decimal inches to meters

```clojure
(assert (=
   (conv/inches->meters 1.375) ;; convert 1.375 inches
   0.034925                    ;; to meters, approximately 35 mm
))
```
#### parse a feet-inches-fraction text string to meters
```clojure
(assert (=
   (conv/parse-feet-inches->meters "1.23 4.56 3/4")
                               ;; convert 1.23 feet 4.56 3/4 inces
   0.509778                    ;; to meters, approximately 510 mm
))
```

**parse-feet-inches->meters** takes a string with one to three numbers:
 1. **feet** (required) as either a whole or decimal number
 2. **inches** (optional) as either a whole or decimal number
 3. **fraction** (optional) as two whole numbers separated by a forward slash character

Examples of valid input strings:

|Input |Comment |
| ------|--------------------|
|"1.233" |only decimal feet |
|"0 3.445" |only decimal inches |
|"5 6 3/4" |feet inches fraction |
|"0 0 3/64" |only a fraction of an inch |

Note: There is a **feet-inches-regex** regular expression 
available that determines valid input strings.

#### Convert a feet-inches-fraction vector to meters

```clojure
(assert (=
   (conv/feet-inches-vector->meters [1.23 4.56 3.0 4.0])
                               ;; convert 1.23 feet 4.56 3/4 inces
   0.509778                    ;; to meters, approximately 510 mm                    
))
```

Internally, this library first parses a
feet inches fraction text string into a 
feet-inches-fraction vector:

|Text |Equivalent vector |
| ------|--------------------|
|"1.233" |[1.233 nil nil nil] |
|"0 3.445" |[0.0 3.445 nil nil] |
|"5 6 3/4" |[5.0 6.0 3.0 4.0 |
|"0 0 3/64" |[0.0 0.0 3.0 64.0] |

### Convert meters to feet and inches 

#### Meters to decimal feet

```clojure
(assert (= 
   (conv/meters->feet 419.1e-3)  ;; Convert 419.1 mm
   1.375                       ;; to decimal feet
))
```

#### Meters to decimal inches

```clojure
(assert (= 
   (conv/meters->inches 34.925e-3) ;; Convert 34.925 mm
   1.375                          ;; to decimal inches
))
```

#### Meters to whole inches with fractional inch information

```clojure
(def meters-value (conv/inches->meters 8.123456)) ;; approximately 206 mm
(assert (= 
    (conv/meters->fractional-inches meters-value 10) ;; convert to tenths of an inch
    [8 [1 10] [1 5] 0.23455999999999122]
    ;; actual meters length is 23% between 8 1/10 and 8 1/5 inches
))  
(assert (= 
    (conv/meters->fractional-inches meters-value) ;; convert to default sixteenths of an inch
    [8 [1 16] [1 8] 0.975295999999986]
    ;; actual meters length is 97.5% between 8 1/16 and 8 1/8 inches
))   
```

**meters->fractional-inches** takes two parameters:
 1. meters
 2. (optional) The numerator of the desired inch fractions.  
 Defaults to 16, giving sixteenths of an inch.
 
The vector returned by **meters->fractional-inches** has the following 4 elements:
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
 
#### Meters to whole feet with fractional inch information

```clojure
(def meters-value (conv/inches->meters 32.123456)) ;; approximately 816 mm
(assert (= 
    (conv/meters->fractional-feet meters-value 10) ;; convert to tenths of an inch
    [2 8 [1 10] [1 5] 0.23455999999997346]
    ;; actual meters length is 23% between [2 feet 8 1/10 inches] and [2 feet 8 1/5 inches]
))  
(assert (= 
    (conv/meters->fractional-feet meters-value) ;; convert to default sixteenths of an inch
    [2 8 [1 16] [1 8] 0.9752959999999575]
    ;; actual meters length is 97.5% between [2 feet 8 1/16 inches] and [2 feet 8 1/8 inches]
))   
```

**meters->fractional-feet** takes two parameters:
 1. meters
 2. (optional) The numerator of the desired inch fractions.  
 Defaults to 16, giving sixteenths of an inch.


The vector returned by **meters->fractional-feet** has the following 5 elements:
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
