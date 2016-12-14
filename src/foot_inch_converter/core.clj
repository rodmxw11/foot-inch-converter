(ns foot-inch-converter.core)

(def foot-inch-regex
;;  #"^\s*(\d+)(\.\d*)?(?:\s+(\d+)(\.\d*)?(?:\s+(\d+)\/(\d+))?)?\s*$"
  #"(?x)^\s*
  (\d+(?:\.\d*)?)
  (?:\s+
     (\d+(?:\.\d*)?)
     (?:\s+(\d+)\/(\d+))?
    )?"
  )

(def epsilon 0.0000001)

(defn float= [f1 f2]
  (case [(nil? f1) (nil? f2)]
    ([true true]) true
    ([true false] [false true]) false
    (<= (Math/abs (- f1 f2)) epsilon)
    )
  )

(defn vfloat= [v1 v2]
  (every? true?
          (map float= v1 v2)
          )
  )

(defn parse-foot-inch [s]
  (if-let [v (re-matches foot-inch-regex s)]
    (->>
      v
      (drop 1)
      (map #(if % (Float/parseFloat %)))
      vec
      )
    )
  )

(defn to-feet [[feet inches num denom]]
  (+
    feet
    (if inches (/ inches 12.0) 0.0)
    (if (and num denom (not (zero? denom))) (/ num denom 12.0) 0.0)
    )
  )

(defn to-inches [[feet inches num denom]]
  (+
    (* feet 12.0)
    (if inches inches 0.0)
    (if (and num denom (not (zero? denom))) (/ num denom 1.0) 0.0)
    )
  )

