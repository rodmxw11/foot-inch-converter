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

(ns foot-inch-converter.core-test
  (:require #?(:cljs [cljs.test :as t]
               :clj [clojure.test :as t])
                    [foot-inch-converter.core :as conv]
                    )
  #?(:cljs
     (:require-macros [cljs.core :refer [exists?]])
     )
  )

(def ^:const epsilon
  "Tolerance for comparing floating point numbers"
  1e-9)

(t/deftest parse-float-test
  (t/is (nil? (conv/parse-float nil)))
  (t/is (= 1.5 (conv/parse-float "1.5")))
  )

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

(defn vtofloat
  "converts every element of a numeric vector to float"
  [v]
  ;; in Javascript *every* number is already a Double
      #?(
         :clj
          (->>
            v
            (map #(if % (double %)))
            vec
          )
         :cljs
         v
         )
  )

(defn vfloat=
  "Compares two vectors of floats for equality using fuzzy compare"
  [v1 v2]
  (and (= (count v1) (count v2))
       (every? true?
               (map float= (vtofloat v1) (vtofloat v2))
               )
       )
  )

(t/deftest feet-inches-regex-test
  (t/testing "foot-inch-regex basic test"
    (t/is (re-matches conv/feet-inches-regex "0"))
    (t/are [input] (re-matches conv/feet-inches-regex input)
             "0"
                   "0 in"
                   "0 inch"
                   "0\""
             "0."
             "1.2"
             "0 1"
             "0.0 1.1"
             "0 1.22"
             "0 0 1/2"
             "1.223 3.331 3/138"
                   " 3 ft"
                   "3.14in"
                   "5'10\""
             )
    ))

(t/deftest parse-feet-inches-test
  (t/testing "foot-inch-regex digit parsing"
    (t/is (vfloat= [0.0 nil nil nil] (conv/parse-feet-inches "0")))
    (t/are [input fiv] (vfloat= fiv (conv/parse-feet-inches input))
               "0" [0.0 nil nil nil]
               "0." [0.0 nil nil nil]
               "000.000" [0.0 nil nil nil]
               "1 2" [1.0 2.0 nil nil]
               "1.23 4.56 3/4" [1.23 4.56 3.0 4.0]
                       "0 3/4" [0 0 3 4]
                       "3/4" [nil 0 3 4]
                       "3/4 inches" [nil 0 3 4]
                       "5.34\"" [0 5.34 nil nil]
                       "4.56'" [4.56 nil nil nil]
                       "4.334" [4.334 nil nil nil]
                       "4.334 in" [0 4.334 nil nil]
                       "4.334 4/7" [0 4.334 4 7]
                       "4.334 4/7 inch" [0 4.334 4 7]
                    ;; TODO:   " 4.334 ft 4/7 inch" [4.334 0 4 7]
                       " 4.33 7/8" [0 4.33 7 8]
               )
    ))

(t/deftest inches->meters-test
  (t/is (float= 25.4e-3 (conv/inches->meters 1.0)))
  )

(t/deftest feet->meters-test
  (t/is (float= 0.3048 (conv/feet->meters 1.0)))
  )

(t/deftest meters->inches-test
  (t/testing "meters->inches works OK"
    (t/is (float= 12.0 (-> [1.0 nil nil nil] conv/feet-inches-vector->meters conv/meters->inches)))
    (t/are [inches fiv] (float= inches (-> fiv conv/feet-inches-vector->meters conv/meters->inches))
                1.0 [0.0 1.0 nil nil]
                0.5 [0.0 0.0 1.0 2.0]
                24.75 [1.5 6.0 3.0 4.0]
                  )
    )
  )

(t/deftest meters->feet-test
  (t/testing "meters->feet works OK"
    (t/is (float= 1.0 (-> [1.0 nil nil nil] conv/feet-inches-vector->meters conv/meters->feet)))
    (t/are [feet fiv] (float= feet (-> fiv conv/feet-inches-vector->meters conv/meters->feet))
                      1.0 [1.0 nil nil nil]
                      1.5 [1.0 6.0 nil nil]
                      1.5 [1.0 0.0 12.0 2.0]
                      2.0 [1.0 6.0 12.0 2.0]
                      0.0 [0.0 nil nil nil]
                      )
    ))

