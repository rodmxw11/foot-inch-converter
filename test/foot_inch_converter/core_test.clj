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
