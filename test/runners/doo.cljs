(ns runners.doo
  (:require
    [doo.runner :refer-macros [doo-all-tests]]
    [foot-inch-converter.core-test]))

(doo-all-tests
  #"foot-inch-converter\.core-test")
