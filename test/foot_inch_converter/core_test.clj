(ns foot-inch-converter.core-test
  (:require [clojure.test :refer :all]
            [foot-inch-converter.core :refer :all]))

(deftest a-test
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

(deftest b-test
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

(deftest c-test
  (testing "to-feet works OK"
    (is (= 1.0 (to-feet [1.0 nil nil nil])))
    (are [ft v] (= ft (to-feet v))
                1.0 [1.0 nil nil nil]
                1.5 [1.0 6.0 nil nil]
                1.5 [1.0 0.0 12.0 2.0]
                2.0 [1.0 6.0 12.0 2.0]
                )
    ))

