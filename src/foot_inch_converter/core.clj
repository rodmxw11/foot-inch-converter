;; Copyright (c) 2016-2017 Rodney Maxwell <contact@unitsapps.com>
;; All rights reserved.
;;
;; Redistribution and use in source and binary forms, with or without
;; modification, are permitted provided that the following conditions are met:
;;
;; * Redistributions of source code must retain the above copyright notice, this
;;   list of conditions and the following disclaimer.
;;
;; * Redistributions in binary form must reproduce the above copyright notice,
;;   this list of conditions and the following disclaimer in the documentation
;;   and/or other materials provided with the distribution.
;;
;; THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
;; AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
;; IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
;; DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
;; FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
;; DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
;; SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
;; CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
;; OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
;; OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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

(defn- parse-float
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
  "Convert a vector of four floats into decimal feet"
  [[feet inches num denom]]
  (+
    feet
    (if inches (/ inches 12.0) 0.0)
    (if (and num denom (not (zero? denom))) (/ num denom 12.0) 0.0)
    )
  )

(defn to-inches
  "Convert a vector of floats into decimal inches"
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

(defn to-feet-fractional-inch
  [f denom]
  (let [[inches hi low error] (to-fractional-inch f denom)]
    [()]
    ))


(defn -main[& args]
  (do
    (println "Enter a number")
    (let [ans (read-line)]
      (println "You entered: " ans))
    )
  )


