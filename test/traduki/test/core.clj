(ns traduki.test.core
  (:require [midje.sweet :refer :all]
            [net.cgrand.enlive-html :as enlv]
            [traduki.core :refer :all]))

(facts "About translating templates"

       (def translator :mock-translations-function)

       (defn html-string->enlive [html-string]
         (-> html-string 
             java.io.StringReader.
             enlv/html-resource))

       (fact "content is translated"
             (let [data-l8n-string "content:some-translation-key"
                   html-element  (str "<p data-l8n=\"" data-l8n-string "\""
                                      ">!UNTRANSLATED_CONTENT</p>")
                   enlive-element (html-string->enlive html-element)]
               (-> (translate translator enlive-element)
                   (enlv/select [:p])
                   first
                   (enlv/text)) => "TRANSLATED_CONTENT"
               (provided
                 (translator :some-translation-key) => "TRANSLATED_CONTENT")))

       (fact "static html content can be used as a translation"
             (let [data-l8n-string "html:some-translation-key"
                   html-element (str "<p data-l8n=\"" data-l8n-string "\""
                                     ">!UNTRANSLATED_NON_HTML_CONTENT</p>")
                   enlive-element (html-string->enlive html-element)]
               (-> (translate translator enlive-element)
                   (enlv/select [:p])
                   first
                   (enlv/select [:.static-html-class])
                   first
                   (enlv/text)) => "TRANSLATED_CONTENT_INSIDE_DIV"
               (provided
                 (translator :some-translation-key) => "<div class=\"static-html-class\">TRANSLATED_CONTENT_INSIDE_DIV</div>")))

       (fact "html attributes can be translated"
             (let [data-l8n-string "attr/title:some-translation-key"
                   html-element (str "<p data-l8n=\"" data-l8n-string "\""
                                     " title=\"UNTRANSLATED_TITLE_ATTRIBUTE\">!SOME_CONTENT</p>")
                   enlive-element (html-string->enlive html-element)]
               (-> (translate translator enlive-element)
                   (enlv/select [:p])
                   first
                   :attrs
                   :title) => "TRANSLATED_TITLE_ATTRIBUTE"
               (provided
                 (translator :some-translation-key) => "TRANSLATED_TITLE_ATTRIBUTE")))

       (fact "can translate content and attributes in same element"
             (against-background
               (translator :translation-key-1) => "TRANSLATED_TITLE_ATTRIBUTE"
               (translator :translation-key-2) => "TRANSLATED_CONTENT")
             (let [data-l8n-string "attr/title:translation-key-1 content:translation-key-2"
                   html-element (str "<p data-l8n=\"" data-l8n-string "\""
                                     " title=\"UNTRANSLATED_TITLE_ATTRIBUTE\">!UNTRANSLATED_CONTENT</p>")
                   enlive-element (html-string->enlive html-element)
                   translated-element (translate translator enlive-element)]
               (-> translated-element
                   (enlv/select [:p])
                   first
                   :attrs
                   :title) => "TRANSLATED_TITLE_ATTRIBUTE"
               (-> translated-element
                   (enlv/select [:p])
                   first
                   (enlv/text)) => "TRANSLATED_CONTENT")))
