(ns online.harrigan.main-test
  (:require
   [expectations.clojure.test :as t :refer [defexpect expect expecting]]
   [online.harrigan.main :as sut]))

(defexpect main-tests
  (expecting
   "I will fail, you will need to fix me!"
   (expect true false)))
