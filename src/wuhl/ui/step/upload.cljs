(ns wuhl.ui.step.upload
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [wuhl.application :refer [SPA]]
            [wuhl.ui.step.common :as common]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.i18n :refer [tr lipsum]]
            [wuhl.models.columns :as wc]
            [wuhl.ui.step.column-config :refer [StepColumnConfig]]
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

(defmutation handle-upload [{:keys [contents name next-step]}]
  (action [{:keys [state component] :as env}]
          (let [{:keys [data errors meta] :as parsed} (parse-csv contents)]

            (if (> (count errors) 0)
              (m/set-value! component :message {:body (format-error-message errors)
                                                :type :error})
              (let [columns (->> (:fields meta)
                                 (map (fn [id] {:column/id id :column/type wc/default-type}))
                                 vec)]
                (m/set-value! component :message {:body (str name " loaded successfully")
                                                  :type :success})
                ;; save a copy of the parsed data
                (swap! state assoc :data parsed)
                ;; merge our columns into the database rooted on the :columns prop of StepColumnConfig
                (merge/merge-component! SPA StepColumnConfig
                                        {:columns columns})
                (next-step)))
            )
          (js/setTimeout #(m/set-value! component :busy false) 1500)))


(defsc StepUpload [this {:keys [data busy message] :as props} {:keys [next-step reset]}]
  {:query         [[:data '_] :busy :message]
   :ident         (fn [] ident)
   :initial-state (fn [_] {:busy false})}

  ;; explanation
  (c/fragment
    (when busy
      (mui/linear-progress))

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
      (dom/input {:type    "file"
                  :hidden  true
                  :accept  "text/csv"
                  :onInput (fn [e]
                             (m/set-value! this :busy true)
                             (let [file (-> e .-target .-files (aget 0))
                                   name (.-name file)
                                   reader (js/FileReader.)]
                               (set! (.-onload reader)
                                     (fn [e]
                                       (let [contents (-> e .-target .-result)]
                                         (m/set-value! this :message nil)
                                         (m/set-value! this :busy true)
                                         (c/transact! this [(handle-upload {:contents  contents
                                                                            :name      name
                                                                            :next-step next-step})]))))
                               (.readAsText reader file)))}))

    (common/action-div
      {:next  (common/next-button {:onClick  next-step
                                   :disabled (or busy (not (some? data)))})
       :reset (common/reset-button {:disabled (or busy (not (some? data)))
                                    :onClick  reset})})))

(def ui-step-upload (c/factory StepUpload))