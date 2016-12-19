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

(def ^:const default-fraction-denominator 16)

(def ^:const foot-inch-regex
  "Regex that recognizes 'feet inch fraction' numbers, eg: '10 3 5/8'"
  #?(:clj
     #"(?x)
  ^\s*                    # optional space at beginning
  (\d+(?:\.\d*)?)         # capture *required* feet with optional decimal digits
  (?:\s+                  # spaces separator
     (\d+(?:\.\d*)?)      # capture optional feet with optional decimal digits
     (?:\s+(\d+)[/](\d+))? # capture optional fraction numerator and denominator
    )?                    # inches and fraction are optional
    \s*$                  # optional space at end"
     :cljs ;; same as :clj but without the "extended formatting"
     ;; BUG: cljs bug with '\/' in clj becoming '\\/' in js
     ;; #"^\s*(\d+(?:\.\d*)?)(?:\s+(\d+(?:\.\d*)?)(?:\s+(\d+)\/(\d+))?)?\s*$"
     #"^\s*(\d+(?:\.\d*)?)(?:\s+(\d+(?:\.\d*)?)(?:\s+(\d+)[/](\d+))?)?\s*$"
     )
  )

(defn parse-float
  "Parse a string into a floating point number"
  [s]
  (if s
    (#?(:clj Double/parseDouble :cljs js/parseFloat) s))
  )

(defn parse-foot-inch
  "Parse a 'foot inch fraction' string and return either nil if the
  string cannot be parsed or a vector of four floats (a foot-inch-vector)"
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

(defn to-inches
  "Convert a foot-inch-vector into decimal inches"
  [[feet inches numerator denominator]]
  {:pre [(number? feet)]}
  (+
    (* feet 12.0)
    (if inches inches 0.0)
    (if (and numerator denominator (not (zero? denominator))) (/ numerator denominator 1.0) 0.0)
    )
  )

(defn to-feet
  "Convert a foot-inch-vector into decimal feet"
  [fiv]
  (/ (to-inches fiv) 12.0)
  )

;; almost verbatim FROM: https://rosettacode.org/wiki/Greatest_common_divisor#Clojure
(defn gcd-pair
  "(gcd a b) computes the greatest common divisor of a and b."
  [[a b] :as pair]
  (if (zero? b)
    pair
    (recur [b (mod a b)])))

;; TODO: cannot use rationals in CLJS
(defn to-fractional-inches
  "Converts decimal inches to whole inches and an upper and lower rational with an error ratio"
  ([inches] (to-fractional-inches inches default-fraction-denominator))
  ([inches denom]
   {:pre [(<= 0.0 inches) (<= 1 denom) (integer? denom)]}
   (let [
         n (int inches)
         frac (- inches n)
         low (int (* frac denom))
         hi (inc low)
         error (- (* frac denom) low)
         ]
     [n (/ low denom) (/ hi denom) error]
     )
    )
  )

(defn divide-by
  "split the first number in a vector into a quotient and remainder"
  [[dividend & rest] divisor]
  (let [
        quotient (int (/ dividend divisor))
        remainder (- dividend (* quotient divisor))
        ]
    (into [quotient remainder] rest)
    )
  )

(defn convert
  "Convert an input string into feet inches and fractions of an inch"
  ([input-string] (convert input-string default-fraction-denominator))
  ([input-string denom]
   (if-let [fiv (parse-foot-inch input-string)]
     {
      :feet            (to-feet fiv)
      :inches          (to-inches fiv)
      :inches-fraction (to-fractional-inches (to-inches fiv) denom)
      :feet-inches-fraction (divide-by (to-fractional-inches (to-inches fiv) denom) 12)
      }
     )
    )
  )

#?(:clj
   (defn -main [& args]
     "simple REPL for foot-inch conversion"
     (let [denom! (volatile! default-fraction-denominator)]
       (do
         (println "\nEnter a foot-inch-fraction string or 'quit' ...\n")
         (loop [input (read-line)]
           (when (not (= "quit" input))
             (if (= "/" (subs input 0 1))
               (vreset! denom! (read-string (subs input 1)))
               (println "/" @denom! " => " (convert input @denom!) "\n")
               )
             (recur (read-line))
             )
           )
         )
       )
     )
   )


