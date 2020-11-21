(ns wuhl.ui.step.upload
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.ui.step.common :as common]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.i18n :refer [tr lipsum]]
            ["papaparse" :as Papa]))


(def ident [:component/id :step-upload])

(defn parse-csv [csv-string]
  (-> (.parse Papa csv-string #js {:header true})
      (js->clj :keywordize-keys true)))
(defn valid-csv? [csv])

(defn format-error-message
  [errors]
  (clojure.string/join "\n" (for [{:keys [type message row]} (sort :row errors)]
                              (str "Row " row ": " type ": " message))))

(defmutation handle-upload [{:keys [contents name advance]}]
  (action [{:keys [state component] :as env}]
          (let [{:keys [data errors meta] :as parsed} (parse-csv contents)]

            (if (> (count errors) 0)
              (m/set-value! component :message {:body (format-error-message errors)
                                                :type :error})
              (do
                (m/set-value! component :message {:body (str name " loaded successfully")
                                                  :type :success})
                (swap! state assoc :data parsed)
                (advance)))
            )
          (m/set-value! component :busy false)))


(defsc StepUpload [this {:keys [data busy message] :as props} {:keys [advance reset]}]
  {:query         [[:data '_] :busy :message]
   :ident         (fn [] ident)
   :initial-state (fn [_] {:busy false})}

  ;; explanation
  (mui/card {}
    (when busy
      (mui/linear-progress))

    (mui/card-content {}

      (when-let [message-type (:type message)]
        (mui/alert {:severity (if (= message-type :error) "error" "success")}
          (mui/alert-title {} (if (= message-type :error) (tr "There are issues with your file") (tr "Load successful")))
          (let [body (:body message)]
            (if (clojure.string/includes? body "\n")
              (dom/ul
                (map #(dom/li {:key %1} %2)
                     (range)
                     (clojure.string/split-lines body)))
              (:body message)))))

      (dom/p (tr lipsum))

      (mui/button {:variant   (if data "outlined" "contained")
                   :component "label"
                   :size      "large"
                   :color     "primary"
                   :disabled  busy
                   :startIcon (muic/publish)}
        (tr "Load Tabular Data")
        (dom/input {:type     "file"
                    :hidden   true
                    :accept   "text/csv"
                    :onChange (fn [e]
                                (m/set-value! this :busy true)
                                (let [file (-> e .-target .-files (aget 0))
                                      name (.-name file)
                                      reader (js/FileReader.)]
                                  (set! (.-onload reader)
                                        (fn [e]
                                          (let [contents (-> e .-target .-result)]
                                            (m/set-value! this :message nil)
                                            (m/set-value! this :busy true)
                                            (c/transact! this [(handle-upload {:contents contents
                                                                               :name     name
                                                                               :advance  advance})])
                                            (js/console.log :submitted))))
                                  (.readAsText reader file)))}))

      )

    (mui/card-actions {}
                      (common/action-div
                        {:next  (common/next-button {:onClick  advance
                                                     :disabled (or busy (not (some? data)))})
                         :reset (common/reset-button {:disabled busy})}))))

(def ui-step-upload (c/factory StepUpload))