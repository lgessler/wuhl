
(ns wuhl.ui.step.language-info
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.i18n :refer [tr lipsum]]))


(def ident [:component/id :step-language-info])

(defsc StepLanguageInfo [this {:keys [data busy error-message] :as props} {:keys [advance reset]}]
  {:query         [[:data '_] :busy :error-message]
   :ident         (fn [] ident)
   :initial-state (fn [_] {:busy false})}


  (dom/div
    (dom/p (tr lipsum))

    (mui/button {:variant   "contained"
                 :component "label"
                 :size      "large"
                 :color     "primary"
                 :startIcon (muic/publish)}
      (tr "LanguageInfo Tabular Data")
      (dom/input {:type     "file"
                  :hidden   true
                  :accept   "text/csv"
                  :onChange (fn [e]
                              (m/set-value! this :busy true)
                              (let [file (-> e .-target .-files (aget 0))
                                    name (.-name file)
                                    reader (js/FileReader.)]
                                (println "Got a file: " name)
                                (set! (.-onload reader)
                                      (fn [e]
                                        (let [contents (-> e .-target .-result)]
                                          (println "Contents: " (pr-str contents)))
                                        ))
                                (.readAsText reader file)))})
      ))
  )

(def ui-step-language-info (c/factory StepLanguageInfo))
