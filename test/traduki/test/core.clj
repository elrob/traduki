(ns traduki.test.core
  (:require [midje.sweet :refer :all]
            [net.cgrand.enlive-html :as enlv]
            [traduki.core :refer :all]))


(def mock-translator :mock-translations-function)

(defn html-string->enlive [html-string]
  (-> html-string
      java.io.StringReader.
      enlv/html-resource))

(defn enlive->html-string [enlive-nodes]
  (->> enlive-nodes
       enlv/emit*
       (apply str)))

(fact "element content is translated with the content key"
      (let [data-l8n-string "content:some-translation-key"
            html-element  (str "<p data-l8n=\"" data-l8n-string "\""
                               ">!UNTRANSLATED_CONTENT</p>")
            enlive-element (html-string->enlive html-element)]
        (-> (translate mock-translator enlive-element)
            (enlv/select [:p])
            first
            (enlv/text)) => "TRANSLATED_CONTENT"
        (provided
          (mock-translator :some-translation-key) => "TRANSLATED_CONTENT")))

(fact "html content can be used as a translation with the html key"
      (let [data-l8n-string "html:some-translation-key"
            html-element (str "<p data-l8n=\"" data-l8n-string "\""
                              ">!UNTRANSLATED_NON_HTML_CONTENT</p>")
            enlive-element (html-string->enlive html-element)]
        (-> (translate mock-translator enlive-element)
            (enlv/select [:p])
            first
            (enlv/select [:div])
            first
            (enlv/text)) => "TRANSLATED_CONTENT_INSIDE_DIV"
        (provided
          (mock-translator :some-translation-key) => "<div>TRANSLATED_CONTENT_INSIDE_DIV</div>")))

(fact "html attributes can be translated with the attr/value key"
      (let [data-l8n-string "attr/title:some-translation-key"
            html-element (str "<p data-l8n=\"" data-l8n-string "\""
                              " title=\"UNTRANSLATED_TITLE_ATTRIBUTE\">!SOME_CONTENT</p>")
            enlive-element (html-string->enlive html-element)]
        (-> (translate mock-translator enlive-element)
            (enlv/select [:p])
            first
            :attrs
            :title) => "TRANSLATED_TITLE_ATTRIBUTE"
        (provided
          (mock-translator :some-translation-key) => "TRANSLATED_TITLE_ATTRIBUTE")))

(fact "can translate content and attributes in same element"
      (against-background
        (mock-translator :translation-key-1) => "TRANSLATED_TITLE_ATTRIBUTE"
        (mock-translator :translation-key-2) => "TRANSLATED_CONTENT")
      (let [data-l8n-string "attr/title:translation-key-1 content:translation-key-2"
            html-element (str "<p data-l8n=\"" data-l8n-string "\""
                              " title=\"UNTRANSLATED_TITLE_ATTRIBUTE\">!UNTRANSLATED_CONTENT</p>")
            enlive-element (html-string->enlive html-element)
            translated-element (translate mock-translator enlive-element)]
        (-> translated-element
            (enlv/select [:p])
            first
            :attrs
            :title) => "TRANSLATED_TITLE_ATTRIBUTE"
        (-> translated-element
            (enlv/select [:p])
            first
            (enlv/text)) => "TRANSLATED_CONTENT"))

(tabular
 (fact "when translation is not provided, node is left unaltered"
       (against-background
        (mock-translator anything) => nil)
       (let [enlive-element (html-string->enlive ?html-fragment)]
         (translate mock-translator enlive-element) => enlive-element))
 ?html-fragment
 "<p data-l8n=\"content:translation-key\">old-content</p>"
 "<p data-l8n=\"attr/title:translation-key\">old-content</p>"
 "<p data-l8n=\"html:translation-key\">old-content</p>")

(def translator
  (fn [translation-key]
    (case translation-key
      :big-dog "großer Hund"
      :bird "Vogel"
      :fish "Fisch"
      :click-fish "klicken für Fische"
      :cat-link "<a href=\"#Katze\" title=\"Katze\">Katze</a>"
      nil)))

(fact "can translate example html"
      (let [english-html-as-enlive (-> (slurp "test/traduki/test/animals-english.html")
                                       html-string->enlive)
            german-html-as-string (->> (slurp "test/traduki/test/animals-german.html")
                                       html-string->enlive
                                       enlive->html-string)]
        (enlive->html-string (translate translator english-html-as-enlive)) => german-html-as-string))
