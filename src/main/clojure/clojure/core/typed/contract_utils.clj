(ns clojure.core.typed.contract-utils
  {:skip-wiki true
   :core.typed {:collect-only true}}
  (:require [clojure.set :as set]))

(alter-meta! *ns* assoc :skip-wiki true
             :core.typed {:collect-only true})



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Constraint shorthands

(defn every-c? [c]
  #(every? c %))

(def nne-seq? (some-fn nil? (every-pred seq seq?)))

(def nat? (every-pred integer? (complement neg?)))

(def boolean? (some-fn true? false?))

(def namespace? #(instance? clojure.lang.Namespace %))

(defn =-c? [& as]
  #(apply = (concat as %&)))

(defn hvector-c? [& ps]
  (apply every-pred vector?
         (map (fn [p i] #(p (nth % i false))) ps (range))))

(defn array-map-c? [ks-c? vs-c?]
  (every-pred #(instance? clojure.lang.PersistentArrayMap %)
              #(every? ks-c? (keys %))
              #(every? vs-c? (vals %))))

(defrecord OptionalKey [k])

(defn optional [k]
  (->OptionalKey k))

(defn hmap-c? [& key-vals]
  {:pre [(even? (count key-vals))]}
  (every-pred map?
              (fn [m]
                (letfn [(mandatory-check [m k vc]
                          (and (contains? m k)
                               (vc (get m k))))
                        (optional-check [m k vc]
                          (or (not (contains? m k))
                              (mandatory-check m k vc)))]
                  (every? identity 
                    (for [[k vc] (partition 2 key-vals)]
                      (cond
                        (instance? OptionalKey k) (optional-check m (:k k) vc)
                        :else (mandatory-check m k vc))))))))

(defn hash-c? [ks-c? vs-c?]
  (every-pred map?
              #(every? ks-c? (keys %))
              #(every? vs-c? (vals %))))

(defn set-c? [c?]
  (every-pred set?
              #(every? c? %)))

(defn sequential-c? [c?]
  (every-pred sequential?
              (every-c? c?)))
