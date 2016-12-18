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

(ns foot-inch-converter.core-test
  (:require [clojure.test :refer :all]
            [foot-inch-converter.core :refer :all]))
(def ^:const epsilon
  "Tolerance for comparing floating point numbers"
  1e-7)


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

(deftest regex-test
  (testing "foot-inch-regex basic test"
    (is (re-matches foot-inch-regex "0"))
    (are [s] (re-matches foot-inch-regex s)
             "0"
             "0."
             "1.2"
             "0 1"
             "0.0 1.1"
             "0 1.22"
             "0 0 1/2"
             "1.223 3.331 3/138"
             )
    ))

(deftest parsing-test
  (testing "foot-inch-regex digit parsing"
    (is (vfloat= [0.0 nil nil nil] (parse-foot-inch "0")))
    (are [s v] (vfloat= v (parse-foot-inch s))
               "0" [0.0 nil nil nil]
               "0." [0.0 nil nil nil]
               "000.000" [0.0 nil nil nil]
               "1 2" [1.0 2.0 nil nil]
               "1.23 4.56 3/4" [1.23 4.56 3.0 4.0]
               )
    ))

(deftest to-feet-test
  (testing "to-feet works OK"
    (is (float= 1.0 (to-feet [1.0 nil nil nil])))
    (are [ft v] (float= ft (to-feet v))
                1.0 [1.0 nil nil nil]
                1.5 [1.0 6.0 nil nil]
                1.5 [1.0 0.0 12.0 2.0]
                2.0 [1.0 6.0 12.0 2.0]
                0.0 [0.0 nil nil nil]
                )
    ))

(deftest to-inches-test
  (testing "to-inches works OK"
    (is (float= 12.0 (to-inches [1.0 nil nil nil])))
    (are [inches v] (float= inches (to-inches v))
                1.0 [0.0 1.0 nil nil]
                0.5 [0.0 0.0 1.0 2.0]
                24.75 [1.5 6.0 3.0 4.0]
                  )
    )
  )

(defn frac-inch=
  [[a1 a2 a3 a4][b1 b2 b3 b4]]
  (and
    (= a1 b1)
    (= a2 b2)
    (= a3 b3)
    (float= a4 b4)
    )
  )

(deftest to-fractional-inch-test
  (testing "to-fractional-inch works OK"
    (is (frac-inch= [0 0 1/16 0.0] (to-fractional-inch 0.0 16)))
    (are [f denom v]
      (frac-inch= v (to-fractional-inch f denom))
      8.5 2 [8 1/2 1 0.0]
      8.25 2 [8 0 1/2 0.5]
      8.75 2 [8 1/2 1 0.5]
      8.9999999 2 [8 1/2 1 0.9999999999]
      8.0000001 2 [8 0 1/2 0.0]
      8.225 1 [8 0 1 0.225]
      8.123456 10 [8 1/10 2/10 0.23456]
      8.123456 100 [8 12/100 13/100 0.3456]
      8.123456 1000 [8 123/1000 124/1000 0.456]
      8.123456 10000 [8 1234/10000 1235/10000 0.56]
      8.123456 100000 [8 12345/100000 12346/100000 0.6]
    ;;  8.123456 1000000 [8 123456/1000000 123457/1000000 0.0] ;; rounding errors !!!
      )
    ))
