(ns foot-inch-converter.core)

(def ^:const foot-inch-regex
  "Regex that recognizes 'feet inch fraction' numbers, eg: '10 3 5/8'"
  #"(?x)^\s*
  (\d+(?:\.\d*)?)
  (?:\s+
     (\d+(?:\.\d*)?)
     (?:\s+(\d+)\/(\d+))?
    )?"
  )

(def ^:const epsilon
  "Tolerance for comparing floating point numbers"
  0.0000001)

(defn float-abs
  "Absolute value of a float"
  [f1]
  (if (>= 0.0 f1) f1 (- f1) )
  )

(defn float=
  "Compare two floats for equality within a tolerance of epsilon"
  [f1 f2]
  (case [(nil? f1) (nil? f2)]
    ([true true]) true
    ([true false] [false true]) false
    (<= (float-abs (- f1 f2)) epsilon)
    )
  )

(defn vfloat=
  "Compares two vectors of floats for equality using fuzzy compare"
  [v1 v2]
  (every? true?
          (map float= v1 v2)
          )
  )

(defn parse-float
  "Parse a string into a floating point number"
  [s]
  (Float/parseFloat s))

(defn parse-foot-inch
  "Parse a 'foot inch fraction' string and return either nil if the
  string cannot be parsed or a vector of four floats"
  [s]
  (if-let [v (re-matches foot-inch-regex s)]
    (->>
      v
      (drop 1)
      (map #(if % (parse-float %)))
      vec
      )
    )
  )

(defn to-feet
  "Convert a vector of four floats into fractional feet"
  [[feet inches num denom]]
  (+
    feet
    (if inches (/ inches 12.0) 0.0)
    (if (and num denom (not (zero? denom))) (/ num denom 12.0) 0.0)
    )
  )

(defn to-inches
  "Convert a vector of floats into fractional inches"
  [[feet inches num denom]]
  (+
    (* feet 12.0)
    (if inches inches 0.0)
    (if (and num denom (not (zero? denom))) (/ num denom 1.0) 0.0)
    )
  )

(defn to-fractional-inch
  "Returns an upper and lower rational with an error"
  [f denom]
  {:pre [(<= 0.0 f) (<= 1 denom) (integer? denom)]}
  (let [
        n (int f)
        frac (- f n)
        low (int (* frac denom))
        hi (inc low)
        error (- (* frac denom) low)
        ]
    [n (/ low denom) (/ hi denom) error]
    )
  )