(t/deftest simplify-ratio-test
  (t/is (= [1 2] (conv/simplify-ratio [2 4])))
  (t/is (= conv/zero-ratio (conv/simplify-ratio [0 22245])))
  (t/are [expected ratio]
    (let [[rnumer rdenom] ratio
          [enumer edenom] expected
          ]
      (and
        (= [enumer edenom] (conv/simplify-ratio [rnumer rdenom]))
        (= [edenom enumer] (conv/simplify-ratio [rdenom rnumer]))
        ))
       [1 1]  [1 1]
       [5 1]  [25 5]
       [2 3]  [2 3]
       [1 2]  [2 4]
       [13 400] [325 10000]
       )
  )

(defn ratio=
  [ratio1 ratio2]
  (= (conv/simplify-ratio ratio1) (conv/simplify-ratio ratio2))
  )

(defn inches-fractionv=
  [
   [inches1 low-fraction1 high-fraction1 error-ratio1]
   [inches2 low-fraction2 high-fraction2 error-ratio2]
   ]
  (and
    (float= inches1 inches2)
    (ratio= low-fraction1 low-fraction2)
    (ratio= high-fraction1 high-fraction2)
    (float= error-ratio1 error-ratio2)
    )
  )

(defn feet-fractionv=
  [
   [feet1 & rest1]
   [feet2 & rest2]
   ]
  (and
    (float= feet1 feet2)
    (inches-fractionv= rest1 rest2)
    )
  )

(t/deftest meters->fractional-inches-test
  (t/testing "meters->fractional-inches works OK"
    (t/is (inches-fractionv= [0 [0 1] [1 16] 0.0] (conv/meters->fractional-inches (conv/inches->meters 0.0) 16)))
    (t/is (inches-fractionv= [0 [0 1] [1 16] 0.0] (conv/meters->fractional-inches (conv/inches->meters 0.0)))
          (t/are [inches fraction-denom fiv]
            (inches-fractionv= fiv (conv/meters->fractional-inches (conv/inches->meters inches) fraction-denom))
            8.5 2 [8 [1 2] [1 1] 0.0]
            8.25 2 [8 [0 1] [1 2] 0.5]
            8.75 2 [8 [1 2] [1 1] 0.5]
            8.9999999 2 [8 [1 2] [1 1] 0.9999999999]
            8.0000001 2 [8 [0 1] [1 2] 0.0]
            8.225 1 [8 [0 1] [1 1] 0.225]
            8.123456 10 [8 [1 10] [2 10] 0.23456]
            8.123456 100 [8 [12 100] [13 100] 0.3456]
            8.123456 1000 [8 [123 1000] [124 1000] 0.456]
            8.123456 10000 [8 [1234 10000] [1235 10000] 0.56]
            8.123456 100000 [8 [12345 100000] [12346 100000] 0.6]
            ;; 8.123456 1000000 [8 [123456 1000000] [123457 1000000] 0.0] ;; rounding errors??? !!!
            )
          ))
  )

(t/deftest divide-by-test
  (t/is (vfloat= (conv/divide-by [10.0] 4) [2 2.0]))
  (t/are [n divisor answer]
    (vfloat= (conv/divide-by [n] divisor) answer)
    0 1 [0 0.0]
    0.5 1 [0 0.5]
    1.5 1 [1 0.5]
    6.5 12 [0 6.5]
    12.5 12 [1 0.5]
    18 12 [1 6]
    )
  )

(t/deftest meters->fraction-feet-test
  (t/is (feet-fractionv= [0 0 [0 1] [1 16] 0.0] (conv/meters->fractional-feet (conv/inches->meters 0.0) 16)))
  (t/is (feet-fractionv= [0 0 [0 1] [1 16] 0.0] (conv/meters->fractional-feet (conv/inches->meters 0.0))))
  (t/is (feet-fractionv= [2 8 [12 100] [13 100] 0.3456] (conv/meters->fractional-feet (conv/inches->meters 8.123456) 100)))
  )



#?(:cljs
   (do
     (enable-console-print!)
     (set! *main-cli-fn* #(t/run-tests))))

#?(:cljs
   (defmethod t/report [:cljs.test/default :end-run-tests]
     [m]
     (if (and (exists? js/process) (t/successful? m))
       (set! (.-exitCode js/process) 0)
       (set! (.-exitCode js/process) 1))))
