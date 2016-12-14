(ns foot-inch-converter.core-test
  (:require [clojure.test :refer :all]
            [foot-inch-converter.core :refer :all]))

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
