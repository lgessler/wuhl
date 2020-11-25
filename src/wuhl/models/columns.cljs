(ns wuhl.models.columns
  (:require [wuhl.i18n :refer [tr]]))

(def default-type :ignore)

(def required-types #{:primary-form :definition})

(def column-types
  {:primary-form   {:id       :primary-form
                    :name     (tr "Primary Form")
                    :example  (tr "Dog")
                    :helptext (tr "For the primary orthographic representation of a word")}
   :alternate-form {:id       :alternate-form
                    :name     (tr "Alternate Form")
                    :example  (tr "dɔɡ")
                    :helptext (tr "For pronunciations or other secondary orthographic representations.")}
   :primary-lexcat {:id       :primary-lexcat
                    :name     (tr "Primary Lexical Category")
                    :example  (tr "Noun")
                    :helptext (tr "Used to group senses so that, for example, all noun senses are listed separately from verb senses.")}
   :lexcat         {:id       :lexcat
                    :name     (tr "Lexical Category")
                    :example  (tr "masculine")
                    :helptext (tr "For non-primary categorizations, like transitivity or grammatical gender")}
   :definition     {:id       :definition
                    :name     (tr "Definition")
                    :example  (tr "A mammal, Canis familiaris or Canis lupus familiaris")
                    :helptext (tr "A gloss of the sense's meaning")}
   :sense-comment  {:id       :sense-comment
                    :name     (tr "Sense-level Comment")
                    :example  (tr "obsolete")
                    :helptext (tr "A short comment on a sense")}
   :ignore         {:id       :ignore
                    :name     (tr "Ignore")
                    :helptext (tr "Ignore this column")}})



(defn- entry->ast [[form rows] grouped-configs]
  "First argument is a pair where the first is the primary form and the second is a seq of
  rows from the spreadsheet that we need to transform into a single entry AST"
  #{:primary-form
    :alternate-forms
    :blocks #{:lexcat :senses #{:body :comments}}
    }
  (println grouped-configs)
  (let [get-keys (fn [column-type grouped-configs]
                   (->> grouped-configs column-type (map :column/id) (map keyword)))
        gather-unique (fn [column-type grouped-configs rows]
                        (->> grouped-configs
                             (get-keys column-type)
                             (mapcat #(map % rows))
                             set
                             vec))
        primary-lexcat-key (->> grouped-configs :primary-lexcat first :column/id keyword)
        definition-key (->> grouped-configs :definition first :column/id keyword)
        sense-comment-keys (get-keys :sense-comment grouped-configs)
        lexcat-senses (if (nil? primary-lexcat-key)
                        {nil rows}
                        (->> rows (group-by primary-lexcat-key)))]
    {:primary-form    form
     :alternate-forms (gather-unique :alternate-form grouped-configs rows)
     :blocks          (vec (for [[primary-lexcat rows-for-lexcat] lexcat-senses]
                             (let [lexcats (->> (get-keys :lexcat grouped-configs)
                                                (mapcat #(map % rows-for-lexcat))
                                                (filter #(> (count %) 0))
                                                set
                                                vec)]
                               (-> {:senses (vec (for [row rows-for-lexcat]
                                                   (let [sense-comments (->> rows-for-lexcat
                                                                             (gather-unique :sense-comment grouped-configs)
                                                                             (filter #(> (count %) 0))
                                                                             vec)]
                                                     (-> {:body (definition-key row)}
                                                         (cond-> (not-empty sense-comments)
                                                                 (assoc :comments sense-comments))))))}
                                   (cond-> primary-lexcat (assoc :primary-lexcat primary-lexcat)
                                           (not-empty lexcats) (assoc :lexcats lexcats))))))}))

(comment
  (entry->ast ["आम" [{:Devanagari "आम", :IAST "āma", :IPA "ɑːmə", :POS "noun", :gender "m", :gander "f", :meaning "mango", :notes "", :references ""}
                     {:Devanagari "आम", :IAST "āma", :IPA "ɑːmə", :POS "adj", :gender "", :gander "f", :meaning "common", :notes "lol", :references ""}]]
              {:primary-form   [{:column/id "Devanagari", :column/type :primary-form}],
               :alternate-form [{:column/id "IAST", :column/type :alternate-form}
                                {:column/id "IPA", :column/type :alternate-form}],
               :primary-lexcat [{:column/id "POS", :column/type :primary-lexcat}]
               :lexcat         [{:column/id "gender", :column/type :lexcat}
                                {:column/id "gander", :column/type :lexcat}]
               :sense-comment [{:column/id "notes", :column/type :sense-comment}]
               :ignore         [{:column/id "notes", :column/type :ignore}
                                {:column/id "references", :column/type :ignore}],
               :definition     [{:column/id "meaning", :column/type :definition}]})

  )


(defn explain-config-error [config]
  (let [grouped-configs (group-by :column/type config)
        primary-form-colnames (map :column/id (:primary-form grouped-configs))
        primary-lexcat-colnames (map :column/id (:primary-lexcat grouped-configs))
        definition-colnames (map :column/id (:definition grouped-configs))]
    (cond
      (not= 1 (count definition-colnames)) (tr "Must supply exactly one definition column")
      (not= 1 (count primary-form-colnames)) (tr "Must supply exactly one primary form column")
      (> (count primary-lexcat-colnames) 1) (tr "Must supply at most one primary lexical category column")
      :else nil
      )))

(defn generate-ast [column-config columns]
  "Given a set of column configurations and a seq of rows, produce a sequence of
  nested maps which abstractly represents the layout of a dictionary entry"
  (let [grouped-configs (group-by :column/type column-config)
        primary-form-colnames (map :column/id (:primary-form grouped-configs))]
    (if-let [error (explain-config-error column-config)]
      error
      (let [primary-form-colname (some-> primary-form-colnames first keyword)]
        ;; group together rows with the same primary form
        (let [entries (group-by primary-form-colname columns)]
          (map #(entry->ast % grouped-configs) entries))))))

(comment
  (let [__column-config [{:column/id "Devanagari", :column/type :primary-form}
                         {:column/id "IAST", :column/type :alternate-form}
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