;; Copyright (c) 2016-2018 Rodney Maxwell <contact@unitsapps.com>
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

(ns foot-inch-converter.core
  (:require clojure.string))

(def ^:const default-fraction-denominator 16)

(def ^:const meters-per-inch
  "Number of meters per inch"
  (/ 25.4 1000))

(def  ^:const zero-ratio
  "A standard representation of any zero ratio  (0/N)==[0 N]; simplifies testing!"
  [0 1])

(def ^:const inches-only-regex
  "Regex that recognizes string that starts with an inches expression:
      '3/4', '4 3/4', '4.332 in', '4.334 4/9'
      "
  #"^\s*((\d+[/]\d+)|(\d+(\.\d*)?(\s+(\d+)[/](\d+))?\s*(i|in|\"|inch|inches))|(\d+(\.\d*)?(\s+(\d+)[/](\d+)))).*$"
  )

(def ^:const foot-fraction-regex
  "Detect something like '4.33 ft 4/3 inches"
  #"^\s*\d+(\.\d*)?\s*(f|ft|'|foot|feet)\s*\d+[/]\d+.*$"
  )

(def ^:const feet-inches-regex
  "Regex that recognizes numbers in a 'feet inches fraction' string, eg: '10 3 5/8'"
  #?(:clj
     #"(?x)
  ^\s*                    # optional space at beginning
  (\d+(?:\.\d*)?)?         # capture *required* feet with optional decimal digits
  (?:\s*(?:f|ft|'|foot|feet))?          # optional ft
  (?:\s*                  # spaces separator
     (\d+(?:\.\d*)?)      # capture optional inches with optional decimal digits
     (?:\s+(\d+)[/](\d+))? # capture optional inch fraction numerator and denominator
       (?:\s*(?:i|in|\"|inch|inches))? # optional inches
    )?                    # inches and fraction are optional
    \s*$                  # optional space at end"
     :cljs ;; same as :clj but without the "extended formatting"
     ;; BUG: cljs bug with '\/' in clj becoming '\\/' in js
     ;; #"^\s*(\d+(?:\.\d*)?)?(?:\s+(\d+(?:\.\d*)?)(?:\s+(\d+)\/(\d+))?)?\s*$"
     #"^\s*(\d+(?:\.\d*)?)?(?:\s*(?:f|ft|'|foot|feet))?(?:\s*(\d+(?:\.\d*)?)(?:\s+(\d+)[/](\d+))?(?:\s*(?:i|in|\"|inch|inches))?)?\s*$"
     )
  )

(defn parse-float
  "Parse a string into a floating point number"
  [s]
  (if s
    (#?(:clj Double/parseDouble :cljs js/parseFloat) s))
  )

(defn parse-feet-inches
  "Parse a 'feet inches fraction' string and return either nil if the
  string cannot be parsed or a vector of four floats (a feet-inches-vector)"
  [input]
  (let [
        ;; feet-inches-regex does not recognize a pure fraction, eg "3/4"
        input-temp (cond
                     (re-matches inches-only-regex input) ;; if input starts with a fraction
                     (str "0 " input)                       ;; then append "0 " to input string because "0 3/4" is inches

                     (re-matches foot-fraction-regex input)
                     (clojure.string/replace input #"f|ft|'|foot|feet" "ft 0 ")

                     :else input                                  ;; else input is OK as is
                     )
        ]
  (if-let [v (re-matches feet-inches-regex input-temp)]
    (->>
      v
      (drop 1)  ;; drop the complete match
      (map #(if % (parse-float %)))
      vec
      )
    ))
  )

(defn feet-inches-vector->meters
  "Convert a feet-inches-vector into meters"
  [[feet inches numerator denominator]]
  {:pre [(number? feet)]}
  (*
    meters-per-inch
    (+
      (* feet 12.0)
      (if inches inches 0.0)
      (if (and numerator denominator (not (zero? denominator))) (/ numerator denominator 1.0) 0.0)
      )
    )
  )

(defn inches->meters
  "Convert decimal inches to meters"
  [inches]
  (feet-inches-vector->meters [0 inches])
  )

(defn feet->meters
  "Convert decimal feet to meters"
  [feet]
  (feet-inches-vector->meters [feet])
  )

(defn parse-feet-inches->meters
  "Parse a 'feet inches fraction' string and return either a meters value
  or nil if the input string cannot be parsed"
  [input]
  (if-let [fiv (parse-feet-inches input)]
    (feet-inches-vector->meters fiv)
    )
  )

(defn meters->inches
  "Converts meters into decimal inches"
  [meters]
  {:pre [(number? meters)]}
  (/ meters meters-per-inch)
  )

(defn meters->feet
  "Convert meters into decimal feet"
  [meters]
  {:pre [(number? meters)]}
  (/ (meters->inches meters) 12.0)
  )

;; verbatim FROM: https://rosettacode.org/wiki/Greatest_common_divisor#Clojure
(defn gcd
  "(gcd a b) computes the greatest common divisor of a and b."
  [a b]
  (if (zero? b)
    a
    (recur b (mod a b))))

(defn simplify-ratio
  "Simplify a ratio"
  [[numerator denominator :as ratio]]
  {:pre [(not (zero? denominator))]}
  (let [divisor (gcd numerator denominator)]
    (case divisor
      0 zero-ratio
      1 ratio
      [(/ numerator divisor) (/ denominator divisor)]
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

(defn meters->fractional-inches
  "Converts meters to whole inches and an upper and lower fraction with an error ratio"
  ([meters] (meters->fractional-inches meters default-fraction-denominator))
  ([meters fraction-denominator]
   {:pre [(<= 0.0 meters) (<= 1 fraction-denominator) (integer? fraction-denominator)]}
   (let [
         inches-decimal (meters->inches meters)
         n (int inches-decimal)
         frac (- inches-decimal n)
         low (int (* frac fraction-denominator))
         high (inc low)
         error (- (* frac fraction-denominator) low)
         low-ratio (simplify-ratio [low fraction-denominator])
         high-ratio (simplify-ratio [high fraction-denominator])
         ]
     [n low-ratio high-ratio error]
     )
    )
  )

(defn meters->fractional-feet
  "returns whole feet and inches along with fractional inch information"
  ([meters] (meters->fractional-feet meters default-fraction-denominator))
  ([meters fraction-denominator]
   (divide-by (meters->fractional-inches meters fraction-denominator) 12)
    )
  )

(defn repl-parse-input->meters
  "Parse repl input strings as 'mm', 'm', or feet inches fraction"
  [input-string]
  (try
    (cond

      ;; "mm 2.223" -- input millimeters
      (and
        (< 2 (count input-string))
        (= "mm" (subs input-string 0 2)))
      (/
        (parse-float (subs input-string 2))
        1000.0
        )

      ;; "m  33.332" -- inputs meters
      (and
        (< 1 (count input-string))
        (= "m" (subs input-string 0 1)))
      (parse-float (subs input-string 1))

      ;; else input feet inches fraction
      :else (parse-feet-inches->meters input-string)
      ) ;;endcond
    (catch #?(
              :clj Exception
              :cljs :default
              )
           error
         nil ;; return nil if parse-float throws exception
      ) ;;endcatch
    ) ;;endtry
  )

(defn convert
  "Convert an input string into feet inches and fractions of an inch"
  ([input-string] (convert input-string default-fraction-denominator))
  ([input-string denom]
   (if-let [meters (repl-parse-input->meters input-string)]
     {
      :meters          meters
      :feet            (meters->feet meters)
      :inches          (meters->inches meters)
      :inches-fraction (meters->fractional-inches meters denom)
      :feet-fraction   (meters->fractional-feet meters denom)
      }
     )
    )
  )

#?(:clj
   (defn -main [& args]
     "simple REPL for foot-inch conversion"
     (let [denom! (volatile! default-fraction-denominator)]
       (do
         (println "\nEnter a foot-inch-fraction strings or 'quit' ...\n")
         (loop [input (read-line)]
           (when (not (= "quit" input))
             (if (= "/" (subs input 0 1))
               (do
                 (vreset! denom! (read-string (subs input 1)))
                 (println "Fractional denominator now set to: " @denom! "\n")
                 )
               (println "1/" @denom! "=>" (convert input @denom!) "\n")
               )
             (recur (read-line))
             ) ;;endwhen
           ) ;;endloop
         ) ;;endo
       ) ;;endlet
     ) ;;endefn
   ) ;;end#?


