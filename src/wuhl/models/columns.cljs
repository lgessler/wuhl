(ns wuhl.models.columns
  (:require [wuhl.i18n :refer [tr]]))

(def default-type :ignore)

(def required-types #{:primary-form :definition})

(def column-types
  {:primary-form {:id       :primary-form
                  :name     (tr "Primary Form")
                  :helptext (tr "For the primary orthographic representation of a word")}
   #_#_:primary-lexcat {:id       :primary-lexcat
                        :name     (tr "Primary Lexical Category")
                        :helptext (tr "For categorizations of the sense based on its syntax")}
   :definition   {:id       :definition
                  :name     (tr "Definition")
                  :helptext (tr "A gloss of the sense's meaning")}
   :ignore       {:id       :ignore
                  :name     (tr "Ignore")
                  :helptext (tr "Ignore this column")}})

(defn- entry->ast [[form rows] grouped-configs primary-form-colname definition-colname]
  "First argument is a pair where the first is the primary form and the second is a seq of
  rows from the spreadsheet that we need to transform into a single entry AST"
  {:form        form
   :definitions (map definition-colname rows)})

(defn generate-ast [column-config columns]
  "Given a set of column configurations and a seq of rows, produce a sequence of
  nested maps which abstractly represents the layout of a dictionary entry"
  (let [grouped-configs (group-by :column/type column-config)
        primary-form-colnames (map :column/id (:primary-form grouped-configs))
        definition-colnames (map :column/id (:definition grouped-configs))]
    (when (> (count primary-form-colnames) 1)
      (js/console.warn "More than one primary form column supplied. Ignoring the rest."))
    (when (> (count definition-colnames) 1)
      (js/console.warn "More than one definition column supplied. Ignoring the rest."))


    (let [primary-form-colname (some-> primary-form-colnames first keyword)
          definition-colname (some-> definition-colnames first keyword)]
      (when-not primary-form-colname
        (throw (js/Error. "Must supply at least one primary form column")))
      (when-not definition-colname
        (throw (js/Error. "Must supply at least one definition column")))
      (let [entries (group-by primary-form-colname columns)]
        (map #(entry->ast % grouped-configs primary-form-colname definition-colname) entries)))))

(comment
  (let [__column-config [{:column/id "Devanagari", :column/type :primary-form}
                         {:column/id "IAST", :column/type :ignore}
                         {:column/id "IPA", :column/type :ignore}
                         {:column/id "POS", :column/type :ignore}
                         {:column/id "gender", :column/type :ignore}
                         {:column/id "meaning", :column/type :definition}
                         {:column/id "notes", :column/type :ignore}
                         {:column/id "references", :column/type :ignore}]
        __columns [{:Devanagari "आम ", :IAST "āma", :IPA "ɑːmə", :POS "noun", :gender "m", :meaning "mango", :notes "", :references ""} {:Devanagari "आम ", :IAST "āma", :IPA "ɑːmə", :POS "adj", :gender "", :meaning "common", :notes "", :references ""} {:Devanagari "नेता", :IAST "netā", :IPA "neːt̪ɑː", :POS "noun", :gender "m", :meaning "leader", :notes "indeclinable, inherently honorific", :references ""} {:Devanagari "धूप ", :IAST "dhūpa", :IPA "d̪ʰuːpə", :POS "noun", :gender "f", :meaning "sunshine", :notes "", :references ""} {:Devanagari "सेना ", :IAST "senā", :IPA "seːnɑː", :POS "noun", :gender "f", :meaning "army", :notes "", :references ""} {:Devanagari "सेना ", :IAST "senā", :IPA "seːnɑː", :POS "verb", :gender "", :meaning "(transitive) to brood (eggs)", :notes "rare", :references "https://dsal.uchicago.edu/cgi-bin/app/dasa-hindi_query.py?qs=%E0%A4%B8%E0%A5%87%E0%A4%A8%E0%A4%BE&searchhws=yes"}]]
    (generate-ast __column-config __columns)
    )

  )