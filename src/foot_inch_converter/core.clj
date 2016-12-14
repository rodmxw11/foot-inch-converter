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
