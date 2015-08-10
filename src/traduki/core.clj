(ns traduki.core
  (:require [clojure.string :as string]
            [net.cgrand.enlive-html :as enlv]))

(defn- apply-translation [translator node translation-string]
  (let [[translation-type-string translation-key-string] (string/split translation-string #":")
        translation-type (keyword translation-type-string)
        translation-key (keyword translation-key-string)
        translation (translator translation-key)]
    
    (cond
      (nil? translation)
      node
      
      (= :content translation-type)
      (assoc node :content (list translation))

      (= :html translation-type)
      (assoc node :content (enlv/html-snippet translation))

      (= "attr" (namespace translation-type))
      (assoc-in node [:attrs (keyword (name translation-type))] translation)

      :else node)))

(defn- translate-node [node translator]
  (let [data-l8n-value (get-in node [:attrs :data-l8n])
        translation-strings (string/split data-l8n-value #"\s+")]
    (reduce #(apply-translation translator %1 %2) node translation-strings)))

(defn translate [translator nodes]
  (enlv/at nodes
           [(enlv/attr? :data-l8n)] #(translate-node % translator)))
