(ns wuhl.ui.step.column-config
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [wuhl.ui.step.common :as common]
            [wuhl.material-ui :as mui]
            [wuhl.material-ui-icon :as muic]
            [wuhl.i18n :refer [tr lipsum]]
            [wuhl.models.columns :as wc]))

(def ident [:component/id :step-column-config])

;; individual column component --------------------------------------------------------------------------------
(defmutation change-type [{:keys [new-type]}]
  (action [{:keys [state component]}]
          (m/set-value! component :column/type new-type)))

(defmutation reset-form-index [_]
  (action [{:keys [state]}]
          (swap! state assoc-in (conj ident :form-index) 0)))


(defsc Column [this {:column/keys [id type options]}]
  {:query [:column/id :column/type :column/options]
   :ident :column/id}
  (mui/grid {:container true :item true :key id}
    (mui/grid {:item true :sm 6}
      (mui/typography {:variant "body1"} id))
    (mui/grid {:item true :sm 6}
      (mui/form-control {}
                        ;; TODO: helptext inside here?
                        (mui/select {:onChange (fn [e]
                                                 (let [new-type (-> e .-target .-value keyword)]
                                                   (c/transact! this [(change-type {:new-type new-type})])
                                                   (when (= new-type :primary-form)
                                                     (c/transact! this [(reset-form-index {})]))
                                                   ))
                                     :value    type}
                          (map (fn [{:keys [id name]}]
                                 (mui/menu-item {:value id :key id} name))
                               (vals wc/column-types)))))
    #_(mui/grid {:item true :sm 4}
        "Options")))

(def ui-column (c/factory Column))

;; preview component --------------------------------------------------------------------------------
(defn render-ast [this {:keys [primary-form alternate-forms blocks]} index total]
  (c/fragment
    (mui/grid {:container true :alignItems "center" :justify "space-between"}
      (mui/grid {:item true :sm 2}
        (mui/icon-button {:disabled (= index 0)
                          :onClick  #(m/set-value! this :form-index (dec index))}
          (muic/arrow-back)))
      (mui/grid {:item true :sm 8}
        (mui/typography {:variant "subtitle1"} (str primary-form " (" (inc index) (tr " of ") total ")")))
      (mui/grid {:item true :sm 2}
        (mui/icon-button {:disabled (= index (- total 1))
                          :onClick  #(m/set-value! this :form-index (inc index))}
          (muic/arrow-forward))))

    (mui/card {}
      (mui/card-header {:title primary-form})
      (mui/card-content {}
        (when (not-empty alternate-forms)
          (c/fragment
            (mui/typography {:variant "h6"} (tr "Alternate Forms"))
            (dom/ul {}
              (map #(dom/li {}
                      (mui/typography {:variant "body1"} %))
                   alternate-forms))))
        (for [{:keys [senses primary-lexcat lexcats]} blocks]
          (c/fragment
            (mui/typography {:variant "h6"} (if primary-lexcat
                                              (-> primary-lexcat
                                                  (cond-> (not-empty lexcats) (str " (" (clojure.string/join ", " lexcats) ")")))
                                              (tr "Definition")))
            (dom/ol
              (map (fn [{:keys [body comments]}]
                     (dom/li {:key body}
                       (mui/typography {:variant "body1"}
                         (cond-> body (not-empty comments) (str " (" (clojure.string/join ", " comments) ")")))))
                   senses))))))))

;; top level component --------------------------------------------------------------------------------
(defsc StepColumnConfig [this {:keys [data columns form-index] :as props} {:keys [next-step last-step reset]}]
  {:query         [[:data '_] {:columns (c/get-query Column)} :form-index]
   :ident         (fn [] ident)
   :initial-state {:columns    []
                   :form-index 0}}

  ;; explanation
  (let []
    (c/fragment
      (mui/typography {:variant "h5" :component "h1"} "Column Configuration")
      (dom/p (tr lipsum))
      (mui/container {}
        (mui/grid {:container true}
          (mui/grid {:item true :container true :md 8 :key "main"}
            (mui/grid {:container true :spacing 3}
              (mui/grid {:container true :item true}
                (mui/grid {:item true :sm 6 :key "cname"} (mui/typography {:variant "h6"} (tr "Column Name")))
                (mui/grid {:item true :sm 6 :key "ctype"} (mui/typography {:variant "h6"} (tr "Column Type")))
                #_(mui/grid {:item true :sm 4 :key "copts"} (mui/typography {:variant "h6"} (tr "Column Options"))))
              (map ui-column columns)))
          (mui/grid {:item true :md 4 :key "sidebar"}
            (c/fragment
              (mui/typography {:variant "h5" :component "h1"} "Preview")
              (if-let [error (wc/explain-config-error columns)]
                (mui/typography {:variant "body1"} error)
                ;; todo: let user select somehow
                (let [form-asts (wc/generate-ast columns (:data data))
                      ast (nth form-asts form-index)]
                  (render-ast this ast form-index (count form-asts))))))))

      (common/action-div
        {:back  (common/back-button {:onClick last-step})
         :next  (common/next-button {:onClick  next-step
                                     :disabled false})
         :reset (common/reset-button {:onClick reset})}))))

(def ui-step-column-config (c/factory StepColumnConfig))
