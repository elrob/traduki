(ns traduki.core
  (:require [net.cgrand.enlive-html :as enlv]))

(defn- apply-translation [translation-string node translator]
  (let [[translation-type-string translation-key-string] (clojure.string/split translation-string #":")
        translation-type (keyword translation-type-string)
        translation-key (keyword translation-key-string)
        translation (translator translation-key)]
    (cond
      (= :content translation-type)
      (assoc node :content (list translation))

      (= :html translation-type)
      (assoc node :content (enlv/html-snippet translation))

      (= "attr" (namespace translation-type))
      (assoc-in node [:attrs (keyword (name translation-type))] translation)

      :else node)))

(defn- apply-translations [[translation-string & more] node translator]
  (let [translated-node (apply-translation translation-string node translator)]
    (if more
      (recur more translated-node translator)
      translated-node)))

(defn- translate-node [node translator]
  (let [translation-strings (clojure.string/split (get-in node [:attrs :data-l8n]) #"\s+")]
    (apply-translations translation-strings node translator)))

(defn translate [translator nodes]
  (enlv/at nodes
           [(enlv/attr? :data-l8n)] #(translate-node % translator)))
