(ns cherry.compiler-test
  (:require
   [cherry.compiler :as cherry]
   [cherry.jsx-test]
   [cherry.test-utils :refer [js! jss! jsv!]]
   [clojure.string :as str]
   [clojure.test :as t :refer [async deftest is]]))

(deftest return-test
  (is (str/includes? (jss! '(do (def x (do 1 2 nil))))
                     "return"))
  (is (str/includes? (jss! '(do (def x (do 1 2 "foo"))))
                     "return"))
  (is (str/includes? (jss! '(do (def x (do 1 2 :foo))))
                     "return"))
  (is (str/includes? (jss! "(do (def x (do 1 2 \"hello\")))")
                     "return"))
  (let [s (jss! "(do (def x (do 1 2 [1 2 3])) x)")]
    (is (= [1 2 3] (js/eval s))))
  (let [s (jss! "(do (def x (do 1 2 {:x 1 :y 2})) x)")]
    (is (= {:x 1 :y 2} (js/eval s))))
  (let [s (jss! "(do (def x (do 1 2 #js {:x 1 :y 2})) x)")]
    (is (= (str #js {:x 1 :y 2}) (str (js/eval s))))))

(deftest do-test
  (let [[v s] (js! '(do 1 2 3))]
    (is (= 3 v))
    (is (not (str/includes? s "function"))))
  (let [[v s] (js! '(do 1 2 3 (do 4 5 6)))]
    (is (= 6 v))
    (is (not (str/includes? s "function"))))
  (let [[v s] (js! '(do (def x (do 4 5 6))
                        x))]
    (is (= 6 v))
    (is (str/includes? s "function")))
  (let [[v s] (js! '(let [x (do 4 5 6)]
                      x))]
    (is (= 6 v))
    (is (str/includes? s "function"))))

(deftest let-test
  (is (= 3 (jsv! '(let [x (do 1 2 3)] x))))
  (is (= 3 (jsv! '(let [x 1 x (+ x 2)] x))))
  (let [s (jss! '(let [x 1 x (let [x (+ x 1)]
                               x)] x))]
    (is (= 2 (js/eval s))))
  (is (= 7 (jsv! '(let [{:keys [a b]} {:a 1 :b (+ 1 2 3)}]
                    (+ a b)))))
  (is (= 8 (jsv!
            '(+ 1
                (let [{:keys [a b]} {:a 1 :b (+ 1 2 3)}]
                  (+ a b)))))))

(deftest let-interop-test
  (is (= "f" (jsv! '(let [x "foo"]
                      (.substring x 0 1)))))
  (is (= 3 (jsv! '(let [x "foo"]
                    (.-length x))))))

(deftest let-shadow-test
  (is (= 1 (jsv! '(let [name 1]
                    name))))
  (is (= 1 (jsv! '(let [name (fn [] 1)]
                    (name)))))
  (let [s (jss! '(let [name (fn [_] 1)]
                   (map name [1 2 3])))]
    (is (= '(1 1 1)
           (js/eval s))))
  (let [s (jss! '(let [name (fn [_] 1)
                       name (fn [_] 2)]
                   (map name [1 2 3])))]
    (is (= '(2 2 2)
           (js/eval s)))))

(deftest destructure-test
  (let [s (jss! "(let [^js {:keys [a b c]} #js {:a 1 :b 2 :c 3}]
                   (+ a b c))")]
    (is (= 6 (js/eval s)))))

(deftest fn-test
  (let [s (jss! '(let [f (fn [x] x)]
                   f))]
    (is (= 1 ((js/eval s) 1))))
  (let [s (jss! '(let [f (fn [x] 1 2 x)]
                   f))]
    (is (= 1 ((js/eval s) 1))))
  (let [s (jss! '(let [f (fn [x] 1 2 (+ 1 x))]
                   f))]
    (is (= 2 ((js/eval s) 1))))
  (let [s (jss! '(let [f (fn [x] 1 2 (do 1 x))]
                   f))]
    (is (= 1 ((js/eval s) 1))))
  (is (= 1 (jsv! '(do (defn foo [] (fn [x] x)) ((foo) 1))))))

(deftest fn-varargs-test
  (is (= '(3 4) (jsv! '(let [f (fn foo [x y & zs] zs)] (f 1 2 3 4)))))
  (is (nil? (jsv! '(let [f (fn foo [x y & zs] zs)] (f 1 2))))))

(deftest fn-multi-arity-test
  (is (= 1 (jsv! '(let [f (fn foo ([x] x) ([x y] y))] (f 1)))))
  (is (= 2 (jsv! '(let [f (fn foo ([x] x) ([x y] y))] (f 1 2))))))

(deftest fn-multi-varargs-test
  (is (= 1 (jsv! '(let [f (fn foo ([x] x) ([x y & zs] zs))] (f 1)))))
  (is (= '(3 4) (jsv! '(let [f (fn foo ([x] x) ([x y & zs] zs))] (f 1 2 3 4)))))
  (is (nil? (jsv! '(let [f (fn foo ([x] x) ([x y & zs] zs))] (f 1 2))))))

(deftest defn-test
  (let [s (jss! '(do (defn f [x] x) f))]
    (is (= 1 ((js/eval s) 1))))
  (let [s (jss! '(do (defn f [x] (let [y 1] (+ x y))) f))]
    (is (= 2 ((js/eval s) 1))))
  (let [s (jss! '(do (defn foo [x]
                       (dissoc x :foo))
                     (foo {:a 1 :foo :bar})))]
    (is (= {:a 1} (js/eval s))))
  (let [s (jss! "(do (defn f [^js {:keys [a b c]}] (+ a b c)) f)")]
    (is (= 6 ((js/eval s) #js {:a 1 :b 2 :c 3}))))
  (let [s (jss! '(do (defn quux [x]
                       (if (pos? x)
                         1
                         2))
                     (quux 1)))]
    (is (= 1 (js/eval s)))))

(deftest defn-multi-arity-test
  (is (= 1 (jsv! '(do
                    (defn foo ([x] x) ([x y] y))
                    (foo 1)))))
  (is (= 2 (jsv! '(do
                    (defn foo ([x] 1) ([x y] y))
                    (foo 1 2))))))

(deftest defn-recur-test
  (let [s (jss! '(do (defn quux [x]
                       (if (pos? x)
                         (recur (dec x))
                         x))
                     (quux 1)))]
    (is (zero? (js/eval s)))))

(deftest defn-varargs-test
  (let [s (jss! '(do (defn foo [x & args] args) (foo 1 2 3)))]
    (is (= '(2 3) (js/eval s)))))

(deftest defn-multi-varargs-test
  (is (= [1 [1 2 '(3 4)]]
         (js/eval
          (jss! '(do (defn foo
                       ([x] x)
                       ([x y & args]
                        [x y args]))
                     [(foo 1) (foo 1 2 3 4)]))))))

(deftest loop-test
  (let [s (jss! '(loop [x 1] (+ 1 2 x)))]
    (is (= 4 (js/eval s))))
  (let [s (jss! '(loop [x 10]
                   (if (pos? x)
                     (recur (dec x))
                     x)))]
    (is (zero? (js/eval s)))))

(deftest if-test
  (is (true? (jsv! "(if 0 true false)")))
  (let [s (jss! "[(if false true false)]")]
    (false? (first (js/eval s))))
  (let [s (jss! "(let [x (if (inc 1) (inc 2) (inc 3))]
                   x)")]
    (is (= 3 (js/eval s))))
  (let [s (jss! "(let [x (do 1 (if (inc 1) (inc 2) (inc 3)))]
                   x)")]
    (is (= 3 (js/eval s)))))

(deftest doseq-test
  (let [s (jss! '(let [a (atom [])]
                   (doseq [x [1 2 3]]
                     (swap! a conj x))
                   (deref a)))]
    (is (= [1 2 3] (js/eval s))))
  (let [s (jss! '(let [a (atom [])]
                   (doseq [x [1 2 3]
                           y [4 5 6]]
                     (swap! a conj x y))
                   (deref a)))]
    (is (=  [1 4 1 5 1 6 2 4 2 5 2 6 3 4 3 5 3 6]
            (js/eval s)))))

(deftest for-test
  (let [s (jss! '(for [x [1 2 3] y [4 5 6]] [x y]))]
    (is (= '([1 4] [1 5] [1 6] [2 4] [2 5] [2 6] [3 4] [3 5] [3 6])
           (js/eval s)))))

(deftest regex-test
  (is (= '("foo" "foo")
         (jsv! '(re-seq #"foo" "foo foo")))))

(deftest new-test
  (is (= "hello" (jsv! '(str (js/String. "hello"))))))

(deftest quote-test
  (is (= '{x 1} (jsv! (list 'quote '{x 1}))))
  (is (= '(def x 1) (jsv! (list 'quote '(def x 1))))))

(deftest case-test
  (let [s (jss! '(case 'x x 2))]
    (is (= 2 (js/eval s))))
  (is (= 2 (jsv! '(case 1 1 2 3 4))))
  (is (= 5 (jsv! '(case 6 1 2 3 4 (inc 4)))))
  (is (= 2 (jsv! '(case 1 :foo :bar 1 2))))
  (is (= :bar (jsv! '(case :foo :foo :bar))))
  (is (thrown-with-msg? js/Error #"No matching clause"
                        (jsv! '(case 'x y 2))))
  (let [s (jss! '(let [x (case 1 1 2 3 4)]
                   (inc x)))]
    (is (= 3 (js/eval s))))
  (let [s (jss! '(do (defn foo []
                       (case 1 1 2 3 4))
                     (foo)))]
    (is (= 2 (js/eval s)))))

(deftest dot-test
  (let [s (jss! "(do (def x (.-x #js {:x 1})) x)")]
    (is (= 1 (js/eval s))))
  (let [s (jss! "(do (def x (. #js {:x 1} -x)) x)")]
    (is (= 1 (js/eval s))))
  (let [s (jss! "(do (def x (.x #js {:x (fn [] 1)})) x)")]
    (is (= 1 (js/eval s))))
  (let [s (jss! "(do (def x (.x #js {:x (fn [x] x)} 1)) x)")]
    (is (= 1 (js/eval s))))
  (let [s (jss! "(do (def x (. #js {:x (fn [x] x)} x 1)) x)")]
    (is (= 1 (js/eval s))))
  (let [s (jss! "(do (def x (. #js {:x (fn [x] x)} (x 1))) x)")]
    (is (= 1 (js/eval s))))
  (let [s (jss! "(.goto #js {:goto (fn [x] [:hello x])} 10)")]
    (is (= [:hello 10] (js/eval s)))))

(deftest dotdot-test
  (let [s (jss! "(.. #js {:foo #js {:bar 2}} -foo -bar)")]
    (is (= 2 (js/eval s)))))

#_(js-delete js/require.cache (js/require.resolve "/tmp/debug.js"))
#_(js/require "/tmp/debug.js")

(deftest backtick-test
  (is (= '(assoc {} :foo :bar) (jsv! "`(assoc {} :foo :bar)"))))

(deftest munged-core-name-test
  (is (jsv! '(boolean 1))))

(deftest defprotocol-extend-type-string-test
  (is (= :foo (jsv! '(do (defprotocol IFoo (foo [_])) (extend-type string IFoo (foo [_] :foo)) (foo "bar"))))))

(deftest deftype-test
  (is (= 1 (jsv! '(do (deftype Foo [x]) (.-x (->Foo 1))))))
  (is (= [:foo :bar]
         (jsv! '(do
                  (defprotocol IFoo (foo [_]) (bar [_]))
                  (deftype Foo [x] IFoo (foo [_] :foo)
                           (bar [_] :bar))
                  (let [x (->Foo 1)]
                    [(foo x) (bar x)])))))
  (is (= [:foo 2]
         (jsv! '(do (defprotocol IFoo (foo [_]) (bar [_]))
                    (deftype Foo [^:mutable x]
                      IFoo
                      (foo [_] [:foo x])
                      (bar [_] :bar))
                    (def x  (->Foo 1))
                    (set! (.-x x) 2)
                    (foo x))))))

(deftest set-test
  (is (= #{1 2 3 4 5 6} (jsv! '(into #{1 2 3} #{4 5 6})))))

(deftest await-test
  (async done
         (->
          (.then (jsv! '(do (defn ^:async foo []
                              (js/await (js/Promise.resolve :hello)))

                            (defn ^:async bar []
                              (let [x (js/await (foo))]
                                x))

                            (bar)))
                 (fn [v]
                   (is (= :hello v))))
          (.catch (fn [err]
                    (is false (.-message err))))
          (.finally #(done)))))

(deftest native-js-array-test
  (let [s (jss! "(let [x 2
                       x #js [1 2 x]]
                   x)")
        x (js/eval s)]
    (is (array? x))
    (is (= [1 2 2] (js->clj x))))
  (is (= 1 (jsv! "(aget  #js [1 2 3] 0)"))))

(deftest keyword-call-test
  (is (= :bar (jsv! '(:foo {:foo :bar}))))
  (is (= :bar (jsv! '(let [x :foo]
                       (x {:foo :bar})))))
  (is (= :bar (jsv! '((keyword "foo") {:foo :bar})))))

(deftest minus-single-arg-test
  (is (= -10 (jsv! '(- 10))))
  (is (= -11 (jsv! '(- 10 21)))))

(deftest namespace-keywords
  (is (= :hello/world (jsv! "(ns hello) ::world"))))

(deftest plus-hof-test
  (is (= 6 (jsv! "(apply + [1 2 3])"))))

(deftest instance?-test
  (is (jsv! '(do (deftype Foo []) (instance? Foo (->Foo))))))

(deftest logic-return
  (is (= 2 (jsv! '(do (defn foo [a b] (and a b)) (foo 1 2)))))
  (is (= 1 (jsv! '(do (defn foo [a b] (or a b)) (foo 1 2))))))

(deftest multiple-arity-infix
  (is (true? (jsv! '(> 5 4 3 2 1))))
  (is (true? (jsv! '(> 5 4 3))))
  (is (true? (jsv! '(> 5 4))))
  ;; I would say this is undefined in clava for now:
  #_(is (true? (jsv! '(> 5)))))

(deftest double-names-in-sig-test
  (is (= 2 (jsv! '(do (defn foo [x x] x) (foo 1 2))))))

(deftest try-catch-test
  (is (= 2 (jsv! '(try (assoc :foo 1 2) (catch :default _ 2))))))

(deftest require-with-kebab-case-alias-test
  (let [s (cherry/compile-string "(ns test-namespace (:require [\"some-js-library$default\" :as some-js-lib])) (some-js-lib/some_fn)")]
    (is (str/includes? s "import { some_fn as some_js_lib_some_fn } from 'some-js-library$default'"))
    (is (str/includes? s "some_js_lib_some_fn.call(null)")))

  (let [s (cherry/compile-string "(ns test-namespace (:require [\"some-js-library\" :as some-js-lib])) (some-js-lib/some_fn)")]
    (is (str/includes? s "import { some_fn as some_js_lib_some_fn } from 'some-js-library'"))
    (is (str/includes? s "some_js_lib_some_fn.call(null)")))

  (let [s (cherry/compile-string "(ns test-namespace (:require [\"./local_file.mjs\" :as local-file])) (local-file/some_fn)")]
    (is (str/includes? s "import { some_fn as local_file_some_fn } from './local_file.mjs'"))
    (is (str/includes? s "local_file_some_fn.call(null)")))

  ;; TODO:
  #_(let [s (cherry/compile-string "(ns test-namespace (:require [clojure.core :as clojure-core])) (clojure-core/some-fn)")]
    (is (str/includes? s "import { some_fn as clojure_core_some_fn } from 'clojure-core'"))
    (is (str/includes? s "clojure_core_some_fn.call(null)"))))

(defn init []
  (cljs.test/run-tests 'cherry.compiler-test 'cherry.jsx-test))
